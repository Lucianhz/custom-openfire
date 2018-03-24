package org.jivesoftware.openfire.plugin.rest.manager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.plugin.rest.entity.GameProperty;
import org.jivesoftware.openfire.plugin.rest.enums.ErrEnum;
import org.jivesoftware.openfire.plugin.rest.utils.HttpsPost;
import org.jivesoftware.openfire.plugin.rest.utils.ResultUtils;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;

import net.sf.json.JSONObject;

public class UserManager {
	private static final Logger Log = LoggerFactory.getLogger(UserManager.class);
	private static final UserManager USER_MANAGER = new UserManager();
	public static UserManager getInstance() {
        return USER_MANAGER;
    }
    private UserManager() {
    }
    private   String UPDATE_USER_PAYPASSWORD = "UPDATE ofuser SET payPassword = ? WHERE username = ?;";
    private   String SELECT_USER_PATPASSWORD = "SELECT payPassword FROM ofuser WHERE username = ?;";
    private  String SELECT_USER_REALNAME = "SELECT realName FROM ofuser WHERE username = ?";
    private  String SELECT_USER_EXISTS = "SELECT creationDate FROM ofuser WHERE username = ?";
    private  String SELECT_USER_INFO = "SELECT realName,mobile,region,gender FROM ofuser WHERE username = ? ";
    private  String SELECT_COUNTRY = "SELECT * FROM ofcountry";
    private  String SELECT_PROVINCE = "SELECT * FROM ofprovince WHERE parentId = ? ";
    private  String SELECT_CITY = "SELECT * FROM ofcity WHERE parentId = ? and ancestorId = ?";
    private  String UPDATE_USER_OPENID_THIRDTYPE = "UPDATE ofuser SET openId = ? ,thirdType = ? WHERE username = ?;";
    private  String UPDATE_USER_MSG_DETAIL = "UPDATE ofuser SET pushDisplayMsgDetail = ? WHERE username = ?;";    
    private  String SELECT_USER_MSG_DETAIL = "SELECT pushDisplayMsgDetail FROM ofuser WHERE username = ?";
    private  String UPDATE_USER_DEVICECODE = "UPDATE ofuser SET deviceCode = ? WHERE username = ? ";
    private  String SELECT_USER_DEVICECODE = "SELECT  deviceCode FROM ofuser WHERE username = ? ";
    private  String SELECT_USER_NICKNAME = "SELECT nickName FROM ofuser WHERE username = ?";
    private  String SELECT_USER_NICKNAME_PIC = "SELECT nickName,pic FROM ofuser WHERE username = ?";
    private  String SELECT_USER_DETAIL = "SELECT u.username,u.accountBalance,u.gender,u.mobile,u.nickName,u.realName,u.region,u.referralCode,u.freezingAmount,u.creationDate,u.modificationDate FROM ofuser u WHERE username = ? ";
    private  String SELECT_USEREXISTS_BYCODE = "SELECT username FROM ofuser WHERE referralCode = ?";
    private  String SELECT_USEREXISTS_BYCODEV2 = "SELECT username FROM ofuser WHERE referralCode1 = ?";
    private  String UPDATE_USER_CODE = "UPDATE ofuser SET referralCode = ? , parentReferralCode = ? , modificationDate = ?  WHERE username = ?";
    private  String UPDATE_USER_CODEV2 = "UPDATE ofuser SET referralCode1 = ? , parentReferralCode1 = ? , modificationDate = ?  WHERE username = ?";
    private  String SELECT_USERINFO_BYMOBILE = "SELECT username,encryptedPassword,openId,qqId FROM ofuser WHERE mobile = ?";
    private  String SELECT_QQID_ISEXISTS = "SELECT username FROM ofuser WHERE qqId = ?";
    private  String SELECT_PASSWORD = "SELECT encryptedPassword FROM ofuser WHERE username = ?";
    private  String SELECT_INIT_BYUSERNAME = "SELECT init FROM ofuser WHERE username = ?";
    private  String UPDATE_USERINIT_BYUSERNAME = "UPDATE ofuser SET init = ? WHERE username = ? ";
    private  String SELECT_THIRDID_BYUSERNAME = "SELECT openId,qqId FROM ofuser WHERE username = ?";
    private  String SELECT_REFERRALCODE_BYUSERNAME = "SELECT referralCode FROM ofuser WHERE username = ?";
    private  String UPDATE_REFERRALCODE_BYUSERNAME = "UPDATE ofuser SET referralCode = ? , parentReferralCode = ? WHERE username = ?";
    
    public boolean updateReferralCode(String username,String referralCode,String parentReferralCode){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_REFERRALCODE_BYUSERNAME);
    		pre.setString(1, referralCode);
    		pre.setString(2, parentReferralCode);
    		pre.setString(3, username);
    		return pre.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user updateReferralCode exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pre, con);
    	}
    }
    
    public String findUserReferralCode(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_REFERRALCODE_BYUSERNAME);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getString("referralCode");
    		}
    		return null;
    	}catch(SQLException e){
    		Log.error("user findUserReferralCode  exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public Map<String,String> findUserThird(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	Map<String,String> map = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_THIRDID_BYUSERNAME);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			map = new HashMap<String,String>();
    			map.put("openId", rs.getString("openId"));
    			map.put("qqId", rs.getString("qqId"));
    		}
    		return map;
    	}catch(SQLException e){
    		Log.error("user select init exception: {}",e);
    		return map;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public boolean updateUserInit(String username,int init){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_USERINIT_BYUSERNAME);
    		pre.setInt(1, init);
    		pre.setString(2, username);
    		return pre.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user select init exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pre, con);
    	}
    }
    public Integer findUserInit(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_INIT_BYUSERNAME);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getInt("init");
    		}
    		return null;
    	}catch(SQLException e){
    		Log.error("user select init exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public String findUserPassword(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_PASSWORD);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getString("encryptedPassword");
    		}
    		return null;
    	}catch(SQLException e){
    		Log.error("user findUserPassword exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    
    public Map<String,String> findUserByThirdId(String openId,String qqId){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs  = null;
    	String sql = "select username,encryptedPassword from ofuser where ";
    	if(openId!=null){
    		sql+= " openId = '"+openId+"' ";
    	}
    	if(qqId!=null){
    		sql+= " qqId = '"+qqId+"' ";
    	}
    	Map<String,String> map = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		stat = con.createStatement();
    		rs =  stat.executeQuery(sql);
    		while(rs.next()){
    			map = new HashMap<String,String>();
    			map.put("username", rs.getString("username"));
    			map.put("encryptedPassword", rs.getString("encryptedPassword"));
    		}
    		return map;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return map;
    	}finally{
    		DbConnectionManager.closeConnection(rs,stat, con);
    	}
    }
    
    public boolean updateUserThirdId(String username,String openId,String qqId){
    	Connection con = null;
    	Statement stat = null;
    	String sql = "update ofuser set ";
    	if(openId!=null){
    		sql+= "openId = '"+openId+"' ,";
    	}
    	if(qqId!=null){
    		sql+= "qqId = '"+qqId+"' ,";
    	}
    	sql = sql.substring(0,sql.length()-1)+" where username = '"+username+"'";
    	try{
    		con = DbConnectionManager.getConnection();
    		stat = con.createStatement();
    		return stat.executeUpdate(sql)>0;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(stat, con);
    	}
    }
    public String findUserIsExsitsByQqId(String qqId){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_QQID_ISEXISTS);
    		pre.setString(1, qqId);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getString("username");
    		}
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
		return null;
    }
    
    public Map<String,String>  findUsernameAndPwdByMobile(String mobile){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USERINFO_BYMOBILE);
    		pre.setString(1, mobile);
    		rs = pre.executeQuery();
    		Map<String,String> map = null;
    		while(rs.next()){
    			map = new HashMap<String,String>();
    			map.put("username", rs.getString("username"));
    			map.put("encryptedPassword", rs.getString("encryptedPassword"));
    			map.put("openId", rs.getString("openId"));
    			map.put("qqId", rs.getString("qqId"));
    		}
    		return map;
    	}catch(SQLException e){
    		Log.error("findUsernameAndPwdByMobile exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    
    
    public boolean updateUserReferralCode(String referralCode,String parentReferralCode,String username ){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try {
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_USER_CODE);
    		pre.setString(1, referralCode);
    		pre.setString(2, parentReferralCode);
    		pre.setString(3, "00"+new Date().getTime());
    		pre.setString(4, username);
    		return pre.executeUpdate()>0;
		} catch (Exception e) {
			Log.error("findUserExists exception: {}",e);
			return false;
		}
    }
    public boolean updateUserReferralCodev2(String referralCode,String parentReferralCode,String username ){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try {
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_USER_CODEV2);
    		pre.setString(1, referralCode);
    		pre.setString(2, parentReferralCode);
    		pre.setString(3, "00"+new Date().getTime());
    		pre.setString(4, username);
    		return pre.executeUpdate()>0;
    	} catch (Exception e) {
    		Log.error("findUserExists exception: {}",e);
    		return false;
    	}
    }
    public boolean  findUserExistsByReferralCode(String referralCode){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USEREXISTS_BYCODE);
    		pre.setString(1, referralCode);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return true;
    		}
    		return false;
    	}catch(SQLException e){
    		Log.error("findUserExists exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    
    public boolean  findUserExistsByReferralCodev2(String referralCode){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USEREXISTS_BYCODEV2);
    		pre.setString(1, referralCode);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return true;
    		}
    		return false;
    	}catch(SQLException e){
    		Log.error("findUserExists exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    
    public Map findUserDetail(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USER_DETAIL);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		Map<String,Object> map = new HashMap<String,Object>();
    		while(rs.next()){
    			 map.put("username", rs.getString("username"));
    			 map.put("accountBalance", rs.getString("accountBalance"));
    			 map.put("gender", rs.getString("gender"));
    			 map.put("mobile", rs.getString("mobile"));
    			 map.put("nickName", rs.getString("nickName"));
    			 map.put("realName", rs.getString("realName"));
    			 map.put("region", rs.getString("region"));
    			 map.put("referralCode", rs.getString("referralCode"));
    			 map.put("freezingAmount", rs.getString("freezingAmount"));
    			 map.put("creationDate", rs.getString("creationDate"));
    			 map.put("modificationDate", rs.getString("modificationDate"));
    		}
    		return map;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public boolean updateUserNickNameOrPic(String username,String nickName,String pic){
    	Connection con = null;
    	Statement stat = null;
    	String sql = "update ofuser set ";
    	if(nickName!=null){
    		sql+= "nickName = '"+nickName+"' ,";
    	}
    	if(pic!=null){
    		sql+= "pic = '"+pic+"' ,";
    	}
    	sql = sql.substring(0,sql.length()-1)+" where username = '"+username+"' ";
    	try{
    		con = DbConnectionManager.getConnection();
    		stat = con.createStatement();
    		return stat.executeUpdate(sql)> 0;
    	}catch(SQLException e){
    		Log.error("user updatePicAndnickName exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(stat, con);
    	}
    }
    public Map findUserNickNameAndPic(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USER_NICKNAME_PIC);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		Map map = new HashMap();
    		while(rs.next()){
    			map.put("nickName", rs.getString(1));
    			map.put("pic", rs.getString(2));
    		}
    		return map;
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public String findUserNickName(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USER_NICKNAME);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getString(1);
    		}
    		return null;
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public String findUserDeviceCode(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USER_DEVICECODE);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		while(rs.next()){
    			return rs.getString(1);
    		}
    		return null;
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public boolean updateUserDeviceCode(String username,String deviceCode){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_USER_DEVICECODE);
    		pre.setString(1, deviceCode);
    		pre.setString(2, username);
    		return pre.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pre, con);
    	}
    }
    public String findMsgDetail(String username){
    	Connection con = null;
    	PreparedStatement pre = null;
    	ResultSet rs = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(SELECT_USER_MSG_DETAIL);
    		pre.setString(1, username);
    		rs = pre.executeQuery();
    		String status = null;
    		while(rs.next()){
    			status = rs.getString(1);
    		}
    		return status;
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pre, con);
    	}
    }
    public boolean updateMsgDetail(String username,String status){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_USER_MSG_DETAIL);
    		pre.setString(1, status);
    		pre.setString(2, username);
    		
    		return pre.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pre, con);
    	}
    }
    public boolean updateGenderAndRegionByUsername(String gender,String region,String username){
    	Connection con = null;
    	Statement state = null;
    	StringBuffer sql = new StringBuffer("UPDATE ofuser SET ");
    	try{
    		con = DbConnectionManager.getConnection();
    		state = con.createStatement();
    		if(gender!=null&&!gender.isEmpty()){
    			sql.append(" gender = '"+gender+"' ,");
    		}
    		if(region!=null&&!region.isEmpty()){
    			sql.append(" region = '"+region+"' ,");
    		}
    		String sqlNew = sql.toString(); 
    		return state.execute(sqlNew.substring(0,sqlNew.length()-1)+" WHERE username = '"+username+"'");
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(state, con);
    	}
    }
    public boolean updateOpenIdAndThirdTypeByUsername(String openId,String thirdType,String username){
    	Connection con = null;
    	PreparedStatement pst = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(UPDATE_USER_OPENID_THIRDTYPE);
    		pst.setString(1, openId);
    		pst.setString(2, thirdType);
    		pst.setString(3, username);
    		return pst.execute();
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pst, con);
    	}
    }
    public String findUserIsExsitsByOpenId(String openId,String thirdType){
    	Connection con = null;
    	Statement stat = null;
    	ResultSet rs = null;
    	String sql = "SELECT username FROM ofuser WHERE openId = '"+openId+"' ";
    	if(thirdType!=null&&!thirdType.isEmpty()){
    		sql+= " and thirdType = '"+thirdType+"'";
    	}
    	try{
    		con = DbConnectionManager.getConnection();
    		stat = con.createStatement();
    		rs = stat.executeQuery(sql);
    		while(rs.next()){
    			return rs.getString("username");
    		}
    	}catch(SQLException e){
    		Log.error("user select exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,stat, con);
    	}
		return null;
    }
    public boolean upDatePayPassword(String username,String pwd){
    	Connection con = null;
    	PreparedStatement pst = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(UPDATE_USER_PAYPASSWORD);
    		pst.setString(1, pwd);
    		pst.setString(2, username);
    		return pst.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pst, con);
    	}
    }
    public String findPayPassword(String username){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_USER_PATPASSWORD);
    		pst.setString(1, username);
    		rs = pst.executeQuery();
    		String pwd = null;
    		while(rs.next()){
    			pwd = rs.getString(1);
    		}
    		return pwd;
    	}catch(SQLException e){
    		Log.error("user find exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public String findUserRealName(String username){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_USER_REALNAME);
    		pst.setString(1, username);
    		rs = pst.executeQuery();
    		String realName = null;
    		while(rs.next()){
    			realName = rs.getString(1);
    		}
    		return realName;
    	}catch(SQLException e){
    		Log.error("user find exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public boolean isExists(String username){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_USER_EXISTS);
    		pst.setString(1, username);
    		rs = pst.executeQuery();
    		String createDate = null;
    		while(rs.next()){
    			createDate = rs.getString(1);
    		}
    		if(createDate==null||createDate.isEmpty()){
    			return false;
    		}
    		return true;
    	}catch(SQLException e){
    		Log.error("user exists exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public boolean updateUserInfo(String userName,String realName,String mobile,String gender,String region){
    	StringBuffer updateSql = new StringBuffer("UPDATE ofuser SET "); 
    	Connection con = null;
    	PreparedStatement pst = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		if(realName!=null&&!"".equals(realName)){
    			Log.info("实名认证，并赠送泡币");
    			updateSql.append("realName = ? ,dbBalance=dbBalance+CONVERT((select `VALUE` from sys_config where `KEY`='REALNAME_dbBalance') , signed),");
    		}
    		if(mobile!=null&&!"".equals(mobile)){
    			updateSql.append("mobile = ? ,");
    		}
    		if(gender!=null&&!"".equals(gender)){
    			updateSql.append("gender = ? ,");
    		}
    		if(region!=null&&!"".equals(region)){
    			updateSql.append("region = ? ,");
    		}
    		String sql = updateSql.toString().substring(0,updateSql.toString().length()-1);
    		sql += " WHERE username = ? ";
    		pst = con.prepareStatement(sql);
    		int i = 0;
    		if(realName!=null&&!"".equals(realName)){
    			pst.setString(i+=1, realName);
    		}
    		if(mobile!=null&&!"".equals(mobile)){
    			pst.setString(i+=1, mobile);
    		}
    		if(gender!=null&&!"".equals(gender)){
    			pst.setString(i+=1, gender);
    		}
    		if(region!=null&&!"".equals(region)){
    			pst.setString(i+=1, region);
    		}
			pst.setString(i += 1, userName);
    		return pst.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pst, con);
    	}
    }
    public Map<String,Object> findUserInfo(String username){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_USER_INFO);
    		pst.setString(1, username);
    		rs = pst.executeQuery();
    		Map<String,Object> userInfo = new HashMap<String,Object>();
    		while(rs.next()){
    			userInfo.put("realName", rs.getString(1));
    			userInfo.put("mobile", rs.getString(2));
    			userInfo.put("gender", rs.getString(4));
    			Map<String,Object> region = new HashMap<String,Object>();
    			if(rs.getString(3)!=null&&!"".equals(rs.getString(3))){
    				if(rs.getString(3).contains(",")){
    					String[] strs = rs.getString(3).split(",");
    					if(strs.length==3){
    						region.put("country", strs[0]);
    						region.put("province", strs[1]);
    						region.put("city", strs[2]);
    					}
    					if(strs.length==2){
    						region.put("country", strs[0]);
    						region.put("province", strs[1]);
    					}
    					if(strs.length==1){
    						region.put("country", strs[0]);
    					}
    				}
    			}
    			userInfo.put("region", region);
    		}
    		return userInfo;
    	}catch(SQLException e){
    		Log.error("user exists exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public List<Map<String,Object>> findCountry(){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	List<Map<String,Object>> country = new ArrayList<Map<String,Object>>();
    	Map<String,Object> map = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_COUNTRY);
    		rs = pst.executeQuery();
    		while(rs.next()){
    			map = new HashMap<String,Object>();
    			map.put("id", rs.getInt(1));
    			map.put("country", rs.getString(2));
    			country.add(map);
    		}
    		return country;
    	}catch(SQLException e){
    		Log.error("user exists exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    @SuppressWarnings("resource")
	public List<Map<String,Object>> findProvince(int countryId){
    	Connection con = null;
    	PreparedStatement pst = null;
    	Statement state = null;
    	ResultSet rs = null;
    	List<Map<String,Object>> province = new ArrayList<Map<String,Object>>();
    	Map<String,Object> map = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_PROVINCE);
    		pst.setInt(1, countryId);
    		rs = pst.executeQuery();
    		if(rs.next()){
    			while(rs.next()){
        			map = new HashMap<String,Object>();
        			map.put("id", rs.getInt(1));
        			map.put("province", rs.getString(2));
        			province.add(map);
        		}
    		}else{
    			String sql = "SELECT * FROM ofcity WHERE ancestorId = "+countryId +" and parentId = " +countryId;
    			state = con.createStatement();
    			rs = state.executeQuery(sql);
    			while(rs.next()){
        			map = new HashMap<String,Object>();
        			map.put("id", rs.getInt(1));
        			map.put("province", rs.getString(2));
        			province.add(map);
        		}
    		}
    		return province;
    	}catch(SQLException e){
    		Log.error("user exists exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public List<Map<String,Object>> findCity(int country,int provinceId){
    	Connection con = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null; 
    	List<Map<String,Object>> province = new ArrayList<Map<String,Object>>();
    	Map<String,Object> map = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pst = con.prepareStatement(SELECT_CITY);
    		pst.setInt(1, provinceId);
    		pst.setInt(2, country);
    		rs = pst.executeQuery();
    		while(rs.next()){
    			map = new HashMap<String,Object>();
    			map.put("id", rs.getInt(1));
    			map.put("city", rs.getString(2));
    			province.add(map);
    		}
    		return province;
    	}catch(SQLException e){
    		Log.error("user exists exception: {}",e);
    		return null;
    	}finally{
    		DbConnectionManager.closeConnection(rs,pst, con);
    	}
    }
    public boolean updateBalance(String username,BigDecimal uBalance,String tousername,BigDecimal tuBalance){
    	Connection con = null;
    	Statement state = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		con.setAutoCommit(false);
    		state = con.createStatement();
    		state.executeUpdate("UPDATE ofuser SET accountBalance = "+uBalance+" WHERE username = '"+username+"';");
    		state.executeUpdate("UPDATE ofuser SET accountBalance = "+tuBalance+" WHERE username = '"+tousername+"';");
    		con.commit();
    		return true;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(state, con);
    	}
    }
    private String UPDATE_THIRDTYPE = "UPDATE ofuser SET isRobot = 1 , nickName = ? WHERE username = ? ";
    public boolean updateRobotType(String username,String nickName){
    	Connection con = null;
    	PreparedStatement pre = null;
    	try{
    		con = DbConnectionManager.getConnection();
    		pre = con.prepareStatement(UPDATE_THIRDTYPE);
    		pre.setString(1, nickName);
    		pre.setString(2, username);
    		return pre.executeUpdate()>0;
    	}catch(SQLException e){
    		Log.error("user update exception: {}",e);
    		return false;
    	}finally{
    		DbConnectionManager.closeConnection(pre, con);
    	}
    }
    
    /*public Map selectNickAndPhoto(String username){
    	String nick = VCardManager.getInstance().getVCardProperty(username, "NICKNAME");
    	Map map = new HashMap();
		map.put("nickName", nick);
		Element vcard = VCardManager.getInstance().getVCard(username);
		if(vcard!=null){
			Element photo = vcard.element("PHOTO");
			if(photo!=null){
				Element type = photo.element("TYPE");
				Element binval = photo.element("BINVAL");
				byte[] data = Base64.decode(binval.getText());
				String name = null;
				if(type!=null){
					name = "123."+type.getText().split("/")[1];
				}else{
					name = "123.jpg";
				}
				Map mm = new HashMap();
				mm.put(name, data);
				String domain = JiveGlobals.getProperty("plugin.restapi.fileserver");
				String result = null;
				try {
					result = HttpsPost.postFile(domain, mm);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				if(result==null||result.isEmpty()){
					return null;
				}
				JSONObject jsonStr = JSONObject.fromObject(result);
				Object code = jsonStr.get("code");
				if(!"0".equals(code)){
					return null;
				}
				String img = (String)jsonStr.get("url");
				map.put("pic", img);
			}else{
				map.put("pic",null);
			}
		}
		return map;
    }*/
}

