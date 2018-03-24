package org.jivesoftware.openfire.plugin.rest.utils;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.util.JiveGlobals;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doowal.offlinepush.plugin.OfflinePushPlugin;

import javapns.communication.exceptions.KeystoreException;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.NotificationThreads;

public class PushMsgUtils {
	private static Logger Log = LoggerFactory.getLogger(PushMsgUtils.class);
	private static AppleNotificationServer appleServer = null;
	private static String dcpath = System.getProperty("openfireHome") + "/conf/";
	private static String dcName = JiveGlobals.getProperty("plugin.offlinepush.dcName");
	private static String password = JiveGlobals.getProperty("plugin.offlinepush.password");
	private static boolean status = JiveGlobals.getBooleanProperty("plugin.offlinepush.enabled");
	private static List<PayloadPerDevice> list;
	static{
		try {
			appleServer = new AppleNotificationServerBasicImpl(dcpath+dcName, password, status);
			if (list == null) {
				list = new ArrayList<PayloadPerDevice>();
			}
		} catch (KeystoreException e) {
			Log.error("Push err:"+e.getMessage());
		}
	}
	public static void applePushOfflineMsg(String token, String pushCont, String jid)   {
		NotificationThreads work = null;
		try {
			Integer size = 1;
			List<PayloadPerDevice> list = new ArrayList<PayloadPerDevice>();
			PushNotificationPayload payload = new PushNotificationPayload();
			payload.addAlert(pushCont);
			payload.addSound("default");
			payload.addBadge(size);
			payload.addCustomDictionary("jid", 123);
			PayloadPerDevice pay = new PayloadPerDevice(payload, token);
			list.add(pay);
			work = new NotificationThreads(appleServer, list, 1);
			work.start();
		} catch (JSONException e) {
			Log.error("Push err:"+e.getMessage());
		} catch (InvalidDeviceTokenFormatException e) {
			Log.error("Push err:"+e.getMessage());
		} 
	}
}
