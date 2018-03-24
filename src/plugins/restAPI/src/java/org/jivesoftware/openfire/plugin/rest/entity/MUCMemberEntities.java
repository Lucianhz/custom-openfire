package org.jivesoftware.openfire.plugin.rest.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "members")
public class MUCMemberEntities {
	List<MUCMemberEntity> mucMembers;

	public MUCMemberEntities() {
	}

	public MUCMemberEntities(List<MUCMemberEntity> mucMembers) {
		this.mucMembers = mucMembers;
	}

	@XmlElement(name = "member")
	public List<MUCMemberEntity> getMucMembers() {
		return mucMembers;
	}

	public void setMucMembers(List<MUCMemberEntity> mucMembers) {
		this.mucMembers = mucMembers;
	}
}
