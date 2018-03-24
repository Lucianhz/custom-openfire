package org.jivesoftware.openfire.plugin.rest.test;

public class Test2 {

	public static void main(String[] args) {
		String s  = "0.08";
		if(!s.contains(".")){
			s = Double.parseDouble(s)/100+"";
		}
		System.out.println(s);
	}

}
