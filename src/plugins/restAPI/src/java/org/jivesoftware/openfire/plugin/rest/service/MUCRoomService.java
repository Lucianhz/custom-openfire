package org.jivesoftware.openfire.plugin.rest.service;

import com.alibaba.fastjson.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.cluster.RoomUpdatedEvent;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.GameProperty;
import org.jivesoftware.openfire.plugin.rest.entity.MUCChannelType;
import org.jivesoftware.openfire.plugin.rest.entity.MUCMemberEntity;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.entity.ParticipantEntities;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.manager.MUCRoomManager;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.MediaType;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Response.Status;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("restapi/v1/chatrooms")
public class MUCRoomService {
	private static Logger Log = LoggerFactory.getLogger(MUCRoomService.class);
	private MUCRoomController plugin;
	private UserManager userManager;

	@PostConstruct
	public void init() {
		plugin = MUCRoomController.getInstance();
		userManager= UserManager.getInstance();
	}
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public MUCRoomEntities getMUCRooms(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@DefaultValue(MUCChannelType.PUBLIC) @QueryParam("type") String channelType,
			@QueryParam("search") String roomSearch,
			@DefaultValue("false") @QueryParam("expandGroups") Boolean expand) {
		return MUCRoomController.getInstance().getChatRooms(serviceName, channelType, roomSearch, expand);
	}

	@GET
	@Path("/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public MUCRoomEntity getMUCRoomJSON2(@PathParam("roomName") String roomName,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@DefaultValue("false") @QueryParam("expandGroups") Boolean expand) throws ServiceException {
		return MUCRoomController.getInstance().getChatRoom(roomName, serviceName, expand);
	}


	@DELETE
	@Path("/{roomName}")
	public Response deleteMUCRoom(@PathParam("roomName") String roomName,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName) throws ServiceException {
		MUCRoomController.getInstance().deleteChatRoom(roomName, serviceName);
		return Response.status(Status.OK).build();
	}

	@POST
	public Response createMUCRoom(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			MUCRoomEntity mucRoomEntity) throws ServiceException {
		MUCRoomController.getInstance().createChatRoom(serviceName, mucRoomEntity);
		return Response.status(Status.CREATED).build();
	}
	@GET
	@Path("/{roomName}/participants")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public ParticipantEntities getMUCRoomParticipants(@PathParam("roomName") String roomName,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName) {
		return MUCRoomController.getInstance().getRoomParticipants(roomName, serviceName);
	}
	@PUT
	@Path("/{roomName}")
	public Response udpateMUCRoom(@PathParam("roomName") String roomName,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName, MUCRoomEntity mucRoomEntity)
			throws ServiceException {
		MUCRoomController.getInstance().updateChatRoom(roomName, serviceName, mucRoomEntity);
		return Response.status(Status.OK).build();
	}


//----------------------------------------------------------------------------------------------------------------------------------


	@PUT
	@Path("/updateRoomPic/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String udpateMUCRoomPic(@PathParam("roomName") String roomName,
			@QueryParam("roomPic") String roomPic,@DefaultValue("conference") @QueryParam("servicename") String serviceName)
			throws ServiceException {
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(roomPic==null||"".equals(roomPic)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMPIC_NULL.getMsg(),ErrEnum.ERR_ROOMPIC_NULL.getValue());
		}
		boolean b = plugin.updateRoomPic(roomName,  roomPic);
		if(b){
			return ResultUtils.success(null);
		}
		return ResultUtils.fail(ErrEnum.ERR_SERVER_ERR.getMsg(),ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@GET
	@Path("/findRoomPic/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findRoomPic(@PathParam("roomName") String roomName)
			throws ServiceException {
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		String roomPic = plugin.findRoomPic(roomName);
		JSONObject json = new JSONObject();
		json.put("roomPic", roomPic);
		if(roomPic!=null&&!"".equals(roomPic)){
			return ResultUtils.success(json);
		}
		return ResultUtils.fail(ErrEnum.ERR_SERVER_ERR.getMsg(),ErrEnum.ERR_SERVER_ERR.getValue());
	}

	@GET
	@Path("/isOwner/{roomName}/{username}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String isOwner(@PathParam("roomName") String roomName,@PathParam("username") String username){
		if (StringUtils.isEmpty(roomName)){
            return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
        }
        if (StringUtils.isEmpty(username)){
            return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(),ErrEnum.ERR_USERNAME_NULL.getValue());
        }
        return ResultUtils.success(plugin.isRoomOwner(roomName,username));
	}

	@GET
	@Path("/findUsers/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String getMUCRoomUsers(@PathParam("roomName") String roomName ,@Context HttpServletResponse response)
			throws ServiceException {
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		List<Map<String, Object>> list =  plugin.findRoomUsers(roomName);
		String pardon = plugin.findRoomPardonByRoomName(roomName);
		Map us = null;
		if(list!=null){
			for(Map<String, Object> map : list){
				String username = ((String)map.get("jid")).split("@")[0];
				if("888888".equals(username)){
					us = map;
				}
				Map param = userManager.findUserNickNameAndPic(username);
				String nickName = null;
				String pic = null;
				//如果是免死用户
				if(username.equals(pardon)){
					map.put("pardon", 1);
				}else{
					map.put("pardon", 0);
				}
				if(param!=null){
					if(param.containsKey("nickName")){
						nickName = (String)param.get("nickName");
						pic = (String)param.get("pic");
					}
				}

				map.put("nickName", nickName);
				map.put("pic", pic);
			}
			if(us!=null){
				list.remove(us);
			}
		}
		return ResultUtils.success(list);
	}
	@POST
	@Path("/changedRemark/{jid}/{name}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String remarkChanged(@PathParam("jid") String jid,@PathParam("name") String name,
			@QueryParam("remark") String remark)throws ServiceException {
		if(jid==null||"".equals(jid)){
			return ResultUtils.fail(ErrEnum.ERR_JID_NULL.getMsg(),ErrEnum.ERR_JID_NULL.getValue());
		}
		if(name==null||"".equals(name)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(remark==null||"".equals(remark)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMUSERREMARK_NULL.getMsg(),ErrEnum.ERR_ROOMUSERREMARK_NULL.getValue());
		}
		MUCMemberEntity member = plugin.findMember(jid, name);
		if(member!=null){
			boolean b = plugin.remarkChanged(name, jid,remark);
			if(b){
				return ResultUtils.success(null);
			}
		}
		boolean b = plugin.updateAffiliationRemark(jid, name, remark);
		if(b){
			return ResultUtils.success(null);
		}
		return ResultUtils.fail(ErrEnum.ERR_SERVER_ERR.getMsg(),ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@GET
	@Path("/findRooms/{jid}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findMUCRooms(@PathParam("jid") String jid){
		if(jid==null||"".equals(jid)){
			return ResultUtils.fail(ErrEnum.ERR_JID_NULL.getMsg(),ErrEnum.ERR_JID_NULL.getValue());
		}
		MUCRoomEntities mrs = plugin.findMUCRooms(jid);
		List<Map<String,String>> list =  plugin.findRoomRemarks(jid);
		plugin.updateAffiliatiomOfflineDateByJid(jid, null);
		plugin.updateMucmemberOfflineDateByJid(jid, null);
		JsonConfig jc = new JsonConfig();
		jc.setExcludes(new String[]{"adminGroups", "admins","broadcastPresenceRoles","canAnyoneDiscoverJID","canChangeNickname","canOccupantsChangeSubject","canOccupantsInvite","logEnabled","loginRestrictedToNickname","memberGroups","members","membersOnly","moderated","outcastGroups","outcasts","ownerGroups","owners","persistent","publicRoom","registrationEnabled","modificationDate"});
		JSONObject json = JSONObject.fromObject(mrs,jc);
		json.put("roomRemark", list);
		return ResultUtils.success(json);
	}
	@PUT
	@Path("/updateRoomMaxUsers/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String updateMaxUsers(@PathParam("roomName") String roomName,
			@QueryParam("maxUsers") String maxUsers, @DefaultValue("conference") @QueryParam("servicename") String serviceName) throws ServiceException{
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(maxUsers==null||"".equals(maxUsers)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMUSERMAX_NULL.getMsg(),ErrEnum.ERR_ROOMUSERMAX_NULL.getValue());
		}
		MUCRoomEntity room = plugin.getChatRoom(roomName, serviceName, false);
		room.setMaxUsers(Integer.parseInt(maxUsers));
		plugin.updateChatRoom(roomName, serviceName, room);
		return ResultUtils.success(null);
	}
	@GET
	@Path("/findRoomMark/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findRoomMark(@PathParam("roomName") String roomName) throws ServiceException{
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		Long roomMark = plugin.findMUCRoomMark(roomName);
		if(roomMark!=null){
			JSONObject json = new JSONObject();
			json.put("roomMark", roomMark);
			return ResultUtils.success(json);
		}
		return ResultUtils.fail(ErrEnum.ERR_SERVER_ERR.getMsg(),ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@DELETE
	@Path("/exitRoom/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String exitRoom(@PathParam("roomName") String roomName,@DefaultValue("conference") @QueryParam("servicename") String serviceName,
		@QueryParam("jid") String jid) throws ServiceException{
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(jid==null||"".equals(jid)){
			return ResultUtils.fail(ErrEnum.ERR_JID_NULL.getMsg(),ErrEnum.ERR_JID_NULL.getValue());
		}
		plugin.deleteAffiliation(serviceName, roomName, jid);
		MUCRoomManager.getInstance().updateRoomPic(roomName);
		return ResultUtils.success(null);
	}
	@DELETE
	@Path("/outRoom/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String outRoom(@PathParam("roomName") String roomName,@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@QueryParam("members") String members,@QueryParam("admins") String admins) throws ServiceException{
		int i = plugin.findAffiliation(roomName, admins);
		if(i!=10&&i!=20){
			return ResultUtils.fail(ErrEnum.ERR_NOT_ROLE.getMsg(), ErrEnum.ERR_NOT_ROLE.getValue());
		}
		String[] jids = members.split(",");
		for(int x = 0; x<jids.length;x++){
			int y = plugin.findAffiliation(roomName, jids[x]);
			if((i==10&&y==10)||(i==20&&y==20)||(i==20&&y==10)){
				return ResultUtils.fail(ErrEnum.ERR_NOT_ROLE.getMsg(), ErrEnum.ERR_NOT_ROLE.getValue());
			}
		}
		for(int x = 0; x<jids.length;x++){
			plugin.deleteAffiliation(serviceName, roomName, jids[x].trim());
		}
		MUCRoomManager.getInstance().updateRoomPic(roomName);
		return ResultUtils.success(null);
	}
	@GET
	@Path("/findRoomDetail/{roomName}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findRoomDetail(@PathParam("roomName") String roomName) throws ServiceException{
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		MUCRoomEntity mr = plugin.findRoomDetail(roomName);
		JsonConfig jc = new JsonConfig();
		jc.setExcludes(new String[]{"adminGroups", "admins","broadcastPresenceRoles","canAnyoneDiscoverJID","canChangeNickname","canOccupantsChangeSubject","canOccupantsInvite","logEnabled","loginRestrictedToNickname","memberGroups","members","membersOnly","moderated","outcastGroups","outcasts","ownerGroups","owners","persistent","publicRoom","registrationEnabled","modificationDate"});
		JSONObject json = JSONObject.fromObject(mr,jc);
		return ResultUtils.success(json);
	}
	@GET
	@Path("/findRoomDetail/{roomName}/{jid}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findRoomDetail(@PathParam("roomName") String roomName,@PathParam("jid") String jid) throws ServiceException{
		if(roomName==null||"".equals(roomName)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(),ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		int affi = plugin.findAffiliation(roomName, jid);
		MUCRoomEntity mr = plugin.findRoomDetail(roomName);
		JsonConfig jc = new JsonConfig();
		jc.setExcludes(new String[]{"adminGroups", "admins","broadcastPresenceRoles","canAnyoneDiscoverJID","canChangeNickname","canOccupantsChangeSubject","canOccupantsInvite","logEnabled","loginRestrictedToNickname","memberGroups","members","membersOnly","moderated","outcastGroups","outcasts","ownerGroups","owners","persistent","publicRoom","registrationEnabled","modificationDate"});
		JSONObject json = JSONObject.fromObject(mr,jc);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("room", json);
		map.put("affiliation", affi);
		return ResultUtils.success(map);
	}
	@GET
	@Path("/findRoomUserTotle/{roomNo}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findRoomUserTotle(@PathParam("roomNo") Long roomNo) throws ServiceException{
		if(roomNo==null||"".equals(roomNo)){
			return ResultUtils.fail(ErrEnum.ERR_ROOMNUMBER_NULL.getMsg(), ErrEnum.ERR_ROOMNUMBER_NULL.getValue());
		}
		int totle = plugin.findRoomUserTotle(roomNo);
		JSONObject json = new JSONObject();
		json.put("roomUserTotle", totle);
		return ResultUtils.success(json);
	}
	@POST
	@Path("/{roomName}/authorization")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String authorization(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@FormParam("owners") String owners,@FormParam("members") String members,
			@FormParam("key") String key,@PathParam("roomName") String roomName) throws ServiceException{
		if(members==null||"".equals(members)){
			return ResultUtils.fail(ErrEnum.ERR_MEMBERS_NULL.getMsg(), ErrEnum.ERR_MEMBERS_NULL.getValue());
		}
		if(key==null||"".equals(key)){
			return ResultUtils.fail(ErrEnum.ERR_KEY_NULL.getMsg(), ErrEnum.ERR_KEY_NULL.getValue());
		}
		if(owners==null&&"".equals(owners)){
			return ResultUtils.fail(ErrEnum.ERR_OWNERS_NULL.getMsg(), ErrEnum.ERR_OWNERS_NULL.value);
		}
		if(key.equals("0")){
			plugin.addMember(serviceName, roomName, members);
			return ResultUtils.success(null);
		}
		if(key.equals("10")){
			plugin.addOwner(serviceName, roomName, members);
			plugin.addMember(serviceName, roomName, owners);
			return ResultUtils.success(null);
		}
		if(key.equals("20")){
			plugin.deleteAffiliation(serviceName, roomName, members);
			plugin.addAdmin(serviceName, roomName, members);
			return ResultUtils.success(null);
		}
		MUCRoomManager.getInstance().updateRoomPic(roomName);
		return ResultUtils.fail(ErrEnum.ERR_SERVER_ERR.getMsg(), ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@POST
	@Path("/createRedPackRoom")
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String  createRedPackRoom(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@FormParam("username") String username,@FormParam("naturalName") String naturalName,@FormParam("pardon") String pardon,
			@FormParam("type") String type,@FormParam("commission") BigDecimal commission,
			@FormParam("parentReferralCode") String parentReferralCode,@FormParam("redPackTotle") String redPackTotle,
			@FormParam("pardonBack") BigDecimal pardonBack,@FormParam("selfManagement")Integer selfManagement) throws ServiceException{
		if(!userManager.isExists(username)){
			return ResultUtils.fail("群主不能为空或者不存在", ErrEnum.ERR_USER_NULL.getValue());
		}
		String domain  = JiveGlobals.getProperty("xmpp.domain");
		String jid = username+"@"+domain;
		String roomName = username+"_"+new Date().getTime();
		Long roomMark = plugin.findMUCRoomMark(roomName);
		if(roomMark!=null){
			return ResultUtils.fail("该群已存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if("1".equals(type)){
			if(!userManager.isExists(pardon)){
				return ResultUtils.fail("免死用户不能为空或者不存在", ErrEnum.ERR_USER_NULL.getValue());
			}
		}
		if("1".equals(type)){
			if(username.equals(pardon)){
				return ResultUtils.fail("群主不能设为免死用户", ErrEnum.ERR_USER_NULL.getValue());
			}
		}
		if(new BigDecimal(1).compareTo(commission)<0){
			return ResultUtils.fail("提成不能大于1", ErrEnum.ERR_NUMBER_ERR.getMsg());
		}
		List<String> owners = new ArrayList<String>();
		owners.add(jid);
		MUCRoomEntity room = new MUCRoomEntity();
		room.setNaturalName(naturalName);
		room.setRoomName(roomName);
		room.setMaxUsers(500);
		room.setPersistent(true);
		room.setOwners(owners);
		room.setPublicRoom(true);
		room.setCanOccupantsInvite(true);
		room.setCanAnyoneDiscoverJID(true);
		room.setLogEnabled(true);
		room.setCanChangeNickname(true);
		room.setRegistrationEnabled(true);
		room.setCanOccupantsChangeSubject(true);
		plugin.createChatRoom(serviceName, room);
		if("1".equals(type)){
			plugin.addMember(serviceName, roomName, pardon+"@"+domain);
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", roomName);
		map.put("type", type);
		map.put("pardon", pardon);
		map.put("commission", commission);
		map.put("parentReferralCode", parentReferralCode);
		map.put("redPackTotle", redPackTotle);
		map.put("pardonBack", pardonBack);
		map.put("selfManagement", selfManagement);
		boolean b = plugin.updateRoomByName(map);
		if(b){
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("服务器错误请重试", ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@POST
	@Path("/updateRedPackRoom")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String  updateRedPackRoom(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@FormParam("name") String name,@FormParam("naturalName") String naturalName,
			@FormParam("description") String description,@FormParam("maxUsers") Integer maxUsers,
			@FormParam("redPackTotle") String redPackTotle,
			@FormParam("redPackAmount") String redPackAmount,@FormParam("multiple") String multiple,
			@FormParam("pardon") String pardon,@FormParam("commission") BigDecimal commission,@FormParam("type") String type,
			@FormParam("parentReferralCode") String parentReferralCode,
			@FormParam("pardonBack") BigDecimal pardonBack,@FormParam("selfManagement")Integer selfManagement,
			@FormParam("state")Integer state,@FormParam("img")String img
			) throws ServiceException{
		String domain  = JiveGlobals.getProperty("xmpp.domain");
		if(name==null||name.isEmpty()){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_SERVER_ERR.getValue());
		}
		if("1".equals(type)){
			if(!userManager.isExists(pardon)){
				return ResultUtils.fail("免死用户不能为空或者不存在", ErrEnum.ERR_USER_NULL.getValue());
			}
			String pardonJid = pardon+"@"+domain;
			int x = plugin.findAffiliation(name, pardonJid);
			if(x>0){
				return ResultUtils.fail("群主或者管理员不能设为免死", ErrEnum.ERR_SERVER_ERR.getValue());
			}
			MUCMemberEntity member = plugin.findMember(pardonJid, name);
			if(member==null){
				return ResultUtils.fail("不是本群成员不能设为免死", ErrEnum.ERR_SERVER_ERR.getValue());
			}
		}
		MUCRoom room =  XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName).getChatRoom(name);
		if(naturalName!=null&&!"".equals(naturalName)){
			room.setNaturalLanguageName(naturalName);
		}
		if(maxUsers!=null){
			room.setMaxUsers(maxUsers);
		}
		if (ClusterManager.isClusteringStarted()) {
			  CacheFactory.doClusterTask(new RoomUpdatedEvent((LocalMUCRoom) room));
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", name);
		map.put("naturalName", naturalName);
		map.put("maxUsers", maxUsers);
		map.put("redPackTotle", redPackTotle);
		map.put("redPackAmount", redPackAmount);
		map.put("multiple", multiple);
		map.put("pardon", pardon);
		map.put("commission", commission);
		map.put("parentReferralCode", parentReferralCode);
		map.put("redPackTotle", redPackTotle);
		map.put("pardonBack", pardonBack);
		map.put("selfManagement", selfManagement);
		map.put("state", state);
		map.put("img", img);
		boolean b = plugin.updateRoomByName(map);
		if(b){
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("服务器错误请重试", ErrEnum.ERR_SERVER_ERR.getValue());
	}
	@GET
	@Path("/findGameProperty")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String findGameProperty(@QueryParam("roomId") Long roomId){
		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		List<GameProperty> list = plugin.findGameProperty(roomId);
		if(list!=null){
			return ResultUtils.success(list);
		}
		return ResultUtils.fail("群不存在或未设置规则", ErrEnum.ERR_GAMEPROPERTY_NULL.getValue());
	}
	@POST
	@Path("/updateGameProperty")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateGameProperty(@FormParam("roomId") Long roomId,
			@FormParam("mineban") String mineban,@FormParam("settlementNumber") String settlementNumber,
			@FormParam("rush") String rush,@FormParam("odds") String odds,
			@FormParam("section") String section,@FormParam("redPackAmount")String redPackAmount){
		if(roomId==null){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if(settlementNumber==null||settlementNumber.isEmpty()){
			return ResultUtils.fail("结算号不能为空", ErrEnum.ERR_SETTLENUMBER_NULL.getValue());
		}
		if(rush==null||rush.isEmpty()){
			return ResultUtils.fail("秒抢号不能为空", ErrEnum.ERR_RUSH_NULL.getValue());
		}
		if(odds==null||odds.isEmpty()){
			return ResultUtils.fail("中雷规则不能为空", ErrEnum.ERR_ODDS_NULL.getValue());
		}
		if(redPackAmount==null||redPackAmount.isEmpty()){
			return ResultUtils.fail("红包金额区间不能为空", ErrEnum.ERR_MONEY_NULL.getValue());
		}
		if(plugin.isExistsGameProperty(roomId, "mineban")){
			plugin.updateGamePropertyById(roomId, mineban, "mineban");
		}else{
			plugin.insertGameProperties(roomId, "mineban", mineban);
		}
		if(plugin.isExistsGameProperty(roomId, "settlementNumber")){
			plugin.updateGamePropertyById(roomId, settlementNumber, "settlementNumber");
		}else{
			plugin.insertGameProperties(roomId, "settlementNumber", settlementNumber);
		}
		if(plugin.isExistsGameProperty(roomId, "rush")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(rush).toJSONString(), "rush");
		}else{
			plugin.insertGameProperties(roomId, "rush", rush);
		}
		if(plugin.isExistsGameProperty(roomId, "odds")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseObject(odds).toJSONString(), "odds");
		}else{
			plugin.insertGameProperties(roomId, "odds", com.alibaba.fastjson.JSONObject.parseObject(odds).toJSONString());
		}
		if(plugin.isExistsGameProperty(roomId, "section")){
			plugin.updateGamePropertyById(roomId, section, "section");
		}else{
			plugin.insertGameProperties(roomId, "section", section);
		}
		if(redPackAmount!=null&&!redPackAmount.isEmpty()){
			plugin.updateRoomPropertyById(roomId,redPackAmount);
		}
		return ResultUtils.success("成功");
	}
	//开启或者关闭群验证   游戏群除外
	@POST
	@Path("/updateRoomIsValidate")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateRoomIsValidate(@FormParam("roomName") String roomName,
			@FormParam("isValidate") Integer isValidate){
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群名称为空", ErrEnum.ERR_ROOMNAME_NULL.getMsg());
		}
		if(isValidate==null){
			return ResultUtils.fail("isValidate为空", ErrEnum.ERR_ISVALIDATE_NULL.getMsg());
		}
		MUCRoomEntity room = plugin.findRoomDetail(roomName);
		if(room==null||!"0".equals(room.getType())){
			return ResultUtils.fail("群不存在或者为游戏群", ErrEnum.ERR_ROOM_ERR.getValue());
		}
		if(isValidate==0){
			plugin.updateRoomIsValidate(0, roomName);
			return ResultUtils.success("成功");
		}
		if(isValidate==1){
			plugin.updateRoomIsValidate(1, roomName);
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("状态码错误", ErrEnum.ERR_STATUS_ERR.getValue());
	}
	//加群审核
	@POST
	@Path("/addRoomValidate")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String addMucRoomValidate(@FormParam("roomName") String roomName,
			@FormParam("username") String username,@FormParam("status") Integer status,
			@FormParam("beInviteds") String beInviteds,@FormParam("id") Integer id){
		if(username==null||username.isEmpty()){
			return ResultUtils.fail("用户名为空", ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群名称为空", ErrEnum.ERR_ROOMNAME_NULL.getMsg());
		}
		if(status==null){
			return ResultUtils.fail("状态码为空", ErrEnum.ERR_STATUS_ERR.getMsg());
		}
		if(beInviteds==null||beInviteds.isEmpty()){
			return ResultUtils.fail("被邀请人不能为空", ErrEnum .ERR_BEINVITED_NULL.getValue());
		}
		if(status==0){
			String[] users = beInviteds.split(",");
			if(users==null||users.length==0){
				return ResultUtils.fail("被邀请人不能为空", ErrEnum.ERR_BEINVITED_NULL.getValue());
			}
			for(String user:users){
				if(!plugin.isExsitsRoomValidate(username, roomName, user)){
					plugin.insertRoomValidate(username, roomName, user);
				}
			}
			return ResultUtils.success("成功");
		}
		if(id==null){
			return ResultUtils.fail("群验证ID为空", ErrEnum.ERR_ROOMVALIDATEID_NULL.getValue());
		}
		if(status==1){
			plugin.deleteRoomValidateById(id);
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("状态码错误", ErrEnum.ERR_STATUS_ERR.getValue());
	}
	@POST
	@Path("findRoomValidateList")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String findRoomValidateList(@FormParam("roomName")String roomName,
			@FormParam("pageNum")Integer pageNum,
			@FormParam("pageSize")Integer pageSize){
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群名称为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		Integer num = null;
		Integer size = null;
		if(pageNum == null){
			num = 1;
		}else{
			num = pageNum;
		}
		if(pageSize == null){
			size = 15;
		}else{
			size = pageSize;
		}

		Integer start = (num-1)*size;
		List<Map<String,Object>> list = plugin.findRoomValidateList(roomName,start,size);
		if(list==null){
			return ResultUtils.fail("群不存在或者无人加群", ErrEnum.ERR_ROOMVALIDATEID_ERR.getValue());
		}
		for(Map<String,Object> map : list){
			String username = (String)map.get("username");
			Map user = userManager.findUserNickNameAndPic(username);
			map.put("user", user);
			String beInvited = (String)map.get("beInvited");
			Map beInvitedUser = userManager.findUserNickNameAndPic(beInvited);
			map.put("beInvitedUser", beInvitedUser);
		}
		return ResultUtils.success(list);
	}
	@POST
	@Path("updateBullGameProperty")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateBullGameProperty(@FormParam("mineban")String mineban,
			@FormParam("odds") String odds, @FormParam("roomId") Long roomId){
		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if(odds==null||odds.isEmpty()){
			return ResultUtils.fail("中雷规则不能为空", ErrEnum.ERR_ODDS_NULL.getValue());
		}
		if(mineban==null||mineban.isEmpty()){
			return ResultUtils.fail("抽水比例不能为空", ErrEnum.ERR_MINEBAN_NULL.getValue());
		}
		if(!mineban.contains(".")){
			mineban = Double.parseDouble(mineban)/100+"";
		}
		double m = Double.parseDouble(mineban);
		if(m>0.05){
			return ResultUtils.fail("抽水比例不能大于百分之五", ErrEnum.ERR_MINEBAN_NULL.getValue());
		}
		if(plugin.isExistsGameProperty(roomId, "mineban")){
			plugin.updateGamePropertyById(roomId, mineban, "mineban");
		}else{
			plugin.insertGameProperties(roomId, "mineban", mineban);
		}
		if(plugin.isExistsGameProperty(roomId, "odds")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString(), "odds");
		}else{
			plugin.insertGameProperties(roomId, "odds", com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString());
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("updateCaiLeiGameProperty")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateCaiLeiGameProperty(@FormParam("reward")String reward,
			@FormParam("odds") String odds, @FormParam("roomId") Long roomId,
			@FormParam("settlementNumber") String settlementNumber,
			@FormParam("rush") String rush){

		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if(rush==null||rush.isEmpty()){
			return ResultUtils.fail("秒抢号不能为空", ErrEnum.ERR_RUSH_NULL.getValue());
		}
		if(odds==null||odds.isEmpty()){
			return ResultUtils.fail("赔率不能为空", ErrEnum.ERR_ODDS_NULL.getValue());
		}
		if(reward==null||reward.isEmpty()){
			return ResultUtils.fail("奖励规则不能为空", ErrEnum.ERR_REWARD_NULL.getValue());
		}
		if(plugin.isExistsGameProperty(roomId, "rush")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(rush).toJSONString(), "rush");
		}else{
			plugin.insertGameProperties(roomId, "rush", rush);
		}
		if(plugin.isExistsGameProperty(roomId, "reward")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(reward).toJSONString(), "reward");
		}else{
			plugin.insertGameProperties(roomId, "reward", com.alibaba.fastjson.JSONObject.parseArray(reward).toJSONString());
		}
		if(plugin.isExistsGameProperty(roomId, "odds")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString(), "odds");
		}else{
			plugin.insertGameProperties(roomId, "odds", com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString());
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("updateHnLeiProperty")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateHnLeiProperty(@FormParam("reward")String reward,
			@FormParam("odds") String odds, @FormParam("roomId") Long roomId){
		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if(odds==null||odds.isEmpty()){
			return ResultUtils.fail("赔率不能为空", ErrEnum.ERR_ODDS_NULL.getValue());
		}
		if(reward==null||reward.isEmpty()){
			return ResultUtils.fail("奖励规则不能为空", ErrEnum.ERR_REWARD_NULL.getValue());
		}
		if(plugin.isExistsGameProperty(roomId, "reward")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(reward).toJSONString(), "reward");
		}else{
			plugin.insertGameProperties(roomId, "reward", com.alibaba.fastjson.JSONObject.parseArray(reward).toJSONString());
		}
		if(plugin.isExistsGameProperty(roomId, "odds")){
			plugin.updateGamePropertyById(roomId, com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString(), "odds");
		}else{
			plugin.insertGameProperties(roomId, "odds", com.alibaba.fastjson.JSONObject.parseArray(odds).toJSONString());
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("/findGameRooms")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public String findGameRooms(@FormParam("pageNum")Integer pageNum,
			@FormParam("pageSize")Integer pageSize){
		if(pageNum==null){
			pageNum = 1;
		}
		if(pageSize==null){
			pageSize = 15;
		}
		Integer start = (pageNum-1)*pageSize;
		MUCRoomEntities mrs = plugin.findGameRooms(start,pageSize);
		JsonConfig jc = new JsonConfig();
		jc.setExcludes(new String[]{"adminGroups", "admins","broadcastPresenceRoles","canAnyoneDiscoverJID","canChangeNickname","canOccupantsChangeSubject","canOccupantsInvite","logEnabled","loginRestrictedToNickname","memberGroups","members","membersOnly","moderated","outcastGroups","outcasts","ownerGroups","owners","persistent","publicRoom","registrationEnabled","modificationDate"});
		JSONObject json = JSONObject.fromObject(mrs,jc);
		return ResultUtils.success(json);
	}
	@POST
	@Path("updateLuckyTurntable")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateLuckyTurntable(@FormParam("prize")String prize,
			@FormParam("roomId") Long roomId){
		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		if(prize==null||prize.isEmpty()){
			return ResultUtils.fail("轮盘规则为空", ErrEnum.ERR_REWARD_NULL.getValue());
		}
		JSONArray ja = null;
		try{
			ja = com.alibaba.fastjson.JSONObject.parseArray(prize);
		}catch(Exception e){
			e.printStackTrace();
			Log.error("updateLuckyTurntable: "+e.getMessage());
			return ResultUtils.fail("轮盘规则错误", ErrEnum.ERR_PRIZE_ERR.getValue());
		}
		if(plugin.isExistsGameProperty(roomId, "prize")){
			plugin.updateGamePropertyById(roomId, ja.toJSONString(), "prize");
		}else{
			plugin.insertGameProperties(roomId, "prize", ja.toJSONString());
		}
		return ResultUtils.success("成功");
	}
	@POST
	@Path("findLuckyTurntable")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String findLuckyTurntable(
			@FormParam("roomId") Long roomId){
		if(roomId==null){
			return ResultUtils.fail("群ID为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(!plugin.isExistsRoomById(roomId)){
			return ResultUtils.fail("群不存在", ErrEnum.ERR_ROOM_EXISTS.getValue());
		}
		String prize = plugin.findLuckyTurntable(roomId);
		return ResultUtils.success(prize);
	}
	@POST
	@Path("findRoomsByType")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String findRoomsByType(@FormParam("type")String type,@FormParam("pageSize") Integer pageSize,
			@FormParam("pageNum")Integer pageNum){
		if(type==null){
			return ResultUtils.fail("类型为空", ErrEnum.ERR_TYPE_NULL.getValue());
		}
		if(pageSize==null){
			return ResultUtils.fail("页面大小为空", ErrEnum.ERR_PAGESIZE_NULL.getValue());
		}
		if(pageNum==null){
			return ResultUtils.fail("当前页码为空", ErrEnum.ERR_PAGENUM_NULL.getValue());
		}
		int start = (pageNum-1)*pageSize;
		MUCRoomEntities rooms = plugin.findRoomsByType(type, 1,start,pageSize);
		JsonConfig jc = new JsonConfig();
		jc.setExcludes(new String[]{"adminGroups", "admins","broadcastPresenceRoles","canAnyoneDiscoverJID","canChangeNickname","canOccupantsChangeSubject","canOccupantsInvite","logEnabled","loginRestrictedToNickname","memberGroups","members","membersOnly","moderated","outcastGroups","outcasts","ownerGroups","owners","persistent","publicRoom","registrationEnabled","modificationDate"});
		JSONObject json = JSONObject.fromObject(rooms,jc);
		return ResultUtils.success(json);
	}
	@POST
	@Path("updateRoomGag")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String updateRoomGag(@FormParam("isGag")Integer isGag,@FormParam("roomName") String roomName){
		if(isGag==null){
			return ResultUtils.fail("禁言状态为空", ErrEnum.ERR_ISGAG_NULL.getValue());
		}
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		if(isGag!=0&&isGag!=1){
			return ResultUtils.fail("设置失败,请重试", ErrEnum.ERR_ISGAG_ERR.getValue());
		}
		boolean result = plugin.updateRoomIsGag(isGag, roomName);
		if(result){
			return ResultUtils.success("成功");
		}
		return ResultUtils.fail("设置失败,请重试", ErrEnum.ERR_ISGAG_ERR.getValue());
	}
	@POST
	@Path("findRoomGag")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON_CHARSET_UTF8})
	public String selectRoomGag(@FormParam("roomName") String roomName){
		if(roomName==null||roomName.isEmpty()){
			return ResultUtils.fail("群标识为空", ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		Integer isGag = plugin.selectRoomIsGag(roomName);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("isGag", isGag);
		return ResultUtils.success(map);
	}


}




    




