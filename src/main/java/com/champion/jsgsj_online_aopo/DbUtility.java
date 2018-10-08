package com.champion.jsgsj_online_aopo;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.champion.webspider.utils.CommUtils;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class DbUtility {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public final static String Driver = "com.mysql.jdbc.Driver";
	public static String Conn = "jdbc:mysql://192.168.25.58:3306/jsgsj-online-aopo?characterEncoding=utf8&useSSL=true";
	public static String User = "root";
	public static String Passwd = "champion";

	public List<ComList> getComList(String t_company_list, boolean isFirst) {

		Connection conn = null;
		PreparedStatement pstmtQuery = null;
		PreparedStatement pstmtUpdate = null;

		List<ComList> lstComList = new ArrayList<ComList>();

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String querySql = "";
			if (isFirst) {
				querySql = "select * from " + t_company_list + " where status=0";
			} else {
				querySql = "select * from " + t_company_list + " where status=2";
			}
			String updateSql = " update " + t_company_list + " set status=?,ts=? where cname=? ";

			// 获取公司名录
			logger.info("获取" + t_company_list + "开始 >>>>>>>");
			pstmtQuery = (PreparedStatement) conn.prepareStatement(querySql);
			ResultSet rs = pstmtQuery.executeQuery();

			while (rs.next()) {
				ComList comList = new ComList();
				comList.setCname(rs.getString("cname"));
				comList.setSearch_type(Integer.valueOf(rs.getString("search_type")));
				comList.setStatus(Integer.valueOf(rs.getString("status")));
				comList.setTs(rs.getString("ts"));

				lstComList.add(comList);

				logger.info(t_company_list + ">>>>>>" + comList.toString());
			}
			logger.info("获取" + t_company_list + "结束 <<<<<<<");

			// 更新获取的公司名录状态
			pstmtUpdate = (PreparedStatement) conn.prepareStatement(updateSql);
			for (ComList comList : lstComList) {
				if (comList.getStatus() == 0) {
					pstmtUpdate.setString(1, "2");
					pstmtUpdate.setString(2, comList.getTs());
					pstmtUpdate.setString(3, comList.getCname());

					pstmtUpdate.executeUpdate();
				} else {
					comList.setStatus(0);
				}
			}
			conn.commit();// 事务提交

		} catch (Exception e) {
			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			return null;

		} finally {
			if (pstmtQuery != null) {
				try {
					pstmtQuery.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmtUpdate != null) {
				try {
					pstmtUpdate.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return lstComList;
	}
	public List<ComUrl> getComUrl(String t_company_url, boolean isFirst) {

		Connection conn = null;
		PreparedStatement pstmtQuery = null;
		PreparedStatement pstmtUpdate = null;

		List<ComUrl> lstComUrl = new ArrayList<ComUrl>();

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String querySql = "select * from " + t_company_url + " where status=0 ";
			if (isFirst) {
				querySql = "select * from " + t_company_url + " where status=0";
			} else {
				//	status = 2 状态大于300s就视为无效
//				querySql = "select * from " + t_company_url + " where status=2 and TIMESTAMPDIFF(SECOND, update_ts, now()) < 300";
				querySql = "select * from " + t_company_url + " where status=2";
			}
			String updateSql = " update " + t_company_url + " set status=?,ts=? where uuid=? ";

			// 获取公司url
			logger.info("获取" + t_company_url + "开始 >>>>>>>");
			pstmtQuery = (PreparedStatement) conn.prepareStatement(querySql);
			ResultSet rs = pstmtQuery.executeQuery();

			while (rs.next()) {
				ComUrl comUrl = new ComUrl();
				comUrl.setUuid(rs.getString("uuid"));
				comUrl.setCorp_org(rs.getString("corp_org"));
				comUrl.setCorp_id(rs.getString("corp_id"));
				comUrl.setCorp_seq_id(rs.getString("corp_seq_id"));
				comUrl.setPre_cname(rs.getString("pre_cname"));
				comUrl.setCname(rs.getString("cname"));
				comUrl.setAdmit_main(rs.getString("admit_main"));
				comUrl.setReg_status(rs.getString("reg_status"));
				comUrl.setReg_code(rs.getString("reg_code"));
				comUrl.setUni_scid(rs.getString("uni_scid"));
				comUrl.setPname(rs.getString("pname"));
				comUrl.setReg_date(rs.getString("reg_date"));
				comUrl.setPart(Integer.valueOf(rs.getString("part")));
				comUrl.setStatus(Integer.valueOf(rs.getString("status")));
				comUrl.setVersion(Integer.valueOf(rs.getString("version")));
				comUrl.setTs(rs.getString("ts"));
				lstComUrl.add(comUrl);

				logger.info(t_company_url + ">>>>>>" + comUrl.toString());
			}
			logger.info("获取" + t_company_url + "结束 <<<<<<<");

			// 更新获取的公司url状态
			pstmtUpdate = (PreparedStatement) conn.prepareStatement(updateSql);
			for (ComUrl comUrl : lstComUrl) {
				if (comUrl.getStatus() == 0) {
					comUrl.setStatus(2);
					pstmtUpdate.setString(1, String.valueOf(comUrl.getStatus()));
					pstmtUpdate.setString(2, comUrl.getTs());
					pstmtUpdate.setString(3, comUrl.getUuid());

					pstmtUpdate.executeUpdate();
				}
			}

			conn.commit();// 事务提交

		} catch (Exception e) {
			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			return null;

		} finally {
			if (pstmtQuery != null) {
				try {
					pstmtQuery.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmtUpdate != null) {
				try {
					pstmtUpdate.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return lstComUrl;
	}

	public ComUrl getJSGSJComUrlByUuid(String t_company_url, String uuid) {
		ComUrl comUrl = null;

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String querySql = " select * from " + t_company_url + " where uuid='" + uuid + "'";

			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(querySql);

			logger.info("获取" + t_company_url + "开始 >>>>>>>");

			try {
				ResultSet rs = pstmt.executeQuery();

				while (rs.next()) {
					comUrl = new ComUrl();
					comUrl.setUuid(rs.getString("uuid"));
					comUrl.setCorp_org(rs.getString("corp_org"));
					comUrl.setCorp_id(rs.getString("corp_id"));
					comUrl.setCorp_seq_id(rs.getString("corp_seq_id"));
					comUrl.setPre_cname(rs.getString("pre_cname"));
					comUrl.setCname(rs.getString("cname"));
					comUrl.setAdmit_main(rs.getString("admit_main"));
					comUrl.setReg_status(rs.getString("reg_status"));
					comUrl.setReg_code(rs.getString("reg_code"));
					comUrl.setUni_scid(rs.getString("uni_scid"));
					comUrl.setPname(rs.getString("pname"));
					comUrl.setReg_date(rs.getString("reg_date"));
					comUrl.setPart(Integer.valueOf(rs.getString("part")));
					comUrl.setStatus(Integer.valueOf(rs.getString("status")));
					comUrl.setVersion(Integer.valueOf(rs.getString("version")));
					comUrl.setTs(rs.getString("ts"));

					logger.info(t_company_url + ">>>>>>" + comUrl.toString());
				}
				logger.info("获取" + t_company_url + "结束 <<<<<<<");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return comUrl;
	}

	public List<BpInvestInfo> getBpInvestInfoList() {

		List<BpInvestInfo> bpInvestInfoList = new ArrayList<BpInvestInfo>();

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String querySql = " select * from bp_com_invest_info "
					+ " where detail_url is not null and detail_url !='' and part<2 ";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(querySql);

			logger.info("获取bp_com_invest_info开始 >>>>>>>");

			try {
				ResultSet rs = pstmt.executeQuery();

				while (rs.next()) {
					BpInvestInfo bpInvestInfo = new BpInvestInfo();
					bpInvestInfo.setId(Integer.valueOf(rs.getString("id")));
					bpInvestInfo.setUuid(rs.getString("uuid"));
					bpInvestInfo.setRn(rs.getString("rn"));
					bpInvestInfo.setShare_holder_type(rs.getString("share_holder_type"));
					bpInvestInfo.setPname(rs.getString("pname"));
					bpInvestInfo.setCerti_type(rs.getString("certi_type"));
					bpInvestInfo.setCerti_code(rs.getString("certi_code"));
					bpInvestInfo.setDetail_url(rs.getString("detail_url"));
					bpInvestInfo.setPart(Integer.valueOf(rs.getString("part")));
					bpInvestInfo.setVersion(Integer.valueOf(rs.getString("version")));
					bpInvestInfo.setTs(rs.getString("ts"));
					bpInvestInfoList.add(bpInvestInfo);

					logger.info("bp_com_invest_info>>>>>>" + bpInvestInfo.getDetail_url());
				}
				logger.info("获取bp_com_invest_info" + "结束 <<<<<<<");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bpInvestInfoList;
	}

	public List<BpInvestInfo> getBpInvestInfoList(ComUrl comUrl) {

		List<BpInvestInfo> bpInvestInfoList = new ArrayList<BpInvestInfo>();

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String querySql = " select * from bp_com_invest_info "
					+ " where detail_url is not null and detail_url !='' and part<2 and uuid='" + comUrl.getUuid()
					+ "'";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(querySql);

			logger.info("获取bp_com_invest_info开始 >>>>>>>" + comUrl.toString());

			try {
				ResultSet rs = pstmt.executeQuery();

				while (rs.next()) {
					BpInvestInfo bpInvestInfo = new BpInvestInfo();
					bpInvestInfo.setId(Integer.valueOf(rs.getString("id")));
					bpInvestInfo.setUuid(rs.getString("uuid"));
					bpInvestInfo.setRn(rs.getString("rn"));
					bpInvestInfo.setShare_holder_type(rs.getString("share_holder_type"));
					bpInvestInfo.setPname(rs.getString("pname"));
					bpInvestInfo.setCerti_type(rs.getString("certi_type"));
					bpInvestInfo.setCerti_code(rs.getString("certi_code"));
					bpInvestInfo.setDetail_url(rs.getString("detail_url"));
					bpInvestInfo.setPart(Integer.valueOf(rs.getString("part")));
					bpInvestInfo.setVersion(Integer.valueOf(rs.getString("version")));
					bpInvestInfo.setTs(rs.getString("ts"));
					bpInvestInfoList.add(bpInvestInfo);

					logger.info("bp_com_invest_info>>>>>>" + bpInvestInfo.getDetail_url());
				}
				logger.info("获取bp_com_invest_info" + "结束 <<<<<<<");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bpInvestInfoList;
	}

	public int setComListByCname(String t_company_list, ComList comList) {
		int result = 0;

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String sql = " update " + t_company_list + " set status=?,ts=? where cname=? ";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql);

			pstmt.setString(1, String.valueOf(comList.getStatus()));
			pstmt.setString(2, comList.getTs());
			pstmt.setString(3, comList.getCname());

			try {
				pstmt.executeUpdate();
				result = 1;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			logger.info(t_company_list + "设置公司名录参数status成功!>>>>>>" + comList.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int setComUrlByUuid(String t_company_url, ComUrl comUrl) {
		int result = 0;

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String sql = " update " + t_company_url
					+ " set corp_org=?,corp_id=?,corp_seq_id=?,pre_cname=?,cname=?,admit_main=?,reg_status=?,"
					+ " reg_code=?,uni_scid=?,pname=?,reg_date=?,part=?,status=?,version=?,ts=? where uuid=?";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql);

			pstmt.setString(1, comUrl.getCorp_org());
			pstmt.setString(2, comUrl.getCorp_id());
			pstmt.setString(3, comUrl.getCorp_seq_id());
			pstmt.setString(4, comUrl.getPre_cname());
			pstmt.setString(5, comUrl.getCname());
			pstmt.setString(6, comUrl.getAdmit_main());
			pstmt.setString(7, comUrl.getReg_status());
			pstmt.setString(8, comUrl.getReg_code());
			pstmt.setString(9, comUrl.getUni_scid());
			pstmt.setString(10, comUrl.getPname());
			pstmt.setString(11, comUrl.getReg_date());
			pstmt.setString(12, String.valueOf(comUrl.getPart()));
			pstmt.setString(13, String.valueOf(comUrl.getStatus()));
			pstmt.setString(14, String.valueOf(comUrl.getVersion()));
			pstmt.setString(15, comUrl.getTs());
			pstmt.setString(16, comUrl.getUuid());

			try {
				pstmt.executeUpdate();
				result = 1;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			logger.info(t_company_url + "设置公司url参数成功!>>>>>>" + comUrl.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int setBpInvestInfo(BpInvestInfo bpInvestInfo) {
		int result = 0;
		String tableName = "bp_com_invest_info";

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String sql = " update " + tableName + " set uuid=?,share_holder_type=?,pname=?,certi_type=?,certi_code=?,"
					+ " detail_url=?,part=?,version=?,ts=? where id=?";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(sql);

			pstmt.setString(1, bpInvestInfo.getUuid());
			pstmt.setString(2, bpInvestInfo.getShare_holder_type());
			pstmt.setString(3, bpInvestInfo.getPname());
			pstmt.setString(4, bpInvestInfo.getCerti_type());
			pstmt.setString(5, bpInvestInfo.getCerti_code());
			pstmt.setString(6, bpInvestInfo.getDetail_url());
			pstmt.setString(7, String.valueOf(bpInvestInfo.getPart()));
			pstmt.setString(8, String.valueOf(bpInvestInfo.getVersion()));
			pstmt.setString(9, bpInvestInfo.getTs());
			pstmt.setString(10, String.valueOf(bpInvestInfo.getId()));

			try {
				pstmt.executeUpdate();
				result = 1;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			logger.info("设置股东信息part参数成功!>>>>>>" + bpInvestInfo.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public int saveComUrlList(String t_company_url, List<ComUrl> comUrlList) {
		int result = 0;

		Connection conn = null;
		PreparedStatement pstmtInsert = null;
		PreparedStatement pstmtUpdate = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String insertSql = " insert into " + t_company_url
					+ " (uuid,corp_org,corp_id,corp_seq_id,pre_cname,cname,admit_main,reg_status,reg_code,uni_scid,"
					+ " pname,reg_date,part,status,version,ts) " + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String updateSql = " update " + t_company_url
					+ " set corp_org=?,corp_id=?,corp_seq_id=?,pre_cname=?,cname=?,admit_main=?,reg_status=?,"
					+ " reg_code=?,uni_scid=?,pname=?,reg_date=?,part=?,status=?,version=?,ts=? where uuid=?";

			pstmtInsert = (PreparedStatement) conn.prepareStatement(insertSql);
			pstmtUpdate = (PreparedStatement) conn.prepareStatement(updateSql);

			for (ComUrl comUrl : comUrlList) {
				ComUrl comUrlOld = getJSGSJComUrlByUuid(t_company_url, comUrl.getUuid());
				if (comUrlOld != null) {
					// 获取版本信息
					comUrl.setVersion(comUrlOld.getVersion());

					// 已经存在，更新
					pstmtUpdate.setString(1, comUrl.getCorp_org());
					pstmtUpdate.setString(2, comUrl.getCorp_id());
					pstmtUpdate.setString(3, comUrl.getCorp_seq_id());
					pstmtUpdate.setString(4, comUrl.getPre_cname());
					pstmtUpdate.setString(5, comUrl.getCname());
					pstmtUpdate.setString(6, comUrl.getAdmit_main());
					pstmtUpdate.setString(7, comUrl.getReg_status());
					pstmtUpdate.setString(8, comUrl.getReg_code());
					pstmtUpdate.setString(9, comUrl.getUni_scid());
					pstmtUpdate.setString(10, comUrl.getPname());
					pstmtUpdate.setString(11, comUrl.getReg_date());
					pstmtUpdate.setString(12, String.valueOf(comUrl.getPart()));
					pstmtUpdate.setString(13, String.valueOf(comUrl.getStatus()));
					pstmtUpdate.setString(14, String.valueOf(comUrl.getVersion()));
					pstmtUpdate.setString(15, comUrl.getTs());
					pstmtUpdate.setString(16, comUrl.getUuid());

					pstmtUpdate.executeUpdate();

				} else {
					pstmtInsert.setString(1, comUrl.getUuid());
					pstmtInsert.setString(2, comUrl.getCorp_org());
					pstmtInsert.setString(3, comUrl.getCorp_id());
					pstmtInsert.setString(4, comUrl.getCorp_seq_id());
					pstmtInsert.setString(5, comUrl.getPre_cname());
					pstmtInsert.setString(6, comUrl.getCname());
					pstmtInsert.setString(7, comUrl.getAdmit_main());
					pstmtInsert.setString(8, comUrl.getReg_status());
					pstmtInsert.setString(9, comUrl.getReg_code());
					pstmtInsert.setString(10, comUrl.getUni_scid());
					pstmtInsert.setString(11, comUrl.getPname());
					pstmtInsert.setString(12, comUrl.getReg_date());
					pstmtInsert.setString(13, String.valueOf(comUrl.getPart()));
					pstmtInsert.setString(14, String.valueOf(comUrl.getStatus()));
					pstmtInsert.setString(15, String.valueOf(comUrl.getVersion()));
					pstmtInsert.setString(16, comUrl.getTs());

					pstmtInsert.executeUpdate();
				}
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info(">>>>>>>开始保存数据到表：" + t_company_url);

		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate entry")) {
				result = 1;
			} else {
				result = 0;
				e.printStackTrace();
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}

		} finally {
			if (pstmtInsert != null) {
				try {
					pstmtInsert.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmtUpdate != null) {
				try {
					pstmtUpdate.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public int saveBpBaseInfo(BpBaseInfo bpBaseInfo) {
		int result = 0;
		String tableName = "bp_com_base_info";

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String insertSql = " insert into " + tableName
					+ " (uuid,cname,reg_code,reg_type,pname,reg_capi,reg_capi_type,compose_form,reg_date,address,"
					+ " business_date_from,business_date_to,business_scope,reg_authority,approval_date,reg_status,"
					+ " writeoff_date,revoke_date,version,ts)" + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			pstmt.setString(1, bpBaseInfo.getUuid());
			pstmt.setString(2, bpBaseInfo.getCname());
			pstmt.setString(3, bpBaseInfo.getReg_code());
			pstmt.setString(4, bpBaseInfo.getReg_type());
			pstmt.setString(5, bpBaseInfo.getPname());
			pstmt.setString(6, bpBaseInfo.getReg_capi());
			pstmt.setString(7, bpBaseInfo.getReg_capi_type());
			pstmt.setString(8, bpBaseInfo.getCompose_form());
			pstmt.setString(9, bpBaseInfo.getReg_date());
			pstmt.setString(10, bpBaseInfo.getAddress());
			pstmt.setString(11, bpBaseInfo.getBusiness_date_from());
			pstmt.setString(12, bpBaseInfo.getBusiness_date_to());
			pstmt.setString(13, bpBaseInfo.getBusiness_scope());
			pstmt.setString(14, bpBaseInfo.getReg_authority());
			pstmt.setString(15, bpBaseInfo.getApproval_date());
			pstmt.setString(16, bpBaseInfo.getReg_status());
			pstmt.setString(17, bpBaseInfo.getWriteoff_date());
			pstmt.setString(18, bpBaseInfo.getRevoke_date());
			pstmt.setString(19, String.valueOf(bpBaseInfo.getVersion()));
			pstmt.setString(20, bpBaseInfo.getTs());

			logger.info(">>>>>>>开始保存数据到表：" + tableName);

			try {
				if (1 == pstmt.executeUpdate())
					result++;

			} catch (Exception e) {
				e.printStackTrace();
				CommUtils.SaveError(bpBaseInfo.toString() + ">>>>>>\n" + e.getMessage());

			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}

			logger.info("保存!>>>>>>" + tableName + ":" + bpBaseInfo.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int saveBpInvestInfoList(List<BpInvestInfo> lstBpInvestInfo) {
		int result = 0;
		String tableName = "bp_com_invest_info";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String insertSql = " insert into " + tableName
					+ " (uuid,rn,share_holder_type,pname,certi_type,certi_code,detail_url,version,ts) "
					+ " values(?,?,?,?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpInvestInfo bpInvestInfo : lstBpInvestInfo) {
				pstmt.setString(1, bpInvestInfo.getUuid());
				pstmt.setString(2, bpInvestInfo.getRn());
				pstmt.setString(3, bpInvestInfo.getShare_holder_type());
				pstmt.setString(4, bpInvestInfo.getPname());
				pstmt.setString(5, bpInvestInfo.getCerti_type());
				pstmt.setString(6, bpInvestInfo.getCerti_code());
				pstmt.setString(7, bpInvestInfo.getDetail_url());
				pstmt.setString(8, String.valueOf(bpInvestInfo.getVersion()));
				pstmt.setString(9, bpInvestInfo.getTs());

				pstmt.executeUpdate();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info(">>>>>>>开始保存数据到表：" + tableName);

		} catch (Exception e) {
			result = 0;

			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public int saveBpInvestPaidList(List<BpInvestPaid> lstBpInvestPaid) {
		int result = 0;
		String tableName = "bp_com_invest_paid";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务
			String insertSql = " insert into " + tableName
					+ " (uuid,detail_url,paid_mode,paid_amount,paid_date,version,ts) values(?,?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpInvestPaid bpInvestPaid : lstBpInvestPaid) {
				pstmt.setString(1, String.valueOf(bpInvestPaid.getUuid()));
				pstmt.setString(2, bpInvestPaid.getDetail_url());
				pstmt.setString(3, bpInvestPaid.getPaid_mode());
				pstmt.setString(4, bpInvestPaid.getPaid_amount());
				pstmt.setString(5, bpInvestPaid.getPaid_date());
				pstmt.setString(6, String.valueOf(bpInvestPaid.getVersion()));
				pstmt.setString(7, bpInvestPaid.getTs());

				pstmt.execute();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info("保存!>>>>>>" + tableName + ":" + lstBpInvestPaid.toString());

		} catch (Exception e) {
			result = 0;

			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public int saveBpInvestSubscribedList(List<BpInvestSubscribed> lstBpInvestSubscribed) {
		int result = 0;
		String tableName = "bp_com_invest_subscribed";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);
			String insertSql = " insert into " + tableName
					+ " (uuid,detail_url,subscribed_mode,subscribed_amount,subscribed_date,version,ts) values(?,?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpInvestSubscribed bpInvestSubscribed : lstBpInvestSubscribed) {
				pstmt.setString(1, String.valueOf(bpInvestSubscribed.getUuid()));
				pstmt.setString(2, bpInvestSubscribed.getDetail_url());
				pstmt.setString(3, bpInvestSubscribed.getSubscribed_mode());
				pstmt.setString(4, bpInvestSubscribed.getSubscribed_amount());
				pstmt.setString(5, bpInvestSubscribed.getSubscribed_date());
				pstmt.setString(6, String.valueOf(bpInvestSubscribed.getVersion()));
				pstmt.setString(7, bpInvestSubscribed.getTs());

				pstmt.execute();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info("保存!>>>>>>" + tableName + ":" + lstBpInvestSubscribed.toString());
		} catch (Exception e) {
			result = 0;
			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public int saveBpPersonInfoList(List<BpPersonInfo> lstBpPersonInfo) {
		int result = 0;
		String tableName = "bp_com_person_info";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);
			String insertSql = " insert into " + tableName
					+ " (uuid,position_name,person_name,version,ts) values(?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpPersonInfo bpPersonInfo : lstBpPersonInfo) {
				pstmt.setString(1, bpPersonInfo.getUuid());
				pstmt.setString(2, bpPersonInfo.getPosition_name());
				pstmt.setString(3, bpPersonInfo.getPerson_name());
				pstmt.setString(4, String.valueOf(bpPersonInfo.getVersion()));
				pstmt.setString(5, bpPersonInfo.getTs());

				pstmt.execute();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info("保存!>>>>>>" + tableName + ":" + lstBpPersonInfo.toString());

		} catch (Exception e) {
			result = 0;
			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public int saveBpBranchInfoList(List<BpBranchInfo> lstBpBranchInfo) {
		int result = 0;
		String tableName = "bp_com_branch_info";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);
			String insertSql = " insert into " + tableName
					+ " (uuid,certi_code,branch_name,reg_authority,version,ts) values(?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpBranchInfo bpBranchInfo : lstBpBranchInfo) {
				pstmt.setString(1, bpBranchInfo.getUuid());
				pstmt.setString(2, bpBranchInfo.getCerti_code());
				pstmt.setString(3, bpBranchInfo.getBranch_name());
				pstmt.setString(4, bpBranchInfo.getReg_authority());
				pstmt.setString(5, String.valueOf(bpBranchInfo.getVersion()));
				pstmt.setString(6, bpBranchInfo.getTs());

				pstmt.execute();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info("保存!>>>>>>" + tableName + ":" + lstBpBranchInfo.toString());

		} catch (Exception e) {
			result = 0;
			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public int saveBpClearInfo(BpClearInfo bpClearInfo) {
		int result = 0;
		String tableName = "bp_com_clear_info";

		try {
			Class.forName(Driver);
			Connection conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			String insertSql = " insert into " + tableName
					+ " (uuid,account_man,account_member,version,ts) values(?,?,?,?,?)";
			PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			pstmt.setString(1, bpClearInfo.getUuid());
			pstmt.setString(2, bpClearInfo.getAccount_man());
			pstmt.setString(3, bpClearInfo.getAccount_member());
			pstmt.setString(4, String.valueOf(bpClearInfo.getVersion()));
			pstmt.setString(5, bpClearInfo.getTs());

			logger.info(">>>>>>>开始保存数据到表：" + tableName);

			try {
				if (1 == pstmt.executeUpdate())
					result++;

			} catch (Exception e) {
				e.printStackTrace();
				CommUtils.SaveError(bpClearInfo.toString() + ">>>>>>\n" + e.getMessage());

			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			}

			logger.info("保存!>>>>>>" + tableName + ":" + bpClearInfo.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int saveBpExceptionInfoList(List<BpExceptionInfo> lstBpExceptionInfo) {
		int result = 0;
		String tableName = "bp_com_exception_info";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String insertSql = " insert into " + tableName
					+ " (uuid,rn,include_reason,include_date,include_authority,exclude_reason,exclude_date, "
					+ " exclude_authority,version,ts) values(?,?,?,?,?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpExceptionInfo bpExceptionInfo : lstBpExceptionInfo) {
				pstmt.setString(1, bpExceptionInfo.getUuid());
				pstmt.setString(2, bpExceptionInfo.getRn());
				pstmt.setString(3, bpExceptionInfo.getInclude_reason());
				pstmt.setString(4, bpExceptionInfo.getInclude_date());
				pstmt.setString(5, bpExceptionInfo.getInclude_authority());
				pstmt.setString(6, bpExceptionInfo.getExclude_reason());
				pstmt.setString(7, bpExceptionInfo.getExclude_date());
				pstmt.setString(8, bpExceptionInfo.getExclude_authority());
				pstmt.setString(9, String.valueOf(bpExceptionInfo.getVersion()));
				pstmt.setString(10, bpExceptionInfo.getTs());

				pstmt.executeUpdate();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info(">>>>>>>开始保存数据到表：" + tableName);

		} catch (Exception e) {
			result = 0;

			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public int saveBpBlackInfoList(List<BpBlackInfo> lstBpBlackInfo) {
		int result = 0;
		String tableName = "bp_com_black_info";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			Class.forName(Driver);
			conn = (Connection) DriverManager.getConnection(Conn, User, Passwd);
			conn.setAutoCommit(false);// 开启事务

			String insertSql = " insert into " + tableName
					+ " (uuid,rn,include_reason,include_date,include_authority,exclude_reason,exclude_date, "
					+ " exclude_authority,version,ts) values(?,?,?,?,?,?,?,?,?,?)";
			pstmt = (PreparedStatement) conn.prepareStatement(insertSql);

			for (BpBlackInfo bpBlackInfo : lstBpBlackInfo) {
				pstmt.setString(1, bpBlackInfo.getUuid());
				pstmt.setString(2, bpBlackInfo.getRn());
				pstmt.setString(3, bpBlackInfo.getInclude_reason());
				pstmt.setString(4, bpBlackInfo.getInclude_date());
				pstmt.setString(5, bpBlackInfo.getInclude_authority());
				pstmt.setString(6, bpBlackInfo.getExclude_reason());
				pstmt.setString(7, bpBlackInfo.getExclude_date());
				pstmt.setString(8, bpBlackInfo.getExclude_authority());
				pstmt.setString(9, String.valueOf(bpBlackInfo.getVersion()));
				pstmt.setString(10, bpBlackInfo.getTs());

				pstmt.executeUpdate();
			}

			conn.commit();// 事务提交
			result = 1;

			logger.info(">>>>>>>开始保存数据到表：" + tableName);

		} catch (Exception e) {
			result = 0;

			e.printStackTrace();

			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

}
