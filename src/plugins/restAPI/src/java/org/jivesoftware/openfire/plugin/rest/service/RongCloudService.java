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

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.SessionResultFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.XMPPServerInfo;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntities;
import org.jivesoftware.openfire.plugin.rest.entity.RongTokenEntity;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntities;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntity;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.manager.MUCRoomManager;
import org.jivesoftware.openfire.plugin.rest.manager.RongCloudManager;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.Base64Utils;
import org.jivesoftware.openfire.plugin.rest.utils.MD5;
import org.jivesoftware.openfire.plugin.rest.utils.MediaType;
import org.jivesoftware.openfire.plugin.rest.utils.RSAUtils;
import org.jivesoftware.openfire.plugin.rest.utils.RandomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.openfire.plugin.rest.utils.StringUtils;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.UserCollection;
import org.jivesoftware.util.Blowfish;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.WebManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doowal.offlinepush.manager.MsgPushManager;
import com.rong.RongCloud;
import com.rong.models.TokenResult;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

@Path("restapi/v1/rong")
public class RongCloudService {

	private RongCloudManager rongCloudManager;
	private RongCloud rongCloud;
	private static final Logger Log = LoggerFactory.getLogger(RongCloudService.class);
	@PostConstruct
	public void init() {
		rongCloudManager = RongCloudManager.getInstance();
		rongCloud = 
				RongCloud.getInstance(JiveGlobals.getProperty("rongAppKey"),
						JiveGlobals.getProperty("rongAppSecret"));
	}
	@POST
	@Path("findRongToken")
	@Produces({ MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String findRongToken(@FormParam("username")String username,
			@FormParam("nickName")String nickName,
			@FormParam("picUrl")String picUrl ) throws ServiceException{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(nickName==null){
			nickName = "";
		}
		if(picUrl==null){
			picUrl="";
		}
		RongTokenEntity rt= rongCloudManager.findRongTokenByUsername(username);
		if(rt!=null){
			return ResultUtils.success(rt);
		}
		TokenResult userGetTokenResult =null;
		try {
			userGetTokenResult = rongCloud.user.getToken(username, nickName, picUrl);
		} catch (Exception e) {
			Log.error("service findRongToken err: " + e.getMessage());
			return ResultUtils.fail("服务器错误,请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		if(userGetTokenResult==null||userGetTokenResult.getCode()!=200){
			return ResultUtils.fail("服务器错误,请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		rongCloudManager.saveRongToken(username, userGetTokenResult.getToken());
		rt = new RongTokenEntity();
		rt.setUsername(username);
		rt.setToken(userGetTokenResult.getToken());
		return ResultUtils.success(rt);
	}
	@POST
	@Path("updateRongToken")
	@Produces({ MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateRongToken(@FormParam("username")String username,
			@FormParam("nickName")String nickName,
			@FormParam("picUrl")String picUrl ) throws ServiceException{
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(nickName==null){
			nickName = "";
		}
		if(picUrl==null){
			picUrl="";
		}
		TokenResult userGetTokenResult =null;
		try {
			userGetTokenResult = rongCloud.user.getToken(username, nickName, picUrl);
		} catch (Exception e) {
			Log.error("service findRongToken err: " + e.getMessage());
			return ResultUtils.fail("服务器错误,请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		if(userGetTokenResult==null||userGetTokenResult.getCode()!=200){
			return ResultUtils.fail("服务器错误,请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		boolean result = rongCloudManager.updateRongToken(username, userGetTokenResult.getToken());
		if(!result){
			return ResultUtils.fail("服务器错误,请重试", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		RongTokenEntity rt = new RongTokenEntity();
		rt.setUsername(username);
		rt.setToken(userGetTokenResult.getToken());
		return ResultUtils.success(rt);
	}
}