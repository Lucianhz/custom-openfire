package org.jivesoftware.openfire.plugin.rest.enums;

public enum RechargeStatusEnum {
	SUCCESS("充值成功"),FAIL("充值失败"),CURRENT("充值中");
	public String title;
	private RechargeStatusEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
