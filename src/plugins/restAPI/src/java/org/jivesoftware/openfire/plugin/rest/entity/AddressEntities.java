package org.jivesoftware.openfire.plugin.rest.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addresses")
public class AddressEntities {
	private List<AddressEntity> addresses;
	public AddressEntities(){
		 
	}
	public AddressEntities(List<AddressEntity> addresses){
		this.addresses = addresses;
	}
	@XmlElement(name = "address")
	public List<AddressEntity> getAddresses() {
		return addresses;
	}
	public void setAddresses(List<AddressEntity> addresses) {
		this.addresses = addresses;
	}
	 
}
