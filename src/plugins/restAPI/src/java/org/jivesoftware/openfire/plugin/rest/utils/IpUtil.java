package org.jivesoftware.openfire.plugin.rest.utils;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

public class IpUtil {
	/**
	 * 获取当前浏览器端IP
	 * @param @param request
	 * @param @return
	 * @return String
	 * @throws
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request) {
		String ip = "";
		ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Forworded-For");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteHost();
		}
		if(ip != null && !"".equals(ip)){
			//#123.82.23.49, 117.27.153.185   
			//unknown, 116.112.66.102
			//10.15.7.61:48297
			ip = ip.replaceAll("#", "");
			ip = ip.trim();
			ip = ip.replaceAll(" ", "");
			ip = ip.toLowerCase();
			ip = ip.replaceAll("unknown,", "");
			for(String ipStr:ip.split(",")){
				if(ipStr != null && !"".equals(ipStr)){
					ip = ipStr;
					break;
				}
			}
			for(String ipStr:ip.split(":")){
				if(ipStr != null && !"".equals(ipStr)){
					ip = ipStr;
					break;
				}
			}
		}
		return ip;
	}
}
