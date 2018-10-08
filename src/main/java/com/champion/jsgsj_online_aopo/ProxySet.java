package com.champion.jsgsj_online_aopo;

public class ProxySet {
	private String host; // 主机名
	private String port; // 端口号
	private String user; // 用户名
	private String passwd; // 密码
	private String type; // 类型
	private String comment; // 说明
	private int delay_time = 0; // 延时
	private int borrow_num = 0; // 失败次数
	private String ts; // 验证时间

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getDelay_time() {
		return delay_time;
	}

	public void setDelay_time(int delay_time) {
		this.delay_time = delay_time;
	}

	public int getBorrow_num() {
		return borrow_num;
	}

	public void setBorrow_num(int borrow_num) {
		this.borrow_num = borrow_num;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	@Override
	public String toString() {
		return "ProxySet [host=" + host + ", port=" + port + ", user=" + user + ", passwd=" + passwd + ", type=" + type
				+ ", comment=" + comment + ", delay_time=" + delay_time + ", borrow_num=" + borrow_num + ", ts=" + ts
				+ "]";
	}

}
