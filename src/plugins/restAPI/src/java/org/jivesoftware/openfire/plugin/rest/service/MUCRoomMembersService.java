package org.jivesoftware.openfire.plugin.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.manager.MUCRoomManager;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.plugin.rest.utils.RandomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Path("restapi/v1/chatrooms/{roomName}/members")
public class MUCRoomMembersService {

	@POST
	@Path("/{jid}")
	public Response addMUCRoomMember(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("jid") String jid, @PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().addMember(serviceName, roomName, jid);
		return Response.status(Status.CREATED).build();
	}

	@POST
	@Path("/group/{groupname}")
	public Response addMUCRoomMemberGroup(@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("groupname") String groupname, @PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().addMember(serviceName, roomName, groupname);
		return Response.status(Status.CREATED).build();
	}

	@DELETE
	@Path("/{jid}")
	public Response deleteMUCRoomMember(@PathParam("jid") String jid,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
		return Response.status(Status.OK).build();
	}

	@DELETE
	@Path("/group/{groupname}")
	public Response deleteMUCRoomMemberGroup(@PathParam("groupname") String groupname,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("roomName") String roomName) throws ServiceException {
		MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
		return Response.status(Status.OK).build();
	}

	// 添加群成员
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String addMUCRoomMembers(@FormParam("jids") String jids,
			@DefaultValue("conference") @QueryParam("servicename") String serviceName,
			@PathParam("roomName") String roomName) throws ServiceException {
		if (jids == null || "".equals(jids)) {
			return ResultUtils.fail(ErrEnum.ERR_JID_NULL.getMsg(), ErrEnum.ERR_JID_NULL.getValue());
		}
		if (roomName == null || "".equals(roomName)) {
			return ResultUtils.fail(ErrEnum.ERR_ROOMNAME_NULL.getMsg(), ErrEnum.ERR_ROOMNAME_NULL.getValue());
		}
		JSONObject json = JSONObject.fromObject(jids);
		JSONArray ja = json.getJSONArray("jids");
		UserManager userManager = UserManager.getInstance();
		MUCRoomController roomController = MUCRoomController.getInstance();
		MUCRoomEntity roomDetail = roomController.findRoomDetail(roomName);
		int totle = MUCRoomController.getInstance().findRoomUserTotle(roomDetail.getRoomMark());
		//普通群上线为500
		if("0".equals(roomDetail.getType())){
			if(totle+ja.size()>500){
				return ResultUtils.fail("超出当前群成员上限", ErrEnum.ERR_ROOMMAX_ERR.getValue());
			}
		}else{
			//游戏群上限为1000
			if(totle+ja.size()>1000){
				return ResultUtils.fail("超出当前群成员上限", ErrEnum.ERR_ROOMMAX_ERR.getValue());
			}
		}
		String referralCode = null;
		// 判断如果是游戏群 获取群主的推荐码
		if (!"0".equals(roomDetail.getType())) {
			MUCRoomEntity room = roomController.getChatRoom(roomName, serviceName, true);
			List<String> owns = room.getOwners();
			if (owns != null) {
				String own = owns.get(0);
				String username = own.split("@")[0];
				referralCode = userManager.findUserReferralCode(username);
			}
		}

		
		List<String> list = new ArrayList<String>();
		for (int x = 0; x < ja.size(); x++) {
			JSONObject jid = JSONObject.fromObject(ja.get(x));
			String j = jid.getString("jid");
			String jname = j.split("@")[0];
			if(!userManager.isExists(jname)){
				return ResultUtils.fail("该用户不存在:"+jname, ErrEnum.ERR_USER_NOEXISTS.getValue());
			}
			// 判断如果是游戏群 更新群成员的推荐码
			if (!"0".equals(roomDetail.getType())) {
				String code = userManager.findUserReferralCode(jname);
				if (code == null || code.isEmpty()) {
					if (referralCode != null && !referralCode.isEmpty()) {
						String codeSon = null;
						do {
							codeSon = RandomUtils.randomByReferralCode();
						} while (userManager.findUserExistsByReferralCode(codeSon));
						userManager.updateUserReferralCode(codeSon, referralCode, jname);
					}
				}
			}
			list.add(j);
		}
		roomController.addMembers(serviceName, roomName, list);
		MUCRoomManager.getInstance().updateRoomPic(roomName);
		return ResultUtils.success(null);
	}
}
