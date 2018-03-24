package org.jivesoftware.openfire.plugin.rest.enums;

public enum TradeTypeEnum {
	RED("红包"),ZHZH("转账"),CHZH("充值"),TX("提现");
	public String title;
	private TradeTypeEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
