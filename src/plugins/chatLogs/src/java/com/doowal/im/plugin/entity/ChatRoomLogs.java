package com.doowal.im.plugin.entity;

public class ChatRoomLogs {
	private Long id; 		   //自增ID
	private String messageId;  //消息id
    private String sender;	   //消息发送者
    private String receiver;   //接受者
    private String createDate; //消息发送、创建时间
    private String content;	   //消息内容
    private String detail;	   //消息源报文
    
	public long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
    
}
