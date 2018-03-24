package org.jivesoftware.openfire.plugin.rest.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Sender;

import javapns.communication.exceptions.KeystoreException;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.NotificationThreads;
@SuppressWarnings("unused")
public class PushUtils {
	private static AppleNotificationServer appleServer = null;
	private static List<PayloadPerDevice> list;
	private static String path = System.getProperty("openfireHome") + "/conf/";
	private static String name = JiveGlobals.getProperty("plugin.offlinepush.dcName");
	private static boolean enabled = JiveGlobals.getBooleanProperty("plugin.offlinepush.enabled");
	private static String password = JiveGlobals.getProperty("plugin.offlinepush.password");
	private static String appSecretKey = JiveGlobals.getProperty("plugin.offlinepush.xiaomi.app_secret_key");
	private static String packageName = JiveGlobals.getProperty("plugin.offlinepush.xiaomi.packagename");
	private static Sender sender = null;
	private static final Logger Log = LoggerFactory.getLogger(PushUtils.class);
	static{
		try {
			appleServer = new AppleNotificationServerBasicImpl(path+name, password, enabled);
			if (list == null) {
				list = new ArrayList<PayloadPerDevice>();
			}
			Sender sender = new Sender(appSecretKey);
	
		} catch (KeystoreException e1) {
			System.out.println(e1.getMessage());
		}
	}
	public static void pushIOSMsg(String token, String pushCont, String jid,String type)   {
		NotificationThreads work = null;
		try {
			Integer size = 1;
			List<PayloadPerDevice> list = new ArrayList<PayloadPerDevice>();
			PushNotificationPayload payload = new PushNotificationPayload();
			payload.addAlert(pushCont);
			payload.addSound("default");
			payload.addBadge(size);
			payload.addCustomDictionary("jid", 123);
			payload.addCustomDictionary("type", type);
			PayloadPerDevice pay = new PayloadPerDevice(payload, token);
			list.add(pay);
			work = new NotificationThreads(appleServer, list, 1);
			work.start();
		} catch (JSONException e) {
			Log.error("JSONException:" + e.getMessage());
		} catch (InvalidDeviceTokenFormatException e) {
			Log.error("InvalidDeviceTokenFormatException:" + e.getMessage());
		} finally {
			Log.info("push to apple: username: " + 123 + " ,context" + pushCont);
		}
	}
	
	public static void pushAndroidMsg(String title,String content ,String username){
	  	Constants.useOfficial();
	    sender = new Sender(appSecretKey);
	    //alias非空白, 不能包含逗号, 长度小于128
	    com.xiaomi.xmpush.server.Message message = 
	    		new com.xiaomi.xmpush.server.Message.Builder()
	                .title(title)
	                .description(content)
	                .restrictedPackageName(packageName)
	                .notifyType(1)     // 使用默认提示音提示
	                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY) //调起APP
	                .build();
	    try {
	    	//根据alias, 发送消息到指定设备上
			sender.sendToAlias(message, username, 3);
		} catch (IOException e) {
			e.printStackTrace();
			Log.error("IOException: "+e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			Log.error("ParseException: "+e.getMessage());
		} 
	}
}
