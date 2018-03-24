package org.jivesoftware.openfire.plugin.rest.enums;

public enum PayTypeEnum {
	ALIPAY("支付宝"),WECHAT("微信"),NOCARD("银联快捷"),BALANCE("余额");
	public String title;
	private PayTypeEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
