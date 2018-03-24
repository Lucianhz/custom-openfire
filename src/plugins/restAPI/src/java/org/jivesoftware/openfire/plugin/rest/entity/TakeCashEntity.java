package org.jivesoftware.openfire.plugin.rest.entity;

import java.math.BigDecimal;

import org.jivesoftware.openfire.plugin.rest.enums.takeCashStatusEnum;

public class TakeCashEntity {
	private Long id;
	private String takeCashNo;
	private String userName;
	private String cardNo;
	private BigDecimal money;
	private takeCashStatusEnum takeCashStatus;
	private BigDecimal balance;
	private String remark;
	private String createDate;
	private String modifyDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTakeCashNo() {
		return takeCashNo;
	}
	public void setTakeCashNo(String takeCashNo) {
		this.takeCashNo = takeCashNo;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	public takeCashStatusEnum getTakeCashStatus() {
		return takeCashStatus;
	}
	public void setTakeCashStatus(takeCashStatusEnum takeCashStatus) {
		this.takeCashStatus = takeCashStatus;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	
}
