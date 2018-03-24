package org.jivesoftware.openfire.plugin.rest.entity;

import java.math.BigDecimal;

import org.jivesoftware.openfire.plugin.rest.enums.TradeStatusEnum;
import org.jivesoftware.openfire.plugin.rest.enums.TradeTypeEnum;

public class TradeRecordEntity {
	private Long Id;
	private String name;
	private String rechargeId;
	private String transferId;
	private String takeCashId;
	private String userName;
	private TradeStatusEnum tradeStatus;
	private BigDecimal money;
	private TradeTypeEnum tradeType;
	private String remark;
	private String createDate;
	private String modifyDate;

	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRechargeId() {
		return rechargeId;
	}
	public void setRechargeId(String rechargeId) {
		this.rechargeId = rechargeId;
	}
	public String getTransferId() {
		return transferId;
	}
	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}
	public String getTakeCashId() {
		return takeCashId;
	}
	public void setTakeCashId(String takeCashId) {
		this.takeCashId = takeCashId;
	}
	public TradeTypeEnum getTradeType() {
		return tradeType;
	}
	public void setTradeType(TradeTypeEnum tradeType) {
		this.tradeType = tradeType;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public TradeStatusEnum getTradeStatus() {
		return tradeStatus;
	}
	public void setTradeStatus(TradeStatusEnum tradeStatus) {
		this.tradeStatus = tradeStatus;
	}
	
}
