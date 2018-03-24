package com.doowal.im.plugin.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doowal.im.plugin.entity.ChatRoomLogs;


public class ChatRoomLogsManager {
	private static final Logger Log = LoggerFactory.getLogger(ChatRoomLogsManager.class);
	private static final ChatRoomLogsManager CHAT_ROOM_LOGS_MANAGER = new ChatRoomLogsManager();
	public static ChatRoomLogsManager getInstance() {
        return CHAT_ROOM_LOGS_MANAGER;
    }
    private ChatRoomLogsManager() {
    }
    
    private static String CHATROOMLOGS_INSERT = "INSERT INTO ofchatroomlogs(messageId,sender,receiver,content,detail,createDate) VALUES(?,?,?,?,?,?);";
    private static String CHATROOMLOGS_SELECT = "SELECT * FROM ofchatroomlogs WHERE messageId = ? ";
    public boolean save(ChatRoomLogs c){
    	Connection con = null;
    	PreparedStatement pst = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(CHATROOMLOGS_INSERT);
    		int i = 1;
    		pst.setString(i++,c.getMessageId());
    		pst.setString(i++, c.getSender());
    		pst.setString(i++, c.getReceiver());
    		pst.setString(i++, c.getContent());
    		pst.setString(i++, c.getDetail());
    		pst.setString(i++, c.getCreateDate());
    		return pst.execute();
    	}catch(SQLException e){
    		Log.error("recharge save exception: {}", e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pst, con);
    	}
    }
    public ChatRoomLogs findChatRoomLogsByMessageId(String messageId){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(CHATROOMLOGS_SELECT);
    		pst.setString(1,messageId);
    		rs = pst.executeQuery();
    		ChatRoomLogs crl = null;
    		while(rs.next()){
    			crl = new ChatRoomLogs();
    			crl.setId(rs.getLong(1));
    			crl.setMessageId(rs.getString(2));
    			crl.setSender(rs.getString(3));
    			crl.setReceiver(rs.getString(4));
    			crl.setContent(rs.getString(5));
    			crl.setDetail(rs.getString(6));
    			crl.setCreateDate(rs.getString(7));
    		}
    		return crl;
    	}catch(SQLException e){
    		Log.error("chatroomlogs select exception: {}", e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(pst, con);
    	}
    }
}