package org.jivesoftware.openfire.plugin.rest.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.utils.ImageUtil;
import org.jivesoftware.openfire.plugin.rest.utils.ImgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;


public class MUCRoomManager {
	private static final Logger Log = LoggerFactory.getLogger(MUCRoomManager.class);
	private static final MUCRoomManager roomManager = new MUCRoomManager();
	public static MUCRoomManager getInstance() {
        return roomManager;
    }
    private MUCRoomManager() {
    }
    //更新普通群为九宫格群头像
	public void updateRoomPic(String roomName){
		try {
			MUCRoomEntity room = MUCRoomController.getInstance().findRoomDetail(roomName);
			//群不存在  或者不是普通群（普通群类型是0）的不更新头像
			if(room==null||!"0".equals(room.getType())){
				return;
			}
			List<Map> roomUsers = MUCRoomController.getInstance().findRoomUsers(roomName);
			if(roomUsers!=null){
				List<String> pics = new ArrayList<String>();
				int x = 0;
				for(Map m : roomUsers){
					if(m!=null){
						String jid = (String)m.get("jid");
						String username = null;
						if(jid!=null){
							username = new JID(jid).getNode();
						}
						Map map = UserManager.getInstance().findUserNickNameAndPic(username);
						String pic = (String)map.get("pic");
						pics.add(pic);
						x++;
						if(x==9){
							break;
						}
					}
				}
				String pic = ImageUtil.getCombinationOfhead(pics);
				if(pic!=null){
					MUCRoomController.getInstance().updateRoomPic(roomName, pic);
				}
			}
		} catch (Exception e) {
			Log.error("MUCRoomManager updateRoomPic err:"+e.getMessage());
		}
	}
}
