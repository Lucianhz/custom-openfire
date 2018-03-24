package org.jivesoftware.openfire.plugin.rest.enums;

public enum OrderStatusEnum {
	YZF("已支付"),WZF("未支付"),ZFZ("支付中"),YTK("已退款");
	public String title;
	private OrderStatusEnum(String title) {  
	   this.title = title;  
	}
	public String getTitle() {
		return title;
	} 
}
