package org.jivesoftware.openfire.plugin.rest.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.RandomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.openfire.plugin.rest.utils.SendMessageUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ValidateCode;
import com.aliyuncs.exceptions.ClientException;

import net.sf.json.JSONObject;

/**
 * Title:
 * Description: 验证码接口
 * @author lhp
 * @date 2017年1月12日 上午9:39:35
 */
@Path("restapi/v1/validatecode")
public class ValidateCodeService {
	/**
	 * Description:获取图片验证码并放入session中
	 * @param String username
	 * @author lhp
	 * @date 2017年1月12日 下午3:11:41
	 */
	/*@GET
	@Path("/findImageCode")
	public void findImageCode(@Context HttpServletRequest request,@Context HttpServletResponse response,
			@QueryParam("username") String username){
		ValidateCode vc = new ValidateCode(120, 40, 4, 50);
		System.out.println(vc.getCode());
		request.getSession().setAttribute(username, vc.getCode());
		try {
			vc.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	/**
	 * Description:验证输入的图片验证码是否正确
	 * @param String username
	 * @param String imageCode
	 * @author lhp
	 * @date 2017年1月12日 下午3:12:42
	 */
	/*@GET
	@Path("/checkImgeCode")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String checkImgeCode(@Context HttpServletRequest request,@Context HttpServletResponse response,
			@QueryParam("username") String username,@QueryParam("imgeCode") String imgeCode){
		String code = (String)request.getSession().getAttribute(username);
		if(code==null||"".equals(code)){
			return ResultUtils.fail(ErrEnum.ERR_IMAGECODE_ERR.getMsg(), ErrEnum.ERR_IMAGECODE_ERR.getValue());
		}
		if(imgeCode==null||"".equals(imgeCode)){
			return ResultUtils.fail(ErrEnum.ERR_IMAGECODE_ERR.getMsg(), ErrEnum.ERR_IMAGECODE_ERR.getValue());
		}
		imgeCode = imgeCode.toUpperCase();
		if(!code.equals(imgeCode)){
			return ResultUtils.fail(ErrEnum.ERR_IMAGECODE_ERR.getMsg(), ErrEnum.ERR_IMAGECODE_ERR.getValue());
		}
		request.getSession().removeAttribute(username);
		return ResultUtils.success(null);
	}*/
	/**
	 * Description:获取短信验证码
	 * @param 
	 * @author lhp
	 * @throws ClientException 
	 * @date 2017年1月17日 下午2:08:11
	 */
	@GET
	@Path("/sendMessage/{mobile}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String sendMessage(@Context HttpServletRequest request,@PathParam("mobile") String mobile) throws ClientException{
		if(mobile==null||mobile.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_MOBILE_NULL.getMsg(), ErrEnum.ERR_MOBILE_NULL.getValue());
		}
		String number = RandomUtils.getRandNum(6);
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(5*60);
		session.setAttribute(mobile, number);
		boolean b = SendMessageUtils.sendSMCode1(mobile, number);
		if(b){
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("请重试", ErrEnum.ERR_MOBILE_NULL.getMsg());
		
	}
	/**
	 * Description: 验证短信验证码是否正确
	 * @param 
	 * @author lhp
	 * @date 2017年1月17日 下午2:30:51
	 */
	@GET
	@Path("/checkMessage/{mobile}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String checkMessage(@Context HttpServletRequest request,@PathParam("mobile") String mobile,
			@QueryParam("number") String number){
		if(mobile==null||mobile.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_MOBILE_NULL.getMsg(), ErrEnum.ERR_MOBILE_NULL.getValue());
		}
		if(number==null||number.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_NUMBER_NULL.getMsg(), ErrEnum.ERR_NUMBER_NULL.getValue());
		}
		String num = (String)request.getSession().getAttribute(mobile);
		if(number.trim().equals(num)){
			return ResultUtils.success(null);
		}
		return ResultUtils.fail(ErrEnum.ERR_NUMBER_ERR.getMsg(), ErrEnum.ERR_NUMBER_ERR.getValue());
	}
	@GET
	@Path("/findPassword")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findPassword(@Context HttpServletRequest request,@QueryParam("username") String username) throws ClientException{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(),ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		boolean b = UserManager.getInstance().isExists(username);
		if(!b){
			return ResultUtils.fail(ErrEnum.ERR_USER_NULL.getMsg(), ErrEnum.ERR_USER_NULL.getValue());
		}
		Map<String,Object> map = UserManager.getInstance().findUserInfo(username);
		String mobile = null;
		if(map!=null&&map.size()>0){
			mobile = (String)map.get("mobile");
		}
		if(mobile == null||mobile.isEmpty()){
			return ResultUtils.fail(ErrEnum.ERR_MOBILE_WBD.getMsg(), ErrEnum.ERR_MOBILE_WBD.getValue());
		}
		sendMessage(request, mobile);
		JSONObject json = new JSONObject();
		json.put("mobile", mobile);
		return ResultUtils.success(json);
	}
}







