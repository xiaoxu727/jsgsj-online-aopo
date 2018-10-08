package com.champion.jsgsj_online_aopo;

public class BpInvestSubscribed {
	private int id = 0;
	private String uuid;
	private String detail_url;
	private String subscribed_mode;
	private String subscribed_amount;
	private String subscribed_date;
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

	public String getSubscribed_mode() {
		return subscribed_mode;
	}

	public void setSubscribed_mode(String subscribed_mode) {
		this.subscribed_mode = subscribed_mode;
	}

	public String getSubscribed_amount() {
		return subscribed_amount;
	}

	public void setSubscribed_amount(String subscribed_amount) {
		this.subscribed_amount = subscribed_amount;
	}

	public String getSubscribed_date() {
		return subscribed_date;
	}

	public void setSubscribed_date(String subscribed_date) {
		this.subscribed_date = subscribed_date;
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
		return "SHGSJBpInvestSubscribed [id=" + id + ", uuid=" + uuid + ", detail_url=" + detail_url
				+ ", subscribed_mode=" + subscribed_mode + ", subscribed_amount=" + subscribed_amount
				+ ", subscribed_date=" + subscribed_date + ", version=" + version + ", ts=" + ts + "]";
	}

}
