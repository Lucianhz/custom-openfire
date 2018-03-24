package com.doowal.offlinepush.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import com.dbay.apns4j.IApnsService;
import com.dbay.apns4j.impl.ApnsServiceImpl;
import com.dbay.apns4j.model.ApnsConfig;
import com.dbay.apns4j.model.Feedback;
import com.dbay.apns4j.model.Payload;

public class IOSPushMsg {

	private static IApnsService apnsService;
	private static final Logger logger = LoggerFactory.getLogger(IOSPushMsg.class);
	// 证书安装的目录
	private static String dcpath = System.getProperty("openfireHome") + "/conf/"+JiveGlobals.getProperty("plugin.offlinepush.dcName");
	private static String dcpwd = JiveGlobals.getProperty("plugin.offlinepush.password");
	private static boolean enabled = JiveGlobals.getBooleanProperty("plugin.offlinepush.enabled");
	private static IApnsService getApnsService() throws FileNotFoundException {
		if (apnsService == null) {
			ApnsConfig config = new ApnsConfig();
			//InputStream is = new FileInputStream(new File("C:/Users/Administrator/Desktop/ios_push/caihongDIstribution.p12"));
			//config.setPassword("doowalF302f302");
			//config.setDevEnv(true);
			InputStream is = new FileInputStream(new File(dcpath));
			config.setPassword(dcpwd);
			config.setKeyStore(is);
			config.setDevEnv(enabled);
			config.setPoolSize(10);
			// 假如需要在同个java进程里给不同APP发送通知，那就需要设置为不同的name
//			config.setName("welove1");
			apnsService = ApnsServiceImpl.createInstance(config);
		}
		return apnsService;
	}
	public static void pushMsg(String token,String content,JID jid,Cache<String, Integer> count,UserManager userManager){
		IApnsService service = null;
		try {
			service = getApnsService();
		} catch (FileNotFoundException e) {
			logger.error("IOS Push getApnsService err: "+e.getMessage());
			return;
		}
		String status = userManager.findMsgDetail(jid.getNode());
		if("0".equals(status)){
			content = "你收到一条消息";
		}
		Integer size = 1;
    	if(count.containsKey(jid.getNode())){
    		size = count.get(jid.getNode()) + 1;
    	}
    	if (size <= 1000)
    		count.put(jid.getNode(), size);
		
		// send notification
		//String token = "1ad016efdf52a12bbd6200428db563cc34aa426e5aed6d4dc870433d06cd72cc";
		Payload payload = new Payload();
		payload.setAlert(content);
		// If this property is absent, the badge is not changed. To remove the badge, set the value of this property to 0
		payload.setBadge(size);
		// set sound null, the music won't be played
		payload.setSound("default");
		payload.addParam("jid", jid.toString());
		service.sendNotification(token, payload);
		
		// payload, use loc string
		/*Payload payload2 = new Payload();
		payload2.setBadge(1);
		payload2.setAlertLocKey("GAME_PLAY_REQUEST_FORMAT");
		payload2.setAlertLocArgs(new String[]{"Jenna", "Frank"});
		service.sendNotification(token, payload2);
		*/
		// get feedback
		/*List<Feedback> list = service.getFeedbacks();
		if (list != null && list.size() > 0) {
			for (Feedback feedback : list) {
				System.out.println(feedback.getDate() + " " + feedback.getToken());
			}
		}*/
	}
	public static void main(String[] args) throws FileNotFoundException {
		IApnsService service = getApnsService();
		
		// send notification
		String token = "1ad016efdf52a12bbd6200428db563cc34aa426e5aed6d4dc870433d06cd72cc";
		
		Payload payload = new Payload();
		payload.setAlert("How are you?");
		// If this property is absent, the badge is not changed. To remove the badge, set the value of this property to 0
		payload.setBadge(1);
		// set sound null, the music won't be played
//		payload.setSound(null);
		payload.setSound("default");
		payload.addParam("uid", 123456);
		payload.addParam("type", 12);
		service.sendNotification(token, payload);
		
		// payload, use loc string
		/*Payload payload2 = new Payload();
		payload2.setBadge(1);
		payload2.setAlertLocKey("GAME_PLAY_REQUEST_FORMAT");
		payload2.setAlertLocArgs(new String[]{"Jenna", "Frank"});
		service.sendNotification(token, payload2);
		*/
		// get feedback
		List<Feedback> list = service.getFeedbacks();
		if (list != null && list.size() > 0) {
			for (Feedback feedback : list) {
				System.out.println(feedback.getDate() + " " + feedback.getToken());
			}
		}
		
//		try {
//			Thread.sleep(5000);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		// It's a good habit to shutdown what you never use
//		service.shutdown();
		
//		System.exit(0);
	}

}