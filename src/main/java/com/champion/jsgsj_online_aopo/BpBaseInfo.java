package com.champion.jsgsj_online_aopo;

public class BpBaseInfo {
	private int id = 0;
	private String uuid;
	private String cname;
	private String reg_code;
	private String reg_type;
	private String pname;
	private String reg_capi;
	private String reg_capi_type;
	private String compose_form;
	private String reg_date;
	private String address;
	private String business_date_from;
	private String business_date_to;
	private String business_scope;
	private String reg_authority;
	private String approval_date;
	private String reg_status;
	private String writeoff_date;
	private String revoke_date;
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
	
	public String getCname() {
		return cname;
	}
	
	public void setCname(String cname) {
		this.cname = cname;
	}
	
	public String getReg_code() {
		return reg_code;
	}
	
	public void setReg_code(String reg_code) {
		this.reg_code = reg_code;
	}
	
	public String getReg_type() {
		return reg_type;
	}
	
	public void setReg_type(String reg_type) {
		this.reg_type = reg_type;
	}
	
	public String getPname() {
		return pname;
	}
	
	public void setPname(String pname) {
		this.pname = pname;
	}
	
	public String getReg_capi() {
		return reg_capi;
	}
	
	public void setReg_capi(String reg_capi) {
		this.reg_capi = reg_capi;
	}
	
	public String getReg_capi_type() {
		return reg_capi_type;
	}
	
	public void setReg_capi_type(String reg_capi_type) {
		this.reg_capi_type = reg_capi_type;
	}
	
	public String getCompose_form() {
		return compose_form;
	}
	
	public void setCompose_form(String compose_form) {
		this.compose_form = compose_form;
	}
	
	public String getReg_date() {
		return reg_date;
	}
	
	public void setReg_date(String reg_date) {
		this.reg_date = reg_date;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getBusiness_date_from() {
		return business_date_from;
	}
	
	public void setBusiness_date_from(String business_date_from) {
		this.business_date_from = business_date_from;
	}
	
	public String getBusiness_date_to() {
		return business_date_to;
	}
	
	public void setBusiness_date_to(String business_date_to) {
		this.business_date_to = business_date_to;
	}
	
	public String getBusiness_scope() {
		return business_scope;
	}
	
	public void setBusiness_scope(String business_scope) {
		this.business_scope = business_scope;
	}
	
	public String getReg_authority() {
		return reg_authority;
	}
	
	public void setReg_authority(String reg_authority) {
		this.reg_authority = reg_authority;
	}
	
	public String getApproval_date() {
		return approval_date;
	}
	
	public void setApproval_date(String approval_date) {
		this.approval_date = approval_date;
	}
	
	public String getReg_status() {
		return reg_status;
	}
	
	public void setReg_status(String reg_status) {
		this.reg_status = reg_status;
	}
	
	public String getWriteoff_date() {
		return writeoff_date;
	}
	
	public void setWriteoff_date(String writeoff_date) {
		this.writeoff_date = writeoff_date;
	}
	
	public String getRevoke_date() {
		return revoke_date;
	}
	
	public void setRevoke_date(String revoke_date) {
		this.revoke_date = revoke_date;
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
		return "SHGSJBpBaseInfo [id=" + id + ", uuid=" + uuid + ", cname=" + cname + ", reg_code=" + reg_code
				+ ", reg_type=" + reg_type + ", pname=" + pname + ", reg_capi=" + reg_capi + ", reg_capi_type="
				+ reg_capi_type + ", compose_form=" + compose_form + ", reg_date=" + reg_date + ", address=" + address
				+ ", business_date_from=" + business_date_from + ", business_date_to=" + business_date_to
				+ ", business_scope=" + business_scope + ", reg_authority=" + reg_authority + ", approval_date="
				+ approval_date + ", reg_status=" + reg_status + ", writeoff_date=" + writeoff_date + ", revoke_date="
				+ revoke_date + ", version=" + version + ", ts=" + ts + "]";
	}
	
}
