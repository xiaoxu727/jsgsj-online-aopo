package com.champion.jsgsj_online_aopo;

public class ComList {
	private String cname;
	private int search_type;
	private int status = 0;
	private String ts;
	private int page_view;

	public int getPage_view() {
		return page_view;
	}

	public void setPage_view(int page_view) {
		this.page_view = page_view;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public int getSearch_type() {
		return search_type;
	}

	public void setSearch_type(int search_type) {
		this.search_type = search_type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	@Override
	public String toString() {
		return "ComList [cname=" + cname + ", search_type=" + search_type + ", status=" + status + ", ts=" + ts + ", page_view=" + page_view + "]";
	}

}
