package org.jivesoftware.openfire.plugin.rest.enums;

public enum takeCashStatusEnum {
	SUCCESS("提现成功"),FAIL("提现失败"),CURRENT("提现中");
	public String title;
	private takeCashStatusEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
