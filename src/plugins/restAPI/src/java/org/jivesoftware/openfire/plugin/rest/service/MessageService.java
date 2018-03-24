package org.jivesoftware.openfire.plugin.rest.service;

import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.controller.MessageController;
import org.jivesoftware.openfire.plugin.rest.entity.MessageEntity;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.manager.MUCRoomManager;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.IpUtil;
import org.jivesoftware.openfire.plugin.rest.utils.MediaType;
import org.jivesoftware.openfire.plugin.rest.utils.PushMsgUtils;
import org.jivesoftware.openfire.plugin.rest.utils.PushUtils;
import org.jivesoftware.openfire.plugin.rest.utils.RandomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.WebManager;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Presence;

import com.doowal.offlinepush.entity.MsgPush;
import com.doowal.offlinepush.entity.MsgPushToken;
import com.doowal.offlinepush.manager.MsgPushManager;

@Path("restapi/v1/messages")
public class MessageService {
	private static final Logger Log = LoggerFactory.getLogger(MessageService.class);
	private MessageController messageController;
	private MsgPushManager msgPushManager;
	private UserManager userManager;
	private MUCRoomController roomController;
	@PostConstruct
	public void init() {
		messageController = MessageController.getInstance();
		userManager = UserManager.getInstance();
		roomController = MUCRoomController.getInstance();
	}

	@POST
	@Path("/users")
	public Response sendBroadcastMessage(MessageEntity messageEntity) throws ServiceException {
		messageController.sendBroadcastMessage(messageEntity);
		return Response.status(Response.Status.CREATED).build();
	}
	@POST
	@Path("/msgpush")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String insertMsgPush(@FormParam("jid") String jid,@FormParam("jidFrom") String jidFrom,@FormParam("roomFrom") String roomFrom,
			@FormParam("status") String status) throws ServiceException {
		msgPushManager = MsgPushManager.getInstance();
		String date = new Date().getTime()+"";
		MsgPush mp = msgPushManager.findMsgPush(jid,jidFrom,roomFrom);
		if(mp == null){
			mp = new MsgPush();
			mp.setCreatedate(date);
			mp.setModifydate(date);
			mp.setMsgpush(status);
			mp.setJid(jid);
			if(jidFrom!=null&&!jidFrom.isEmpty()){
				mp.setJidfrom(jidFrom);
			}
			if(roomFrom!=null&&!roomFrom.isEmpty()){
				mp.setRoomfrom(roomFrom);
			}
			msgPushManager.save(mp);
		}else{
			mp.setMsgpush(status);
			mp.setModifydate(date);
			msgPushManager.updateMsgPush(mp);
		}
		return ResultUtils.success(null);
	}
	@POST
	@Path("/findMsgPush")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findMsgPush(@FormParam("jid") String jid, @FormParam("jidFrom") String jidFrom, @FormParam("roomFrom") String roomFrom) throws ServiceException {
		msgPushManager = MsgPushManager.getInstance();
		if((jidFrom==null&&roomFrom==null)||("".equals(jidFrom)&&"".equals(roomFrom))||(jidFrom==null&&"".equals(roomFrom))||(roomFrom==null&&"".equals(jidFrom))){
			return ResultUtils.fail("jid或者群名称为空", ErrEnum.ERR_JID_NULL.getValue());
		}
		MsgPush mp = msgPushManager.findMsgPush(jid,jidFrom,roomFrom);
		Map map = new HashMap();
		if(mp==null){
			map.put("status", null);
			return ResultUtils.success(map);
		}
		if(mp.getMsgpush().equals("1")){
			map.put("status", "1");
			return ResultUtils.success(map);
		}
		if(mp.getMsgpush().equals("0")){
			map.put("status", "0");
			return ResultUtils.success(map);
		}
		return ResultUtils.fail("服务器错误", ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@POST
	@Path("/updatePushMsgSize")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String updatePushMsgSize(@FormParam("username") String username) throws ServiceException {
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		Cache[] cs = CacheFactory.getAllCaches();
		Cache count = null;
		for(Cache c :cs){
			if("msgCount".equals(c.getName())){
				count = c;
			}
		}
		if(count!=null&&count.containsKey(username)){
			count.put(username, 0);
		}
		return ResultUtils.success(null);
	}
	@POST
	@Path("/sendMucMessage")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String sendMucMessage(@DefaultValue("conference")@QueryParam("servicename") String serviceName,
			@FormParam("roomName") String roomName,@FormParam("body") String body,
			@FormParam("username") String username,@FormParam("type") String type){
		if(!userManager.isExists(username)){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NOEXISTS.getValue());
		}
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(body==null||body.isEmpty()){
			return ResultUtils.fail("消息内容为空", ErrEnum.ERR_MESSAGEBODY_NULL.getValue());
		}
		if(type==null||type.isEmpty()){
			return ResultUtils.fail("消息类型为空", ErrEnum.ERR_TYPE_NULL.getValue());
		}
		String domain = JiveGlobals.getProperty("xmpp.domain");
		String nickName = findNickNameByUsername(username, roomName, domain);
		JID roomJid = new JID(roomName+"@"+serviceName+"."+domain+"/"+username);
		Message message = new Message();  
	    message.setBody(body);
	    message.setFrom(roomJid);
	    message.setType(Type.groupchat);
	    message.setID(UUID.randomUUID().toString());
	    message.setSubject(nickName);
	    Element msgtype =  message.addChildElement("msgtype", "com.dim");
	    Element typeElement = DocumentHelper.createElement(QName.get("type", msgtype.getNamespace())); 
	    Element timeElement = DocumentHelper.createElement(QName.get("time", msgtype.getNamespace())); 
	    timeElement.setText(new Date().getTime()+"");
	    typeElement.setText(type);
	    msgtype.add(typeElement);
	    msgtype.add(timeElement);
	    try{
		    MUCRoom mr =XMPPServer.getInstance().getMultiUserChatManager()
					.getMultiUserChatService(roomJid).getChatRoom(roomName);
		    mr.send(message);
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Log.error("send Muc Message fail: "+e.getMessage());
	    	return ResultUtils.fail("服务器异常", ErrEnum.ERR_SERVER_ERR.getValue());
	    }
	    return ResultUtils.success(null);
	}
	@POST
	@Path("/sendMucReceiveMessage")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String sendMucReceiveMessage(@DefaultValue("conference")@QueryParam("servicename") String serviceName,
			@FormParam("roomName") String roomName,@FormParam("body") String body,
			@FormParam("username") String username,@FormParam("type") String type,
			@FormParam("redPackSender") String redPackSender,@FormParam("remark") String remark){
		Log.info(body);
		if(!userManager.isExists(username)){
			return ResultUtils.fail("用户不存在", ErrEnum.ERR_USER_NOEXISTS.getValue());
		}
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(body==null||body.isEmpty()){
			return ResultUtils.fail("消息内容为空", ErrEnum.ERR_MESSAGEBODY_NULL.getValue());
		}
		if(type==null||type.isEmpty()){
			return ResultUtils.fail("消息类型为空", ErrEnum.ERR_TYPE_NULL.getValue());
		}
		String domain = JiveGlobals.getProperty("xmpp.domain");
		String nickName = findNickNameByUsername(username, roomName, domain);
		JID roomJid = new JID(roomName+"@"+serviceName+"."+domain+"/"+username);
		Message message = new Message();  
	    message.setBody(body);
	    message.setFrom(roomJid);
	    message.setType(Type.groupchat);
	    message.setID(UUID.randomUUID().toString());
	    message.setSubject(nickName);
	    Element msgtype =  message.addChildElement("msgtype", "com.dim");
	    Element typeElement = DocumentHelper.createElement(QName.get("type", msgtype.getNamespace())); 
	   
	    Element timeElement = DocumentHelper.createElement(QName.get("time", msgtype.getNamespace())); 
	    if(remark!=null&&!remark.isEmpty()){
		    Element remarkElement = DocumentHelper.createElement(QName.get("remark", msgtype.getNamespace())); 
		    remarkElement.setText(remark);
		    msgtype.add(remarkElement);
	    }
	    if(redPackSender!=null&&!redPackSender.isEmpty()){
	    	Element sender = DocumentHelper.createElement(QName.get("redPackSender", msgtype.getNamespace())); 
	 	    Element receiver = DocumentHelper.createElement(QName.get("redPackReceiver", msgtype.getNamespace()));
	 	    sender.setText(redPackSender);
		    receiver.setText(username);
		    msgtype.add(sender);
		    msgtype.add(receiver);
	    }
	    timeElement.setText(new Date().getTime()+"");
	    typeElement.setText(type);
	    msgtype.add(typeElement);
	    msgtype.add(timeElement);
	    try{
		    MUCRoom mr =XMPPServer.getInstance().getMultiUserChatManager()
					.getMultiUserChatService(roomJid).getChatRoom(roomName);
		    mr.send(message);
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Log.error("send Muc Message fail: "+e.getMessage());
	    	return ResultUtils.fail("服务器异常", ErrEnum.ERR_SERVER_ERR.getValue());
	    }
	    return ResultUtils.success(null);
	}
	@POST
	@Path("/msgPush")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String msgPush(@FormParam("username") String username,@FormParam("title") String title,
			@FormParam("content") String content){
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USER_NULL.getValue());
		}
		if(content==null||content.isEmpty()){
			content = "你收到一条消息";
		}
		if(title == null||title.isEmpty()){
			title = "服务通知";
		}
		String jid = username+"@"+JiveGlobals.getProperty("xmpp.domain");
		Cache[] cs = CacheFactory.getAllCaches();
		String token = null;
		Integer count = null;
		for(Cache c : cs){
			if("appleToken".equals(c.getName())){
				if(c!=null){
					token = (String)c.get(username);
				}
			}
			if("msgCount".equals(c.getName())){
				if(c!=null){
					count = (Integer)c.get(username);
				}
			}
		}
		if(token!=null){
			PushUtils.pushIOSMsg(token, content, jid, "settlement");
		}else{
			PushUtils.pushAndroidMsg(title, content, username);
		}
		 return ResultUtils.success(null);
	}
	@POST
	@Path("/BroadcastingMessage")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String BroadcastingMessage(@Context HttpServletRequest request,
			@FormParam("subject")String subject,@FormParam("content")String content,
			@FormParam("type")String type ){
		try {
			String domain = JiveGlobals.getProperty("xmpp.domain");
			String reDomain = JiveGlobals.getProperty("red.domain");
			String ip = IpUtil.getIp(request);
			if(!reDomain.equals(ip)){
				return ResultUtils.fail("ip不被允许", ErrEnum.ERR_SERVER_ERR.value);
			}
			if(subject==null||"".equals(subject)){
				return ResultUtils.fail("subject为空", ErrEnum.ERR_SERVER_ERR.value);
			}
			if(type==null||"".equals(type)){
				return ResultUtils.fail("类型为空", ErrEnum.ERR_SERVER_ERR.value);
			}
			if(content==null||"".equals(content)){
				return ResultUtils.fail("内容为空", ErrEnum.ERR_SERVER_ERR.value);
			}
			
			WebManager webManager = new WebManager();
			Collection<User> users = webManager.getUserManager().getUsers();//获取所有用户
			  Message message = new Message();
			  message.setType(Message.Type.chat);
			  Element msgtype = message.addChildElement("msgtype", "com.dim");
			  Element typeElement = msgtype.addElement("type");
			  typeElement.setText(type);          
			  Element timeElement = msgtype.addElement("time");
			  timeElement.setText(new Date().getTime()+"");
			  message.setFrom("admin@"+domain);//目前不加from则会导致客户端不能自动获取离线消息，除主动获取。
			  message.setBody(content);
			  message.setSubject(subject);
			  message.setID(RandomUtils.getRandNum(5));
			  PresenceManager presenceManager = webManager.getPresenceManager();
			  for (User user : users) {
			      String username = user.getUsername();
			      message.setTo(username+"@"+domain);
			      if(presenceManager.isAvailable(user)){
			          XMPPServer.getInstance().getRoutingTable().broadcastPacket(message, false);
			      } else {
			          if (!username.equals("admin@"+domain)) {
			        	  MsgPushToken msg = MsgPushManager.getInstance().selectPushToken(username);
			        	  XMPPServer.getInstance().getOfflineMessageStrategy().storeOffline(message);
			        	  if(msg!=null){
			        		  //如果不为空  推送苹果消息
			        		  if(msg.getToken()!=null&&!"".equals(msg.getToken())){
			        			  PushMsgUtils.applePushOfflineMsg(msg.getToken(), content, username+"@"+domain);
			        		  }else{
			        			  //为空推送小米消息
			        			  PushUtils.pushAndroidMsg("通知", content, username);
			        		  }
			        	  }
			          }
			      }
			  }
			  return ResultUtils.success("成功");
		} catch (Exception e) {
			Log.error("BroadcastingMessage err:"+e.getMessage());
			return ResultUtils.fail(e.getMessage(), ErrEnum.ERR_SERVER_ERR.value);
		}
	}
	@POST
	@Path("/sendPersonalMessage")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String sendPersonalMessage(@Context HttpServletRequest request,
			@FormParam("content")String content,@FormParam("type")String type,@FormParam("username")String username){
		String domain = JiveGlobals.getProperty("xmpp.domain");
		String reDomain = JiveGlobals.getProperty("red.domain");
		String ip = IpUtil.getIp(request);
		if(!reDomain.equals(ip)){
			return ResultUtils.fail("ip不被允许", ErrEnum.ERR_SERVER_ERR.value);
		}
		if(type==null||"".equals(type)){
			return ResultUtils.fail("类型为空", ErrEnum.ERR_SERVER_ERR.value);
		}
		if(content==null||"".equals(content)){
			return ResultUtils.fail("内容为空", ErrEnum.ERR_SERVER_ERR.value);
		}
		String nick = UserManager.getInstance().findUserNickName(username);
		if(nick==null||"".equals(nick)){
			nick = username;
		}
		Message message = new Message();
		message.setType(Message.Type.chat);
		Element msgtype = message.addChildElement("msgtype", "com.dim");
		Element typeElement = msgtype.addElement("type");
		typeElement.setText(type);          
		Element timeElement = msgtype.addElement("time");
		timeElement.setText(new Date().getTime()+"");
	    message.setFrom("admin@"+domain+"/Spark");//目前不加from则会导致客户端不能自动获取离线消息，除主动获取。
		message.setTo(username+"@"+domain+"/Spark");
		message.setBody(content);
	  	message.setSubject(nick);
	  	message.setID(UUID.randomUUID().toString());
	  	XMPPServer xmpp = XMPPServer.getInstance();
	  	Presence presence = null;
		try {
			presence = xmpp.getPresenceManager().getPresence(xmpp.getUserManager().getUser(username));
		} catch (UserNotFoundException e) {
			e.printStackTrace();
		}
	  	if(presence!=null){
	  		xmpp.getMessageRouter().route(message);
	  	}else{
	  		xmpp.getOfflineMessageStrategy().storeOffline(message);
	  	}
	  	Log.info(message.toXML());
	  	Log.info("presence: "+presence);
	  	return ResultUtils.success("成功");
	}
	private String findNickNameByUsername(String username,String roomName,String domain){
		String nickName = null;
		if(roomName!=null){
			String jid = username+"@"+domain;
			nickName = roomController.findRoomRemark(jid, roomName);
		}
		if(nickName==null){
			nickName = userManager.findUserNickName(username);
		}
		return nickName==null?username:nickName;
	}
}
