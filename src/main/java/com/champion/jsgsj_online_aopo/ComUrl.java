	package com.champion.jsgsj_online_aopo;

	public class ComUrl {
		private String uuid;
		private String corp_org;
		private String corp_id;
		private String corp_seq_id;
		private String pre_cname;
		private String cname;
		private String admit_main;
		private String reg_status;
		private String reg_code;
		private String uni_scid;
		private String pname;
		private String reg_date;
		private int part = 0;
		private int status = 0;
		private int version = 0;
		private String ts;
		private int page_view;


		public int getPage_view() {
			return page_view;
		}

		public void setPage_view(int page_view) {
			this.page_view = page_view;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public String getCorp_org() {
			return corp_org;
		}

		public void setCorp_org(String corp_org) {
			this.corp_org = corp_org;
		}

		public String getCorp_id() {
			return corp_id;
		}

		public void setCorp_id(String corp_id) {
			this.corp_id = corp_id;
		}

		public String getCorp_seq_id() {
			return corp_seq_id;
		}

		public void setCorp_seq_id(String corp_seq_id) {
			this.corp_seq_id = corp_seq_id;
		}

		public String getPre_cname() {
			return pre_cname;
		}

		public void setPre_cname(String pre_cname) {
			this.pre_cname = pre_cname;
		}

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		public String getAdmit_main() {
			return admit_main;
		}

		public void setAdmit_main(String admit_main) {
			this.admit_main = admit_main;
		}

		public String getReg_status() {
			return reg_status;
		}

		public void setReg_status(String reg_status) {
			this.reg_status = reg_status;
		}

		public String getReg_code() {
			return reg_code;
		}

		public void setReg_code(String reg_code) {
			this.reg_code = reg_code;
		}

		public String getUni_scid() {
			return uni_scid;
		}

		public void setUni_scid(String uni_scid) {
			this.uni_scid = uni_scid;
		}

		public String getPname() {
			return pname;
		}

		public void setPname(String pname) {
			this.pname = pname;
		}

		public String getReg_date() {
			return reg_date;
		}

		public void setReg_date(String reg_date) {
			this.reg_date = reg_date;
		}

		public int getPart() {
			return part;
		}

		public void setPart(int part) {
			this.part = part;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
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
			return "ComUrl [uuid=" + uuid + ", corp_org=" + corp_org + ", corp_id=" + corp_id + ", corp_seq_id="
					+ corp_seq_id + ", pre_cname=" + pre_cname + ", cname=" + cname + ", admit_main=" + admit_main
					+ ", reg_status=" + reg_status + ", reg_code=" + reg_code + ", uni_scid=" + uni_scid + ", pname="
					+ pname + ", reg_date=" + reg_date + ", part=" + part + ", status=" + status + ", version=" + version
					+ "]";
		}

	}
