package org.jivesoftware.openfire.plugin.rest.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Test {
	/*public static void sendMessage(GaContext context,ApointBusiCustomer apointBusiCustomer) throws JSONException, SQLException {
        String xmppDomain = JDBCUtilHelper.getSystemDomain();
        String username = context.getUsername();
        String roomName = apointBusiCustomer.getRoomName();
        String fromJid = roomName+"@conference."+xmppDomain+"/"+username;
        long businessCusBallotId = apointBusiCustomer.getBusinessCusBallotId();
        //获取讨论组成员JID列表
        List<String> list = getRoomMember(roomName);
        for (int i = 0; i < list.size(); i++) {
            String userJid = list.get(i);
            String toJid = userJid;
            Message message = new Message();
            message.setType(Message.Type.groupchat);
            message.setID(UUID.randomUUID().toString());
            message.setFrom(new JID(fromJid));
            message.setTo(new JID(toJid));
            JSONObject obj = new JSONObject();
            JSONObject contentBody = new JSONObject();
            contentBody.put("ballotId", businessCusBallotId);
            contentBody.put("title", "福利商机大派送");
            contentBody.put("content", "抽取商机");
            contentBody.put("ballotStatus", 1);
            obj.put("content", contentBody);
            obj.put("type", 6);
            message.setBody(obj.toString());
            //发送方式三
            XMPPServer.getInstance().getMessageRouter().route(message);
        }
    }*/
	public static void main(String[] args) throws Exception {
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		 Connection con = getCon();
		 Statement state = null;
	        try  
	        {  
	            DocumentBuilder db = dbf.newDocumentBuilder();  
	            Document doc = db.parse("C:/Users/Administrator/Desktop/LocList.xml");  
	            NodeList Country = doc.getElementsByTagName("CountryRegion");
	            state = con.createStatement();
	            for(int x =0 ; x<Country.getLength();x++){
	            	//得到一个国家
	            	Node co = Country.item(x);
	            	Element elem = (Element)co;
	            	System.out.println(elem.getAttribute("Name")+(x+1));
	            	String sqlco = "insert into ofcountry(id,name) values("+(x+1)+", '"+elem.getAttribute("Name")+"');";
	            	state.execute(sqlco);
	            	//得到这个国家的所有省份
	            	NodeList st = elem.getElementsByTagName("State");
	            	if(st.getLength()>0){
	            		for(int z = 0; z<st.getLength();z++){
	            			Node s =  st.item(z);
	            			Element ss = (Element) s ;
	            			int proId = 0;
	            			if(ss.getAttribute("Name")!=null&&!"".equals(ss.getAttribute("Name"))){
	            				proId = Integer.valueOf((x+1)+""+(z+1));
	            				System.out.println("省：-->"+ss.getAttribute("Name")+proId);
	            			String sqlpo = "insert into ofprovince(id,name,parentId) values("+proId+", '"+ss.getAttribute("Name")+"', "+(x+1)+");";
	            			state.execute(sqlpo);
	            			}
	            			NodeList citys = ss.getElementsByTagName("City");
	            			if(citys.getLength()>0){
	            				for(int i = 0; i<citys.getLength();i++){
	            					Node ci =  citys.item(i);
	    	            			Element cc = (Element) ci ;
	    	            			if(ss.getAttribute("Name")!=null&&!"".equals(ss.getAttribute("Name"))){
	    	            				System.out.println("市：---->"+cc.getAttribute("Name")+proId+""+(i+1));
	    	            				String sql1 = "insert into ofcity(id,name,parentId,ancestorId) values("+Integer.valueOf(proId+""+(i+1))+", '"+cc.getAttribute("Name")+"', "+proId+" , "+(x+1)+" );";
		    	            			state.execute(sql1);
	    	            			}
	    	            			if(ss.getAttribute("Name")==null||"".equals(ss.getAttribute("Name"))){
	    	            				System.out.println("市：---->"+cc.getAttribute("Name")+(x+1)+""+(i+1));
		    	            			String sql = "insert into ofcity(id,name,parentId,ancestorId) values("+Integer.valueOf((x+1)+""+(i+1))+", '"+cc.getAttribute("Name")+"', "+(x+1)+" , "+(x+1)+" );";
		    	            			state.execute(sql);
	    	            			}
	            				}
	            			}
	            		}
	            	}
	            }
	            System.out.println(Country.getLength());
	            
	        }  
	        catch (Exception e)  
	        {  
	            e.printStackTrace();  
	        }  
	}
	
	public static Connection getCon(){
		Connection conn = null;
		  String url = null;
		  String user = "root";
		  String password = "root";
		  try {
		   Class.forName("com.mysql.jdbc.Driver"); //加载mysq驱动
		  } catch (ClassNotFoundException e) {
		   System.out.println("驱动加载错误");
		   e.printStackTrace();//打印出错详细信息
		  }
		  try {
		   url = 
		    "jdbc:mysql://127.0.0.1:3306/openfire?rewriteBatchedStatements=true";
		   conn = DriverManager.getConnection(url,user,password);
		   return conn;
		  } catch (SQLException e) {
		   System.out.println("数据库链接错误");
		   e.printStackTrace();
		  }
		return null;
	}
	/*@POST
	@Path("/drawMoney")
	public String drawMoney(@FormParam("username") String username,@FormParam("money") String money,@FormParam("bankCode") String bankCode,@FormParam("provice") String provice,
			@FormParam("city") String city,@FormParam("branchName") String branchName,@FormParam("remark") String remark,
			@FormParam("amountName") String amountName,@FormParam("cardNo") String cardNo) throws UserNotFoundException, UnsupportedEncodingException{
		if(username==null||"".equals(username)){
			return ResultUtils.fail(ErrEnum.ERR_USERNAME_NULL.getMsg(), ErrEnum.ERR_USERNAME_NULL.getValue());
		}
		User user = UserManager.getInstance().getUser(username);
		if(user==null){
			return ResultUtils.fail(ErrEnum.ERR_USER_NULL.getMsg(),ErrEnum.ERR_USER_NULL.getValue());
		}
		if(money==null||"".equals(money)){
			return ResultUtils.fail(ErrEnum.ERR_MONEY_NULL.getMsg(), ErrEnum.ERR_MONEY_NULL.getValue());
		}
		if(bankCode==null||"".equals(bankCode)){
			return ResultUtils.fail(ErrEnum.ERR_BANKCODE_NULL.getMsg(), ErrEnum.ERR_BANKCODE_NULL.getValue());
		}
		if(provice==null||"".equals(provice)){
			return ResultUtils.fail(ErrEnum.ERR_PROVICE_NULL.getMsg(), ErrEnum.ERR_PROVICE_NULL.getValue());
		}
		if(city==null||"".equals(city)){
			return ResultUtils.fail(ErrEnum.ERR_CITY_NULL.getMsg(), ErrEnum.ERR_CITY_NULL.getValue());
		}
		if(branchName==null||"".equals(branchName)){
			return ResultUtils.fail(ErrEnum.ERR_BRANCHNAME_NULL.getMsg(), ErrEnum.ERR_BRANCHNAME_NULL.getValue());
		}
		if(amountName==null||"".equals(amountName)){
			return ResultUtils.fail(ErrEnum.ERR_ACCOUNTNAME_NULL.getMsg(), ErrEnum.ERR_ACCOUNTNAME_NULL.getValue());
		}
		//生成订单
		OrderEntity order = new OrderEntity();
		order.setMoney(DecimalUtils.format(money));
		order.setUserName(username);
		order.setOrderNo(RandomUtils.random());
		order.setOrderName("提现");
		order.setOrderStatus(OrderStatusEnum.WZF);
		order.setPayType(PayTypeEnum.BALANCE);
		order.setRemark(remark);
		OrderManager.getInstance().save(order);
		
		//生成签名
		String str = "transId="+order.getOrderNo()+"&accountNumber="
		+Config.WYNO+"&cardNo="+cardNo+"&amount="+money+"&"+Config.MD5key;
		System.out.println(str);
		MD5 md5 = new MD5();
		String sign = md5.getMD5ofStr(str);
		
		//组装xml
		Document doc = DocumentHelper.createDocument(); 
        Element yemadai = doc.addElement("yemadai");
        Element accountNumber = yemadai.addElement("accountNumber"); 
        accountNumber.setText(Config.WYNO);
        Element notifyURL = yemadai.addElement("notifyURL");
        notifyURL.setText("http://pay1.doowal.com:9090/plugins/restapi/vi/account/test");
        Element tt = yemadai.addElement("tt");
        tt.setText("1");
        Element transferList = yemadai.addElement("transferList");
        Element transId = transferList.addElement("transId");
        transId.setText(order.getOrderNo());
        Element bankCodes = transferList.addElement("bankCode");
        bankCodes.setText(bankCode);
        Element provices = transferList.addElement("provice");
        provices.setText(provice);
        Element citys = transferList.addElement("city");
        citys.setText(city);
        Element branchNames = transferList.addElement("branchName");
        branchNames.setText(branchName);
        Element accountNames = transferList.addElement("accountName");
        accountNames.setText(amountName);
        Element cardNos = transferList.addElement("cardNo");
        cardNos.setText(cardNo);
        Element amounts = transferList.addElement("amount");
        amounts.setText(money);
        Element remarks = transferList.addElement("remark");
        remarks.setText(remark);
        Element secureCode = transferList.addElement("secureCode");
        secureCode.setText(sign);
        String data = null;
        String strDoc = doc.asXML();
        String[] ss = strDoc.split("\\?>");
        strDoc = ss[0]+" standalone=\"yes\"?>"+ss[1];
        System.out.println(strDoc);
       // try {
			data = HttpsPost.post("https://gwapi.yemadai.com/transfer/transferapi", "transData="+Base64Utils.encode(strDoc));
		} catch (KeyManagementException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
        String xml = Base64Utils.decode(data);
        System.out.println(xml);
        try {
			Document d = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
        
        return "";
	} */
}
