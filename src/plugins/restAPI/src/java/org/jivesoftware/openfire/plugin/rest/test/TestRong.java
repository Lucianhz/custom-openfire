package org.jivesoftware.openfire.plugin.rest.test;

import java.io.Reader;

import org.jivesoftware.util.JiveGlobals;

import com.rong.RongCloud;
import com.rong.models.TokenResult;

public class TestRong {

	public static void main(String[] args) throws Exception {
		String appKey = "pgyu6atqp9r3u";//替换成您的appkey
		String appSecret = "L6bo7SiyWxX";//替换成匹配上面key的secret
		
		RongCloud rongCloud = RongCloud.getInstance(JiveGlobals.getProperty("rongAppKey"),
				JiveGlobals.getProperty("rongAppSecret"));
		// 获取 Token 方法 
		TokenResult userGetTokenResult = rongCloud.user.getToken("7788", "", "");
		System.out.println("getToken:  " + userGetTokenResult.toString());

	}

}
