package org.jivesoftware.test;

import org.json.JSONException;
import org.json.JSONObject;

public class Test {

	public static void main(String[] args) {
		JSONObject js = null;
			js= new JSONObject();
			try {
				js.put("bofy", "213");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println(js);

	}
}
