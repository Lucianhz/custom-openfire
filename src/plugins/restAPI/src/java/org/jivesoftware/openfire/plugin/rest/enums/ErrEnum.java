package org.jivesoftware.openfire.plugin.rest.enums;

public enum ErrEnum {
	
	ERR_SERVER_ERR("服务器内部异常","10000"),
	ERR_ROOMNAME_NULL("群标识为空","10001"),
	ERR_ROOMPIC_NULL("群头像为空","10002"),
	ERR_ROOMUSERREMARK_NULL("群用户备注为空","10003"),
	ERR_ROOMNUMBER_NULL("群号为空","10004"),
	ERR_JID_NULL("JID为空","10005"),
	ERR_ROOMID_NULL("群ID为空","10006"),
	ERR_ROOMUSERMAX_NULL("用户最大值为空","10007"),
	ERR_USERNAME_NULL("用户名为空","10008"),
	ERR_USER_NULL("用户不存在","10009"),
	ERR_MONEY_NULL("金额为空","10010"),
	ERR_BANKCODE_NULL("银行名称为空","10011"),
	ERR_PROVICE_NULL("省份为空","10012"),
	ERR_CITY_NULL("城市为空","10013"),
	ERR_BRANCHNAME_NULL("支行名称为空","10014"),
	ERR_CARDNO_NULL("银行卡号为空","10015"),
	ERR_ACCOUNTNAME_NULL("开户人名为空","10016"),
	ERR_MEMBERS_NULL("群成员为空","10017"),
	ERR_KEY_NULL("key为空","10018"),
	ERR_OWNERS_NULL("群主为空","10019"),
	ERR_NOT_ROLE("没有权限","10020"),
	ERR_IMAGECODE_ERR("验证码错误","10021"),
	ERR_REALNAME_EXISTS("用户已实名","10022"),
	ERR_REALNAME_NULL("真实姓名为空","10023"),
	ERR_PASSWORD_EXISTS("支付密码已存在","10024"),
	ERR_PASSWORD_NULL("密码为空","10025"),
	ERR_MOBILE_NULL("手机号码为空","10026"),
	ERR_NUMBER_NULL("短信验证码为空","10027"),
	ERR_NUMBER_ERR("短信验证码错误","10028"),
	ERR_COUNTRYID_NULL("国家ID为空","10029"),
	ERR_PROVINCEID_NULL("省份ID为空","10030"),
	ERR_PASSWORD_ERR("密码错误","10031"),
	ERR_BALANCE_ERR("余额不足","10032"),
	ERR_MOBILE_WBD("手机号码未绑定","10033"),
	ERR_USER_EXISTS("用户已存在","10034"),
	ERR_OPENID_NULL("用户已存在","10035"),
	ERR_THIRDTYPE_NULL("用户已存在","10036"),
	ERR_DEVICECODE_NULL("设备标识为空","10037"),
	ERR_LOGIN_ERR("设备标识为空","10038"),
	ERR_TYPE_NULL("类型为空","10039"),
	ERR_ROOM_EXISTS("该群已存在","10040"),
	ERR_ACCOUNT_YBD("该群已存在","100041"),
	ERR_SGIN_ERR("该群已存在","100042"),
	ERR_GAMEPROPERTY_NULL("群不存在或者未设置游戏规则","100043"),
	ERR_MINEBAN_NULL("禁雷号为空","100044"),
	ERR_SETTLENUMBER_NULL("结算号为空","100045"),
	ERR_RUSH_NULL("秒抢号为空","100046"),
	ERR_ODDS_NULL("中雷规则为空","100047"),
	ERR_USER_NOEXISTS("用户不存在","100048"),
	ERR_ROOM_ERR("群错误","100049"),
	ERR_STATUS_ERR("状态码错误","100050"),
	ERR_BEINVITED_NULL("被邀请人为空","100051"),
	ERR_ROOMVALIDATEID_NULL("群验证ID为空","100052"),
	ERR_ROOMVALIDATEID_ERR("群验证ID无效","100052"),
	ERR_ISVALIDATE_NULL("isValidate为空","100053"),
	ERR_REWARD_NULL("奖励规则不能为空","100054"),
	ERR_PRIZE_NULL("轮盘规则为空","100055"),
	ERR_PRIZE_ERR("轮盘规则错误","100055"),
	ERR_ROOMMAX_ERR("群人数超出最大值错误","100056"),
	ERR_PAGESIZE_NULL("页面大小为空","100057"),
	ERR_PAGENUM_NULL("页码为空","100058"),
	ERR_ISGAG_NULL("禁言状态为空","100059"),
	ERR_ISGAG_ERR("禁言设置失败","100060"),
	ERR_NICKNAME_NULL("昵称为空","100061"),
	ERR_MESSAGEBODY_NULL("消息内容为空","100062"),
	ERR_PIC_NULL("头像为空","100063")
	;
	
	
	public String msg;
	public String value;
	private ErrEnum(String msg,String value) {  
	   this.msg = msg;  
	   this.value=value;
	}
	public String getMsg() {
		return msg;
	}
	public String getValue() {
		return value;
	}
}
