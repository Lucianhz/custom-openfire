package org.jivesoftware.openfire.plugin.rest.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {
	public static String encode(String data){
		String str = null;
		if(data!=null&&!"".equals(data)){
			try {
				str = new String(Base64.encodeBase64(data.getBytes("utf-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str;
	}
	public static String decode(String data){
		String str = null;
		if(data!=null&&!"".equals(data)){
			try {
				str = new String(Base64.decodeBase64(data.getBytes("utf-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str;
	}
}
