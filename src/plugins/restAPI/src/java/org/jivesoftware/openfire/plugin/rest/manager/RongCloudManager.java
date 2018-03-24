package org.jivesoftware.openfire.plugin.rest.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.plugin.rest.entity.RongTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RongCloudManager {
	private static final Logger Log = LoggerFactory.getLogger(UserManager.class);
	private static final RongCloudManager rong_manager = new RongCloudManager();
	public static RongCloudManager getInstance() {
        return rong_manager;
    }
    private RongCloudManager() {
    }
    private String INSERT_RONGTOKEN = "INSERT INTO ofrongtoken(`username`,`token`) values(?,?)";
    public boolean saveRongToken(String username,String token){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try {
			con = DbConnectionManager.getConnection();
			pre = con.prepareStatement(INSERT_RONGTOKEN);
			pre.setString(1, username);
			pre.setString(2, token);
			return pre.executeUpdate()>0;
		} catch (Exception e) {
			Log.error("saveRongToken err: "+e.getMessage());
			return false;
		}finally{
			DbConnectionManager.closeConnection(pre, con);
		}
    }
    
    private String SELECT_RONGTOKEN = "SELECT username,token FROM ofrongtoken WHERE username = ?";
    public RongTokenEntity findRongTokenByUsername(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try {
			con = DbConnectionManager.getConnection();
			pre = con.prepareStatement(SELECT_RONGTOKEN);
			pre.setString(1, username);
			rs = pre.executeQuery();
			RongTokenEntity rt = null;
			while(rs.next()){
				rt = new RongTokenEntity();
				rt.setUsername(rs.getString("username"));
				rt.setToken(rs.getString("token"));
			}
			return rt;
		} catch (Exception e) {
			Log.error("findRongTokenByUsername err: "+e.getMessage());
			return null;
		}finally{
			DbConnectionManager.closeConnection(pre, con);
		}
    }
    
    private String UPDATE_RONGTOKEN = "UPDATE ofrongtoken SET token = ? ,modifyDate = ? WHERE username = ?";
    public boolean updateRongToken(String username,String token){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try {
			con = DbConnectionManager.getConnection();
			pre = con.prepareStatement(UPDATE_RONGTOKEN);
			pre.setString(1, token);
			pre.setTimestamp(2, new java.sql.Timestamp(new Date().getTime()));
			pre.setString(3, username);
			return pre.executeUpdate()>0;
		} catch (Exception e) {
			Log.error("updateRongToken err: "+e.getMessage());
			return false;
		}finally{
			DbConnectionManager.closeConnection(pre, con);
		}
    }
    
    
    
}
