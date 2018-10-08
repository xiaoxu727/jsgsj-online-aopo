package com.champion.jsgsj_online_aopo;

public class BpClearInfo {
	private int id = 0;
	private String uuid;
	private String account_man;
	private String account_member;
	private int version = 0;
	private String ts;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAccount_man() {
		return account_man;
	}

	public void setAccount_man(String account_man) {
		this.account_man = account_man;
	}

	public String getAccount_member() {
		return account_member;
	}

	public void setAccount_member(String account_member) {
		this.account_member = account_member;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	@Override
	public String toString() {
		return "SHGSJBpClearInfo [id=" + id + ", uuid=" + uuid + ", account_man=" + account_man + ", account_member="
				+ account_member + ", version=" + version + ", ts=" + ts + "]";
	}

}
