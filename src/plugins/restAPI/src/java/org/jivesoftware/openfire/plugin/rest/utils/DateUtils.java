package org.jivesoftware.openfire.plugin.rest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String format(Date date){
		if(date==null) return null;
		String dateNew = sdf.format(date);
		return dateNew;
	}
	public static String formats(String format,Date date){
		if(format==null) return null;
 		if(date==null) return null;
 		
		String dateNew = new SimpleDateFormat(format).format(date);
		return dateNew.toString();
	}
	public static Date timeFormat(String time){
		if(time!=null&&!"".equals(time)){
			Long timeNew = new Long(time.substring(1));
			try {
				return sdf.parse(sdf.format(timeNew));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public static String timeFormat2(String time){
		if(time!=null&&!"".equals(time)){
			Long timeNew = new Long(time);
			return sdf.format(new Date(timeNew));
		}
		return null;
	}
}
