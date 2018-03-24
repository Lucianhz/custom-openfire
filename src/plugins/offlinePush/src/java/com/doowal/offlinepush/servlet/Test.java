package com.doowal.offlinepush.servlet;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationBigPayload;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.NotificationThreads;

public class Test {
	private static AppleNotificationServer appleServer = null;
	private static List<PayloadPerDevice> list;
	private static NotificationThreads work = null;
	static PushNotificationManager pushManager = new PushNotificationManager();
	static{
		try {
			appleServer = new AppleNotificationServerBasicImpl("F:/develop_test.p12", "doowalF302f302", true);
			 // true：表示的是产品测试推送服务 false：表示的是产品发布推送服务
	        pushManager.initializeConnection(new AppleNotificationServerBasicImpl(
	                 "F:/develop_test.p12", "doowalF302f302", true));
		} catch (KeystoreException | CommunicationException e1) {
			System.out.println(e1.getMessage());
		}
	}
	private static void pushOfflineMsg(String token, String pushCont, String jid) throws Exception   {
		
		try {
			Integer size = 1;
			
			PushNotificationBigPayload payload =  PushNotificationBigPayload.complex();
			payload.addAlert(pushCont);
			payload.addSound("default");
			payload.addBadge(size);
			payload.addCustomDictionary("jid", 123);
			PayloadPerDevice pay = new PayloadPerDevice(payload, token);
			if (list == null) {
				list = new ArrayList<PayloadPerDevice>();
			}
			//Push.payload(payload, "F:/develop_test.p12", "doowalF302f302", true, "dd15755ac449ef91dab8c34d31f0fad47f824558d6d586f3c55e17206c1aecff");
			list.add(pay);
			work = new NotificationThreads(appleServer, list, 1);
			list= null;
			work.setMaxNotificationsPerConnection(20);
			work.start();
		} catch (JSONException e) {
			System.out.println("JSONException:" + e.getMessage());
		//} catch (InvalidDeviceTokenFormatException e) {
		//	System.out.println("InvalidDeviceTokenFormatException:" + e.getMessage());
		} finally {
			System.out.println("push to apple: username: " + 123 + " ,context" + pushCont);
		}
	}
	/*public static void main(String[] args)  {
		int s = 10;
		//while(s>0){
		//	s--;
			pushOfflineMsg("dd15755ac449ef91dab8c34d31f0fad47f824558d6d586f3c55e17206c1aecff","光头强请求加你为好友123","光头强123");
		//}
	}*/
	public static void push() throws Exception{
		   System.out.println("zsl==========开始推送消息");
	         int badge = 1; // 图标小红圈的数值
	         String sound = "default"; // 铃音
	        // String msgCertificatePassword = "doowalF302f302";//导出证书时设置的密码
	         String deviceToken = "e1f000d40a3e09ac5825cd76c4550dc5d1697f3af046905374f793f57a1bcdfb"; //手机设备token号
	         String message = "test push message to ios device";

	         List<String> tokens = new ArrayList<String>();
	         tokens.add(deviceToken);
	         
	         boolean sendCount = true;

	         PushNotificationPayload payload = new PushNotificationPayload();
	         payload.addAlert(message); // 消息内容
	         payload.addBadge(badge);


	         //payload.addCustomAlertBody(msgEX);
	         if (null == sound || "".equals(sound)) {
	             payload.addSound(sound);
	         }
	        // List<PushedNotification> notifications = new ArrayList<PushedNotification>();
	         // 开始推送消息
	         if (sendCount) {
	             Device device = new BasicDevice();
	             device.setToken(deviceToken);
	             pushManager.initializeConnection(new AppleNotificationServerBasicImpl(
		                 "F:/develop_test.p12", "doowalF302f302", true));
	             pushManager.sendNotification(
	                     device, payload, true);
	             //notifications.add(notification);
	         } else {
	             List<Device> devices = new ArrayList<Device>();
	             for (String token : tokens) {
	                 devices.add(new BasicDevice(token));
	             }
	             pushManager.sendNotifications(payload, devices);
	             
	         }
	         pushManager.stopConnection();
	       /*  List<PushedNotification> failedNotification = PushedNotification
	                 .findFailedNotifications(notifications);
	         List<PushedNotification> successfulNotification = PushedNotification
	                 .findSuccessfulNotifications(notifications);
	         int failed = failedNotification.size();
	         int successful = successfulNotification.size();
	         System.out.println("zsl==========成功数：" + successful);
	         System.out.println("zsl==========失败数：" + failed);
	         pushManager.stopConnection();
	         System.out.println("zsl==========消息推送完毕"+pushManager);*/
	 }
	 public static void main(String[] args) throws Exception{
		 while(true){
			pushOfflineMsg("e1f000d40a3e09ac5825cd76c4550dc5d1697f3af046905374f793f57a1bcdfb","光头强请求加你为好友123","光头强123");
			//push();
		 }
	 }
      
}
