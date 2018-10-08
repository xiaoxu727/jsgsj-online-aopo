package com.champion.jsgsj_online_aopo;

public class BpExceptionInfo {
	private int id = 0;
	private String uuid;
	private String rn;
	private String include_reason;
	private String include_date;
	private String include_authority;
	private String exclude_reason;
	private String exclude_date;
	private String exclude_authority;
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

	public String getInclude_reason() {
		return include_reason;
	}

	public void setInclude_reason(String include_reason) {
		this.include_reason = include_reason;
	}

	public String getInclude_date() {
		return include_date;
	}

	public void setInclude_date(String include_date) {
		this.include_date = include_date;
	}

	public String getInclude_authority() {
		return include_authority;
	}

	public void setInclude_authority(String include_authority) {
		this.include_authority = include_authority;
	}

	public String getExclude_reason() {
		return exclude_reason;
	}

	public void setExclude_reason(String exclude_reason) {
		this.exclude_reason = exclude_reason;
	}

	public String getExclude_date() {
		return exclude_date;
	}

	public void setExclude_date(String exclude_date) {
		this.exclude_date = exclude_date;
	}

	public String getExclude_authority() {
		return exclude_authority;
	}

	public void setExclude_authority(String exclude_authority) {
		this.exclude_authority = exclude_authority;
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
		return "SHGSJBpExceptionInfo [id=" + id + ", uuid=" + uuid + ", rn=" + rn + ", include_reason=" + include_reason
				+ ", include_date=" + include_date + ", include_authority=" + include_authority + ", exclude_reason="
				+ exclude_reason + ", exclude_date=" + exclude_date + ", exclude_authority=" + exclude_authority
				+ ", version=" + version + ", ts=" + ts + "]";
	}

}
