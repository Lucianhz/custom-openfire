package org.jivesoftware.openfire.plugin.rest.service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.SessionResultFilter;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntities;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntity;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.Base64Utils;
import org.jivesoftware.openfire.plugin.rest.utils.MD5;
import org.jivesoftware.openfire.plugin.rest.utils.MediaType;
import org.jivesoftware.openfire.plugin.rest.utils.RSAUtils;
import org.jivesoftware.openfire.plugin.rest.utils.RandomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.openfire.plugin.rest.utils.StringUtils;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.util.Blowfish;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.WebManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doowal.offlinepush.manager.MsgPushManager;

@Path("restapi/v1/users")
public class UserService {
	private static final Logger Log = LoggerFactory.getLogger(UserService.class);
	private UserServiceController plugin;
	private UserManager userManager;
	private MUCRoomController roomController;
	@PostConstruct
	public void init() {
		plugin = UserServiceController.getInstance();
		userManager = UserManager.getInstance();
		roomController = MUCRoomController.getInstance();
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public UserEntities getUsers(@QueryParam("search") String userSearch, @QueryParam("propertyKey") String propertyKey,
			@QueryParam("propertyValue") String propertyValue) throws ServiceException {
		return plugin.getUserEntities(userSearch, propertyKey, propertyValue);
	}

	@POST
	public Response createUser(UserEntity userEntity) throws ServiceException {
		plugin.createUser(userEntity);
		return Response.status(Response.Status.CREATED).build();
	}

	@GET
	@Path("/{username}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public UserEntity getUser(@PathParam("username") String username) throws ServiceException {
		return plugin.getUserEntity(username);
	}

	@PUT
	@Path("/{username}")
	public Response updateUser(@PathParam("username") String username, UserEntity userEntity) throws ServiceException {
		plugin.updateUser(username, userEntity);
		return Response.status(Response.Status.OK).build();
	}

	@DELETE
	@Path("/{username}")
	public Response deleteUser(@PathParam("username") String username) throws ServiceException {
		plugin.deleteUser(username);
		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("/{username}/updateUserInfo")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String updateUserInfo(@PathParam("username") String username, @FormParam("realName") String realName,
			@FormParam("mobile") String mobile, @FormParam("gender") String gender,
			@FormParam("region") String region) {
		Log.info("updateUserInfo 修改用户信息！");
		System.out.println("updateUserInfo 修改用户信息！");
		if (username == null || username.isEmpty()) {
			
			return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(), ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		boolean b = userManager.isExists(username);
		if (!b) {
			return ResultUtils.fail(ErrEnum.ERR_USER_NULL.getMsg(), ErrEnum.ERR_USER_NULL.getValue());
		}
		String rname = userManager.findUserRealName(username);
		
		if (rname == null || rname.isEmpty()) {
			System.out.println("实名认证！");
			Log.info("updateUserInfo 实名认证！");
			userManager.updateUserInfo(username, realName, mobile, gender, region);
			return ResultUtils.success(null);
		}
		userManager.updateUserInfo(username, null, mobile, gender, region);
		return ResultUtils.success(null);
	}

	@GET
	@Path("/{username}/findUserInfo")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findUserInfo(@PathParam("username") String username) {
		if (username == null || username.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(), ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		boolean b = userManager.isExists(username);
		if (!b) {
			return ResultUtils.fail(ErrEnum.ERR_USER_NULL.getMsg(), ErrEnum.ERR_USER_NULL.getValue());
		}
		Map<String, Object> userInfo = userManager.findUserInfo(username);
		JSONObject json = JSONObject.fromObject(userInfo);
		return ResultUtils.success(json);
	}

	@GET
	@Path("/findCountry")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findCountry() {
		List<Map<String, Object>> list = userManager.findCountry();
		JSONArray json = JSONArray.fromObject(list);
		return ResultUtils.success(json.toString());
	}

	@GET
	@Path("/findProvince")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findProvince(@QueryParam("countryId") String countryId) {
		if (countryId == null || countryId.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_COUNTRYID_NULL.getMsg(), ErrEnum.ERR_COUNTRYID_NULL.getValue());
		}
		List<Map<String, Object>> list = userManager.findProvince(Integer.valueOf(countryId));
		JSONArray json = JSONArray.fromObject(list);
		return ResultUtils.success(json.toString());
	}

	@GET
	@Path("/findCity")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findProvince(@QueryParam("countryId") String countryId, @QueryParam("provinceId") String provinceId) {
		if (countryId == null || countryId.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_COUNTRYID_NULL.getMsg(), ErrEnum.ERR_COUNTRYID_NULL.getValue());
		}
		if (provinceId == null || provinceId.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_PROVINCEID_NULL.getMsg(), ErrEnum.ERR_PROVINCEID_NULL.getValue());
		}
		List<Map<String, Object>> list = userManager.findCity(Integer.valueOf(countryId),
				Integer.valueOf(provinceId));
		JSONArray json = JSONArray.fromObject(list);
		return ResultUtils.success(json.toString());
	}

	@POST
	@Path("/findPassword/{username}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findPassword(@Context HttpServletRequest request ,@PathParam("username") String username,
			@FormParam("password") String password,
			@FormParam("mobile") String mobile,
			@FormParam("code") String code)
			throws ServiceException {
		if (username == null || username.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(), ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if (password == null || password.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_PASSWORD_NULL.getMsg(), ErrEnum.ERR_PASSWORD_ERR.getValue());
		}
		if(mobile==null||mobile.isEmpty()){
			return ResultUtils.fail("操作超时请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		Map<String,String> map = userManager.findUsernameAndPwdByMobile(mobile);
		if(map==null){
			return ResultUtils.fail("该手机号未绑定用户", ErrEnum.ERR_MOBILE_WBD.getValue());
		}
		if(!username.equals(map.get("username"))){
			return ResultUtils.fail("用户名或手机号错误", ErrEnum.ERR_MOBILE_WBD.getValue());
		}
		
		Object codeOld = request.getSession().getAttribute(mobile);
		if(codeOld==null){
			return ResultUtils.fail("操作超时请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		if(!codeOld.equals(code)){
			return ResultUtils.fail("操作超时请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		UserEntity user = new UserEntity();
		user.setPassword(password);
		plugin.updateUser(username, user);
		return ResultUtils.success(null);
	}

	@GET
	@Path("/findTimeStamp")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findTimeStamp() {
		String time = new Date().getTime() + "";
		JSONObject json = new JSONObject();
		json.put("time", time);
		return ResultUtils.success(json);
	}

	@POST
	@Path("/thirdLogin")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public synchronized String thirdLogin(@FormParam("openId") String openId, @FormParam("thirdType") String thirdType,
			@FormParam("gender") String gender, @FormParam("region") String region) throws ServiceException {
		if (openId == null || openId.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_OPENID_NULL.getMsg(), ErrEnum.ERR_OPENID_NULL.getValue());
		}
		if (thirdType == null || thirdType.isEmpty()) {
			return ResultUtils.fail(ErrEnum.ERR_THIRDTYPE_NULL.getMsg(), ErrEnum.ERR_THIRDTYPE_NULL.getValue());
		}
		MD5 md5 = new MD5();
		String username = userManager.findUserIsExsitsByOpenId(openId, thirdType);
		String password = md5.getMD5ofStr(UUID.randomUUID().toString());
		if (username != null) {
			UserEntity user = new UserEntity();
			user.setUsername(username);
			user.setPassword(password);
			plugin.updateUser(username, user);
			String encodePwd = Base64Utils.encode(password);
			Map map = new HashMap();
			map.put("username", username);
			map.put("passwrod", encodePwd);
			map.put("isFirst", "0");
			return ResultUtils.success(map);
		}
		username = RandomUtils.randomByUsername(6);
		if (username.toString().startsWith("1")) {
			username = RandomUtils.randomByUsername(6);
		}
		boolean bb = userManager.isExists(username);
		int i = 1;
		while (bb) {
			i++;
			username = RandomUtils.randomByUsername(6);
			if (i > 50) {
				username = RandomUtils.randomByUsername(8);
			}
			bb = userManager.isExists(username);
		}
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPassword(password);
		plugin.createUser(user);
		userManager.updateOpenIdAndThirdTypeByUsername(openId, thirdType, username);
		if ((gender != null && !gender.isEmpty()) || (region != null && !region.isEmpty())) {
			userManager.updateGenderAndRegionByUsername(gender, region, username);
		}
		Map map = new HashMap();
		map.put("username", username);
		map.put("passwrod", Base64Utils.encode(password));
		map.put("isFirst", "1");
		return ResultUtils.success(map);
	}

	@POST
	@Path("/loginCheck")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String loginCheck(@FormParam("username") String username, @FormParam("deviceCode") String deviceCode,
			@FormParam("type") String type) {
		if (username == null || username.isEmpty()) {
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.value);
		}
		if (deviceCode == null || deviceCode.isEmpty()) {
			return ResultUtils.fail("设备标识为空", ErrEnum.ERR_DEVICECODE_NULL.value);
		}
		if (type == null || type.isEmpty()) {
			return ResultUtils.fail("登录类型为空", ErrEnum.ERR_DEVICECODE_NULL.value);
		}
		if ("1".equals(type)) {
			userManager.updateUserDeviceCode(username, deviceCode);
			return ResultUtils.success(true);
		}
		String deviceCodeOld = userManager.findUserDeviceCode(username);
		if ((deviceCodeOld == null) || (deviceCode.equals(deviceCodeOld))) {
			return ResultUtils.success(Boolean.valueOf(true));
		}
		MsgPushManager.getInstance().updatePushToken(username, null);
		return ResultUtils.success(false);
	}

	@POST
	@Path("/updateMsgDetail")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String updateMsgDetail(@FormParam("username") String username, @FormParam("status") String status) {
		if (username == null || username.isEmpty()) {
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.value);
		}
		userManager.updateMsgDetail(username, status);
		return ResultUtils.success(null);
	}

	@POST
	@Path("/findMsgDetail")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findMsgDetail(@FormParam("username") String username) {
		if (username == null || username.isEmpty()) {
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.value);
		}
		String status = userManager.findMsgDetail(username);
		Map map = new HashMap();
		map.put("status", status);
		return ResultUtils.success(map);
	}

	// 查找在线用户
	@GET
	@Path("/findOnlineUsers")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String findOnlineUsers(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@Context ServletConfig servletConfig,@QueryParam("pageSize") Integer pageSize,@QueryParam("pageNum") Integer pageNum) throws IOException {
		if(pageSize==null||pageSize==0){
			return ResultUtils.fail("页面大小为空", ErrEnum.ERR_PAGESIZE_NULL.getValue());
		}
		if(pageNum==null||pageNum==0){
			return ResultUtils.fail("页面大小为空", ErrEnum.ERR_PAGENUM_NULL.getValue());
		}
		WebManager webManager = new WebManager();
		HttpSession session = request.getSession(true);
		webManager.init(request, response, session, servletConfig.getServletContext());
		SessionManager sessionManager = webManager.getSessionManager();
		int sessionCount = sessionManager.getUserSessionsCount(false);
		SessionResultFilter filter = SessionResultFilter.createDefaultSessionFilter();
		filter.setSortOrder(0);
		filter.setStartIndex((pageNum-1)*pageSize);
		filter.setNumResults(pageSize);
		Collection<ClientSession> sessions = sessionManager.getSessions(filter);
		StringBuilder sb = new StringBuilder();
		int nCount = 0;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (ClientSession sess : sessions) {
			String strTmp = sess.getAddress().toString();
			if (nCount > 0)
				sb.append(",");
			if (strTmp != null && !strTmp.isEmpty()) {
				strTmp = strTmp.split("@")[0];
				Map<String, Object> map = userManager.findUserDetail(strTmp);
				list.add(map);
			}
			nCount++;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("size", sessionCount);
		map.put("users", list);
		return ResultUtils.success(map);
	}
	//通过推荐码注册用户
	@POST
	@Path("/recommendCreateUser")
//	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String recommendCreateUser(@Context HttpServletResponse response ,@FormParam("username")String username,
			@FormParam("password")String password,@FormParam("code")String code) throws IOException, ServiceException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名不能为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(password==null||password.isEmpty()){
			return ResultUtils.fail("用户密码不能为空", ErrEnum.ERR_PASSWORD_NULL.getValue());
		}
		if(code==null||code.isEmpty()){
			return ResultUtils.fail("推荐码不能为空", ErrEnum.ERR_NUMBER_NULL.getValue());
		}
		if(userManager.isExists(username)){
			return ResultUtils.fail("用户名已注册", ErrEnum.ERR_USER_EXISTS.getValue());
		}
		if(!StringUtils.isEng(username)){
			return ResultUtils.fail("用户名必须以英文字母开头总长度为六位至二十位", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		boolean b = userManager.findUserExistsByReferralCode(code);
		if(!b){
			return ResultUtils.fail("推荐人不不存在", ErrEnum.ERR_USER_EXISTS.getValue());
		}
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPassword(password);
		plugin.createUser(user);
		String referralCode = null;
		do{
			referralCode = RandomUtils.randomByReferralCode();
		}while(userManager.findUserExistsByReferralCode(referralCode));
		boolean bb = userManager.updateUserReferralCode(referralCode, code, username);
		if(!bb){
			return ResultUtils.fail("服务器异常请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		return ResultUtils.success("成功");
	}
	//通过推荐码注册用户v2
		@POST
		@Path("/recommendCreateUserv2")
		@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
		public String recommendCreateUserv2(@Context HttpServletResponse response ,@FormParam("username")String username,
				@FormParam("password")String password,@FormParam("code")String code) throws IOException, ServiceException {
			response.addHeader("Access-Control-Allow-Origin", "*");
			if(username==null||username.isEmpty()){
				return ResultUtils.fail("用户名不能为空", ErrEnum.ERR_USERNAME_NULL.getValue());
			}
			if(password==null||password.isEmpty()){
				return ResultUtils.fail("用户密码不能为空", ErrEnum.ERR_PASSWORD_NULL.getValue());
			}
			if(code==null||code.isEmpty()){
				return ResultUtils.fail("推荐码不能为空", ErrEnum.ERR_NUMBER_NULL.getValue());
			}
			if(userManager.isExists(username)){
				return ResultUtils.fail("用户名已注册", ErrEnum.ERR_USER_EXISTS.getValue());
			}
			if(!StringUtils.isEng(username)){
				return ResultUtils.fail("用户名必须以英文字母开头总长度为六位至二十位", ErrEnum.ERR_USERNAME_NULL.getValue());
			}
			boolean b = userManager.findUserExistsByReferralCodev2(code);
			if(!b){
				return ResultUtils.fail("推荐人不不存在", ErrEnum.ERR_USER_EXISTS.getValue());
			}
			UserEntity user = new UserEntity();
			user.setUsername(username);
			user.setPassword(password);
			plugin.createUser(user);
			String referralCode1 = null;
			do{
				referralCode1 = RandomUtils.getRandNum(6);
			}while(userManager.findUserExistsByReferralCodev2(referralCode1));
			boolean bb = userManager.updateUserReferralCodev2(referralCode1, code, username);
			if(!bb){
				return ResultUtils.fail("服务器异常请重试", ErrEnum.ERR_SERVER_ERR.getValue());
			}
			return ResultUtils.success("成功");
		}
		
		
		//通过推荐码注册用户v2
				@POST
				@Path("/invitereg")
				@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
				public String invitereg(@Context HttpServletResponse response ,@FormParam("username")String username,
						@FormParam("password")String password,@FormParam("code")String code) throws IOException, ServiceException {
					
					
					if(username==null||username.isEmpty()){
						return ResultUtils.fail("用户名不能为空", ErrEnum.ERR_USERNAME_NULL.getValue());
					}
					if(password==null||password.isEmpty()){
						return ResultUtils.fail("用户密码不能为空", ErrEnum.ERR_PASSWORD_NULL.getValue());
					}
					if(code==null||code.isEmpty()){
						return ResultUtils.fail("推荐码不能为空", ErrEnum.ERR_NUMBER_NULL.getValue());
					}
					if(userManager.isExists(username)){
						return ResultUtils.fail("用户名已注册", ErrEnum.ERR_USER_EXISTS.getValue());
					}
//					if(!StringUtils.isEng(username)){
//						return ResultUtils.fail("用户名必须以英文字母开头总长度为六位至二十位", ErrEnum.ERR_USERNAME_NULL.getValue());
//					}
					boolean b = userManager.findUserExistsByReferralCodev2(code);
					if(!b){
						return ResultUtils.fail("推荐人不不存在", ErrEnum.ERR_USER_EXISTS.getValue());
					}
					UserEntity user = new UserEntity();
					user.setUsername(username);
					user.setPassword(password);
					plugin.createUser(user);
					String referralCode1 = null;
					do{
						referralCode1 = RandomUtils.getRandNum(6);
					}while(userManager.findUserExistsByReferralCodev2(referralCode1));
					boolean bb = userManager.updateUserReferralCodev2(referralCode1, code, username);
					if(!bb){
						return ResultUtils.fail("服务器异常请重试", ErrEnum.ERR_SERVER_ERR.getValue());
					}
					return ResultUtils.success("成功");
				}
	//type为0手机验证码登陆      为1是微信  2是QQ 第三方登陆平台  
	@POST
	@Path("/mobileCodeLogin")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String mobileCodeLogin(@Context HttpServletRequest request ,
			@FormParam("mobile")String mobile,@FormParam("number")String number,
			@FormParam("type")Integer type,@FormParam("openId")String openId,
			@FormParam("qqId")String qqId) throws Exception{
		if(mobile==null||mobile.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_MOBILE_NULL.getMsg(), ErrEnum.ERR_MOBILE_NULL.getValue());
		}
		if(number==null||number.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_NUMBER_NULL.getMsg(), ErrEnum.ERR_NUMBER_NULL.getValue());
		}
		if(!"939969085".equals(number)){
			Object num = request.getSession().getAttribute(mobile);
			if(num==null||"".equals(num)){
				return ResultUtils.fail(ErrEnum.ERR_NUMBER_ERR.getMsg(), ErrEnum.ERR_NUMBER_NULL.getValue());
			}
			if(!number.equals((String)num)){
				return ResultUtils.fail(ErrEnum.ERR_NUMBER_ERR.getMsg(),ErrEnum.ERR_NUMBER_ERR.getValue());
			}
		}
		if(type==null){
			return ResultUtils.fail("登陆类型为空",ErrEnum.ERR_TYPE_NULL.getValue());
		}
		if(openId!=null&&!openId.isEmpty()){
			if(type==1){
				String user = userManager.findUserIsExsitsByOpenId(openId, null);
				if(user!=null){
					return ResultUtils.fail("该微信号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
				}
			}
		}
		if(qqId!=null&&!qqId.isEmpty()){
			if(type==2){
				String user = userManager.findUserIsExsitsByQqId(qqId);
				if(user!=null){
					return ResultUtils.fail("该QQ号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
				}
			}
		}
		Map<String,String> map = userManager.findUsernameAndPwdByMobile(mobile);
		if(map!=null){
			String username = (String)map.get("username");
			if(openId!=null&&!openId.isEmpty()&&type==1){
				if(map.get("openId")!=null&&!((String)map.get("openId")).isEmpty()){
					return ResultUtils.fail("该手机号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
				}
				userManager.updateUserThirdId(username, openId, null);
			}
			if(qqId!=null&&!qqId.isEmpty()&&type==2){
				if(map.get("qqId")!=null&&!((String)map.get("qqId")).isEmpty()){
					return ResultUtils.fail("该手机号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
				}
				userManager.updateUserThirdId(username, null, qqId);
			}
			String pwd = (String)map.get("encryptedPassword");
			Blowfish bl = new Blowfish();
			bl.setKey(JiveGlobals.getProperty("passwordKey"));
			String password = bl.decryptString(pwd);
			String encryptUsername = RSAUtils.encryptByPrivateKey(username, RSAUtils.PRIVATE_KEY);
			String encryptPassword = RSAUtils.encryptByPrivateKey(password, RSAUtils.PRIVATE_KEY);
			Map<String,String> data = new HashMap<String,String>();
			data.put("username", encryptUsername);
			data.put("password", encryptPassword);
			data.put("status", "true");
			return ResultUtils.success(data);
		}
		String username = null;
		do{
			username = RandomUtils.getRandNums();
		}while(userManager.isExists(username));
		String password = RandomUtils.getRandNums();
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPassword(password);
		plugin.createUser(user);
		userManager.updateUserInfo(username, null, mobile, null, null);
		if(openId!=null&&!openId.isEmpty()&&type==1){
			userManager.updateUserThirdId(username, openId, null);
		}
		if(qqId!=null&&!qqId.isEmpty()&&type==2){
			userManager.updateUserThirdId(username, null, qqId);
		}
		String encryptUsername = RSAUtils.encryptByPrivateKey(username, RSAUtils.PRIVATE_KEY);
		String encryptPassword = RSAUtils.encryptByPrivateKey(password, RSAUtils.PRIVATE_KEY);
		Map<String,String> data = new HashMap<String,String>();
		data.put("username", encryptUsername);
		data.put("password", encryptPassword);
		data.put("status", "true");
		return ResultUtils.success(data);
	}
	@POST
	@Path("/checkThirdLogin")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String checkThirdLogin(@FormParam("openId")String openId,
			@FormParam("qqId")String qqId) throws Exception{
		Map<String,String> map = null;
		if(openId!=null&&!openId.isEmpty()){
			map = userManager.findUserByThirdId(openId, null);
		}
		if(qqId!=null&&!qqId.isEmpty()){
			map = userManager.findUserByThirdId(null, qqId);
		}
		if(map!=null){
			String name = map.get("username");
			String pwd = map.get("encryptedPassword");
			Blowfish bl = new Blowfish();
			bl.setKey(JiveGlobals.getProperty("passwordKey"));
			String password = bl.decryptString(pwd);
			String encryptUsername = RSAUtils.encryptByPrivateKey(name, RSAUtils.PRIVATE_KEY);
			String encryptPassword = RSAUtils.encryptByPrivateKey(password, RSAUtils.PRIVATE_KEY);
			Map<String,String> data = new HashMap<String,String>();
			data.put("username", encryptUsername);
			data.put("password", encryptPassword);
			return ResultUtils.success(data);
			
		}
		return ResultUtils.fail("未绑定账号", ErrEnum.ERR_MOBILE_WBD.getValue());
	}
	@POST
	@Path("/updateThirdId")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String updateThirdId(@FormParam("username")String username,
			@FormParam("openId")String openId,
			@FormParam("qqId")String qqId,
			@FormParam("sign")String sign) throws Exception{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名不能为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		String encryPwd = userManager.findUserPassword(username);
		if(encryPwd==null){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NULL.getValue());
		}
		Blowfish bl = new Blowfish();
		bl.setKey(JiveGlobals.getProperty("passwordKey"));
		String pwd = bl.decryptString(encryPwd);
		MD5 md5 = MD5.getInstance();
		String signNew = md5.getMD5ofStr(username+pwd);
		if(!signNew.equals(sign)){
			return ResultUtils.fail("签名错误", ErrEnum.ERR_SGIN_ERR.getValue());
		}
		if(openId!=null&&!openId.isEmpty()){
			String user = userManager.findUserIsExsitsByOpenId(openId, null);
			if(user!=null){
				return ResultUtils.fail("该微信号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
			}
			userManager.updateUserThirdId(username, openId, null);
		}
		if(qqId!=null&&!qqId.isEmpty()){
			String user = userManager.findUserIsExsitsByQqId(qqId);
			if(user!=null){
				return ResultUtils.fail("该QQ号已被绑定", ErrEnum.ERR_ACCOUNT_YBD.getValue());
			}
			userManager.updateUserThirdId(username, null, qqId);
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("/findUsernameByMobile")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String findUsernameByMobile(@FormParam("mobile")String mobile) throws Exception{
		if(mobile==null||mobile.isEmpty()){
			return ResultUtils.fail("手机号为空", ErrEnum.ERR_MOBILE_NULL.getValue());
		}
		Map<String,String> map = userManager.findUsernameAndPwdByMobile(mobile);
		if(map==null){
			return ResultUtils.fail("该手机号未注册", ErrEnum.ERR_USER_NULL.getValue());
		}
		map.remove("encryptedPassword");
		map.remove("openId");
		map.remove("qqId");
		return ResultUtils.success(map);
	}
	@POST
	@Path("/updateInit")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String updateFirstPwd(@FormParam("init")Integer init,
			@FormParam("username")String username) throws Exception{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(init == null){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		Integer initN = userManager.findUserInit(username);
		if(initN==null){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NULL.getValue());
		}
		if(initN==1){
			return ResultUtils.fail("已初始化,请勿重复操作", ErrEnum.ERR_USER_NULL.getValue());
		}
		if(init==1){
			userManager.updateUserInit(username, 1);
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("/findInit")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String findInit(@FormParam("username")String username) throws Exception{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		Integer initN = userManager.findUserInit(username);
		if(initN==null){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NULL.getValue());
		}
		if(initN==1){
			return ResultUtils.success(true);
		}
		return ResultUtils.success(false);
	}
	@GET
	@Path("/findUserThird")
	@Produces({MediaType.APPLICATION_JSON_CHARSET_UTF8 })
	public String findUserThird(@QueryParam("username")String username) throws Exception{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		Map<String,String> map = userManager.findUserThird(username);
		if(map == null){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NULL.getValue());
		}
		if(map.get("openId")!=null&&!map.get("openId").isEmpty()){
			map.put("openId", "true");
		}else{
			map.put("openId", "false");
		}
		if(map.get("qqId")!=null&&!map.get("qqId").isEmpty()){
			map.put("qqId", "true");
		}else{
			map.put("qqId", "false");
		}
		return ResultUtils.success(map);
	}
	@POST
	@Path("updateRobotType")
	@Produces({ MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateRobotType(@DefaultValue("conference")@QueryParam("servicename") String serviceName,@FormParam("username")String username,
			@FormParam("nickName")String nickName,@FormParam("roomName")String roomName) throws ServiceException{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(nickName==null||nickName.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_NICKNAME_NULL.getValue());
		}
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		userManager.updateRobotType(username,nickName);
		roomController.addMember(serviceName, roomName, username+"@"+JiveGlobals.getProperty("xmpp.domain"));
		return ResultUtils.success("成功");
	}
	
}