package org.jivesoftware.openfire.plugin.rest.enums;

public enum TradeStatusEnum {
	SUCCESS("交易成功"),FAIL("交易失败"),CURRENT("交易中");
	public String title;
	private TradeStatusEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
