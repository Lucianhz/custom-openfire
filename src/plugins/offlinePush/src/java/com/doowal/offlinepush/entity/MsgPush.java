package com.doowal.offlinepush.entity;

public class MsgPush {
	private String jid;
	private String jidfrom;
	private String roomfrom;
	private String msgpush;
	private String createdate;
	private String modifydate;
	
	
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	public String getJidfrom() {
		return jidfrom;
	}
	public void setJidfrom(String jidfrom) {
		this.jidfrom = jidfrom;
	}
	public String getRoomfrom() {
		return roomfrom;
	}
	public void setRoomfrom(String roomfrom) {
		this.roomfrom = roomfrom;
	}
	public String getMsgpush() {
		return msgpush;
	}
	public void setMsgpush(String msgpush) {
		this.msgpush = msgpush;
	}
	public String getCreatedate() {
		return createdate;
	}
	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}
	public String getModifydate() {
		return modifydate;
	}
	public void setModifydate(String modifydate) {
		this.modifydate = modifydate;
	}
	
}
