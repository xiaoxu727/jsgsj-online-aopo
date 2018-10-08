package com.champion.jsgsj_online_aopo;

public class BpInvestPaid {
	private int id = 0;
	private String uuid;
	private String detail_url;
	private String paid_mode;
	private String paid_amount;
	private String paid_date;
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

	public String getDetail_url() {
		return detail_url;
	}

	public void setDetail_url(String detail_url) {
		this.detail_url = detail_url;
	}

	public String getPaid_mode() {
		return paid_mode;
	}

	public void setPaid_mode(String paid_mode) {
		this.paid_mode = paid_mode;
	}

	public String getPaid_amount() {
		return paid_amount;
	}

	public void setPaid_amount(String paid_amount) {
		this.paid_amount = paid_amount;
	}

	public String getPaid_date() {
		return paid_date;
	}

	public void setPaid_date(String paid_date) {
		this.paid_date = paid_date;
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
		return "SHGSJBpInvestPaid [id=" + id + ", uuid=" + uuid + ", detail_url=" + detail_url + ", paid_mode="
				+ paid_mode + ", paid_amount=" + paid_amount + ", paid_date=" + paid_date + ", version=" + version
				+ ", ts=" + ts + "]";
	}

}
