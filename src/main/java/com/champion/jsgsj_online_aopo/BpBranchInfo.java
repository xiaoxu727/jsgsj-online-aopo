package com.champion.jsgsj_online_aopo;

public class BpBranchInfo {
	private int id = 0;
	private String uuid;
	private String certi_code;
	private String branch_name;
	private String reg_authority;
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

	public String getCerti_code() {
		return certi_code;
	}

	public void setCerti_code(String certi_code) {
		this.certi_code = certi_code;
	}

	public String getBranch_name() {
		return branch_name;
	}

	public void setBranch_name(String branch_name) {
		this.branch_name = branch_name;
	}

	public String getReg_authority() {
		return reg_authority;
	}

	public void setReg_authority(String reg_authority) {
		this.reg_authority = reg_authority;
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
		return "SHGSJBpBranchInfo [id=" + id + ", uuid=" + uuid + ", certi_code=" + certi_code + ", branch_name="
				+ branch_name + ", reg_authority=" + reg_authority + ", version=" + version + ", ts=" + ts + "]";
	}

}
