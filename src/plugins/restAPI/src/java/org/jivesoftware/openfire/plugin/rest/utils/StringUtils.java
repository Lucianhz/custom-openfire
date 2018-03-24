package org.jivesoftware.openfire.plugin.rest.utils;

public class StringUtils {
	// 1>判断字符串是否仅为数字:  
	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	// 2.判断一个字符串的首字符是否为字母
	public static boolean isEng(String s) {
		if(s.length()<6||s.length()>20){
			return false;
		}
		char c = s.charAt(0);
		int i = (int) c;
		if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println(isEng("A123"));

	}

}
