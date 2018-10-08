package com.champion.jsgsj_online_aopo;

public class BpInvestInfo {
	private int id = 0;
	private String uuid;
	private String rn;
	private String share_holder_type;
	private String pname;
	private String certi_type;
	private String certi_code;
	private String detail_url;
	private int part = 0;
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

	public String getRn() {
		return rn;
	}

	public void setRn(String rn) {
		this.rn = rn;
	}

	public String getShare_holder_type() {
		return share_holder_type;
	}

	public void setShare_holder_type(String share_holder_type) {
		this.share_holder_type = share_holder_type;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public String getCerti_type() {
		return certi_type;
	}

	public void setCerti_type(String certi_type) {
		this.certi_type = certi_type;
	}

	public String getCerti_code() {
		return certi_code;
	}

	public void setCerti_code(String certi_code) {
		this.certi_code = certi_code;
	}

	public String getDetail_url() {
		return detail_url;
	}

	public void setDetail_url(String detail_url) {
		this.detail_url = detail_url;
	}

	public int getPart() {
		return part;
	}

	public void setPart(int part) {
		this.part = part;
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
		return "SHGSJBpInvestInfo [id=" + id + ", uuid=" + uuid + ", rn=" + rn + ", share_holder_type="
				+ share_holder_type + ", pname=" + pname + ", certi_type=" + certi_type + ", certi_code=" + certi_code
				+ ", detail_url=" + detail_url + ", part=" + part + ", version=" + version + ", ts=" + ts + "]";
	}
}
