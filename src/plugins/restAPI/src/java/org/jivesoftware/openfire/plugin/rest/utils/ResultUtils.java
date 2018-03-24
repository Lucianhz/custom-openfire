package org.jivesoftware.openfire.plugin.rest.utils;

import net.sf.json.JSONObject;

public class ResultUtils {
	public static String success(Object data) {
		JSONObject json = new JSONObject();
		json.put("code", "0");
		json.put("data", data);
		json.put("msg", "success");
		return json.toString();
	}
	public static String fail(String msg,String code){
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		return json.toString();
	}
}
