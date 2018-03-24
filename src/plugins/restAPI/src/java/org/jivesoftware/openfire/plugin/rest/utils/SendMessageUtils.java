package org.jivesoftware.openfire.plugin.rest.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageUtils {

	public static final String charset = "utf-8";
	// 用户平台API账号(非登录账号,示例:N1234567)
	public static String account = "N2357400";
	// 用户平台API密码(非登录密码)
	public static String pswd = "PboyqwOGW5c5a0";
	private static final Logger Log = LoggerFactory.getLogger(SendMessageUtils.class);
	//网建短信平台用户名
	public static String ssmuid = JiveGlobals.getProperty("ssm.user");
	//接口安全秘钥
	public static String ssmpwd = JiveGlobals.getProperty("ssm.password");
	
	/*public static String ssmuid = "xiaopao";
	public static String ssmpwd = "ea67ff80ef3c7920e577";*/
	/*public static boolean sendSMCode(String phone,String number){
		try{
		//请求地址请登录253云通讯自助通平台查看或者询问您的商务负责人获取
		String smsVariableRequestUrl = "http://smssh1.253.com/msg/variable/json";
		//短信内容
		String msg = "【彩虹社交】您的验证码为{$var},请在5分钟内使用有效，请不要告诉别人哦~";
		//参数组	eg 187********,女士,123456,3;130********,先生,123456,3;															
		String params = phone+","+number;
		//状态报告
		String report= "true";
		
		SmsVariableRequest smsVariableRequest=new SmsVariableRequest(account, pswd, msg, params, report);
		
        String requestJson = JSON.toJSONString(smsVariableRequest);
		
		System.out.println("before request string is: " + requestJson);
		
		String response = ChuangLanSmsUtil.sendSmsByPost(smsVariableRequestUrl, requestJson);
		
		System.out.println("response after request result is : " + response);
		
		SmsVariableResponse smsVariableResponse = JSON.parseObject(response, SmsVariableResponse.class);
		System.out.println("response  toString is : " + smsVariableResponse);
		if(smsVariableResponse!=null){
			if("0".equals(smsVariableResponse.getCode())){
				return true;
			}
		}
		return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}*/
	public static boolean sendSMCode1(String phone,String number){
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod("http://gbk.api.smschinese.cn"); 
		post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=gbk");//在头文件中设置转码
		NameValuePair[] data ={ new NameValuePair("Uid", ssmuid),new NameValuePair("Key", ssmpwd),new NameValuePair("smsMob",phone),new NameValuePair("smsText","您的验证码为："+number+",请在5分钟内使用,打死都不能告诉别人哦~")};
		post.setRequestBody(data);
		try {
			client.executeMethod(post);
			//Header[] headers = post.getResponseHeaders();
			//int statusCode = post.getStatusCode();
			/*System.out.println("statusCode:"+statusCode);
			for(Header h : headers)
			{
			System.out.println(h.toString());
			}*/
			String result = new String(post.getResponseBodyAsString().getBytes("gbk")); 
			//System.out.println("result: "+result); //打印返回消息状态
			post.releaseConnection();
			if("1".equals(result)){
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public static void main(String[] args){
		System.out.println(sendSMCode1("13282160536","123123"));
	}
}
