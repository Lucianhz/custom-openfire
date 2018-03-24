package org.jivesoftware.openfire.plugin.rest.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "member")
public class MUCMemberEntity {
	private int roomID;
	private String jid;
	private String nickname;
	private String firstName;
	private String lastName;
	private String url;
	private String email;
	private String faqentry;
	private String remark;
	
	@XmlElement
	public int getRoomID() {
		return roomID;
	}
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}
	@XmlElement
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	@XmlElement
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFaqentry() {
		return faqentry;
	}
	public void setFaqentry(String faqentry) {
		this.faqentry = faqentry;
	}
	@XmlElement
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}