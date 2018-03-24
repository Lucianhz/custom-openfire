package com.doowal.offlinepush.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doowal.offlinepush.entity.MsgPush;
import com.doowal.offlinepush.entity.MsgPushToken;

public class MsgPushManager {
	private static final Logger Log = LoggerFactory.getLogger(MsgPushManager.class);
	private static final MsgPushManager MSG_PUSH_MANAGER = new MsgPushManager();

	public static MsgPushManager getInstance() {
		return MSG_PUSH_MANAGER;
	}

	private MsgPushManager() {
	}

	private String INSERT_MSGPUSH = "INSERT INTO ofmsgpush(jid,jidFrom,roomFrom,msgPush,createDate,modifyDate) values(?,?,?,?,?,?);";

	/*
	 * public List findMsgPush(String jid,String jidFrom, String roomFrom){
	 * Connection con = null; PreparedStatement pst = null; try{
	 * 
	 * }catch(SQLException e){ Log.error("msgpush save exception: {}", e);
	 * return false; }finally{ DbConnectionManager.closeConnection(pst, con); }
	 * } }
	 */
	public boolean save(MsgPush m) {
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = DbConnectionManager.getConnection();
			pst = con.prepareStatement(INSERT_MSGPUSH);
			int i = 1;
			pst.setString(i++, m.getJid());
			pst.setString(i++, m.getJidfrom());
			pst.setString(i++, m.getRoomfrom());
			pst.setString(i++, m.getMsgpush());
			pst.setString(i++, m.getCreatedate());
			pst.setString(i++, m.getModifydate());
			return pst.execute();
		} catch (SQLException e) {
			Log.error("msgpush save exception: {}", e);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pst, con);
		}
	}

	public MsgPush findMsgPush(String jid, String jidFrom, String roomFrom) {
		Connection con = null;
		Statement state = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM ofmsgpush WHERE jid = '" + jid + "'";
		if (jidFrom != null && !jidFrom.isEmpty()) {
			sql += "and jidFrom = '" + jidFrom + "'";
		}
		if (roomFrom != null && !roomFrom.isEmpty()) {
			sql += "and roomFrom = '" + roomFrom + "'";
		}
		try {
			con = DbConnectionManager.getConnection();
			state = con.createStatement();
			rs = state.executeQuery(sql);
			MsgPush m = null;
			while (rs.next()) {
				m = new MsgPush();
				int i = 2;
				m.setJid(rs.getString(i++));
				m.setJidfrom(rs.getString(i++));
				m.setRoomfrom(rs.getString(i++));
				m.setMsgpush(rs.getString(i++));
				m.setCreatedate(rs.getString(i++));
				m.setModifydate(rs.getString(i++));
			}
			return m;
		} catch (SQLException e) {
			Log.error("msgpush find exception: {}", e);
			return null;
		} finally {
			DbConnectionManager.closeConnection(rs,state, con);
		}
	}

	public MsgPushToken selectPushToken(String username) {
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			pst = con.prepareStatement("SELECT * FROM ofmsgpushtoken WHERE username = ?");
			pst.setString(1, username);
			rs = pst.executeQuery();
			MsgPushToken mpt = null;
			if (rs.next()) {
				mpt = new MsgPushToken();
				mpt.setId(Long.valueOf(rs.getLong("id")));
				mpt.setUsername(rs.getString("username"));
				mpt.setToken(rs.getString("token"));
				return mpt;
			}
			return mpt;
		} catch (SQLException e) {
			Log.error("msgpush save exception: {}", e);
			return null;
		} finally {
			DbConnectionManager.closeConnection(rs,pst, con);
		}
	}

	public boolean updateMsgPush(MsgPush m) {
		Connection con = null;
		Statement state = null;
		String sql = "update ofmsgpush SET ";
		if (m.getJidfrom() != null && !m.getJidfrom().isEmpty()) {
			sql += " jidFrom = '" + m.getJidfrom() + "' ,";
		}
		if (m.getRoomfrom() != null && !m.getRoomfrom().isEmpty()) {
			sql += " roomFrom = '" + m.getRoomfrom() + "' ,";
		}
		if (m.getMsgpush() != null && !m.getMsgpush().isEmpty()) {
			sql += " msgPush = '" + m.getMsgpush() + "' ,";
		}
		sql += " modifyDate = '" + m.getModifydate() + "' where jid = '" + m.getJid() + "'";
		if (m.getJidfrom() != null && !m.getJidfrom().isEmpty()) {
			sql += " and jidFrom = '" + m.getJidfrom() + "'";
		}
		if (m.getRoomfrom() != null && !m.getRoomfrom().isEmpty()) {
			sql += " and roomFrom = '" + m.getRoomfrom() + "'";
		}
		try {
			con = DbConnectionManager.getConnection();
			state = con.createStatement();
			return state.executeUpdate(sql) > 0;
		} catch (SQLException e) {
			Log.error("msgpush update exception: {}", e);
			return false;
		} finally {
			DbConnectionManager.closeConnection(state, con);
		}
	}

	public boolean updatePushToken(String username, String token) {
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = DbConnectionManager.getConnection();
			pst = con.prepareStatement("UPDATE ofmsgpushtoken SET token = ? WHERE username = ?");
			pst.setString(1, token);
			pst.setString(2, username);
			return pst.executeUpdate() > 0;
		} catch (SQLException e) {
			Log.error("msgpush save exception: {}", e);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pst, con);
		}
	}

	public boolean savePushToken(String username, String token) {
		Connection con = null;
		PreparedStatement pst = null;
		try {
			con = DbConnectionManager.getConnection();
			pst = con.prepareStatement("INSERT INTO ofmsgpushtoken(username,token) values(?,?);");
			pst.setString(1, username);
			pst.setString(2, token);
			return pst.execute();
		} catch (SQLException e) {
			Log.error("msgpush save exception: {}", e);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pst, con);
		}
	}
}
