package org.jivesoftware.openfire.plugin.rest.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "address")
public class AddressEntity {
	private long id;
	private String username;
	private String consignee;
	private String region;
	private String detaileAddress;
	private String postcode;
	private String createDate;
	private String modifyDate;
	
	@XmlElement
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@XmlElement
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@XmlElement
	public String getConsignee() {
		return consignee;
	}
	public void setConsignee(String consignee) {
		this.consignee = consignee;
	}
	@XmlElement
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	@XmlElement
	public String getDetaileAddress() {
		return detaileAddress;
	}
	public void setDetaileAddress(String detaileAddress) {
		this.detaileAddress = detaileAddress;
	}
	@XmlElement
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	@XmlElement
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	@XmlElement
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
}
