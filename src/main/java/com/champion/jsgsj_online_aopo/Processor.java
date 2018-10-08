package com.champion.jsgsj_online_aopo;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.*;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.champion.webspider.thread.CountableThreadPool;
import com.champion.webspider.utils.BCConvert;
import com.champion.webspider.utils.CommUtils;
import com.champion.webspider.utils.JsonUtils;
import com.champion.webspider.utils.RedisUtils;

import com.gargoylesoftware.htmlunit.util.Cookie;

import redis.clients.jedis.Jedis;

public class Processor {
	public static final String URL_HOME = "http://www.jsgsj.gov.cn:58888/province/";
	public static final String URL_JIYAN_PARAM = "http://www.jsgsj.gov.cn:58888/province/geetestViladateServlet.json?register=true";
	public static final String URL_JIYAN_CHECK = "http://www.jsgsj.gov.cn:58888/province/geetestViladateServlet.json?validate=true";
	public static final String URL_COM_URL = "http://www.jsgsj.gov.cn:58888/province/infoQueryServlet.json?queryCinfo=true";
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static DbUtility dbUtil = new DbUtility();
	private static String cluster = null;
	private static String task = null;
	private static String target = null;
	private static String tb_company_list = null;
	private static String tb_company_url = null;
	private static int threadnum = 1;
	private static int polltime_url = 5000;
	private static int polltime_info = 1000;
	private static boolean comUrlIpLocked = false;
	private static boolean comInfoIpLocked = false;
	private static String comUrlIP = "";
	private static String comInfoIP = "";
	private static String is_second_master = null;

	private static String email_list = null;
	private static String email_exception_history = null;

	private static int expire_second = 3600;
	private static int redo_interval_second = 120;
	private static int max_page_view = 10;


	Processor() {
	}

	public static Jedis getRedis() {
		Jedis jedis = null;

		while (true) {
			try {
				jedis = RedisUtils.getJedis();

			} catch (Exception e) {
				jedis = null;
				sendMail("redis异常", "未获取到redis实例");
			}

			if (jedis != null) {
				break;
			}

			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return jedis;
	}

	public static void returnRedis(Jedis jedis){
		try {
			if (jedis != null) {
				RedisUtils.returnResource(jedis);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getRedisProxySetKey() {
		return "proxy:Set:ProxySet";
	}

	public static WebClient getClient() {
		WebClient client = null;
		Jedis jedis = getRedis();

		try {
			client = PooledClientFactory.getInstance(threadnum + 8).getClient();

			client.getOptions().setTimeout(30000);
			client.getOptions().setCssEnabled(false);
			client.getOptions().setJavaScriptEnabled(false);
			client.getOptions().setThrowExceptionOnScriptError(false);
			client.getOptions().setThrowExceptionOnFailingStatusCode(false);
			client.getCookieManager().setCookiesEnabled(true);
			ProxyConfig proxyConfig = client.getOptions().getProxyConfig();
			String memberProxySet = "";
//			memberProxySet = jedis.srandmember(getRedisProxySetKey());
			if (StringUtils.isNotBlank(memberProxySet)) {

				ProxySet proxySet = JSON.parseObject(memberProxySet, ProxySet.class);

				proxyConfig.setProxyHost(proxySet.getHost());
				proxyConfig.setProxyPort(Integer.valueOf(proxySet.getPort()));


//				proxyConfig.setProxyHost("http://H7T3EL0D3A6B72UD:3E5C33A799E1C5E5@http-dyn.abuyun.com");
//				proxyConfig.setProxyPort(9020);

				System.out.print(proxySet.getHost() + "	" + proxySet.getPort());
				client.getOptions().setProxyConfig(proxyConfig);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return client;
	}

	public static void returnClient(WebClient client) {

		try {
			if (client != null) {
				PooledClientFactory.getInstance().returnClient(client);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void waitForComUrl() {
		try {
			Thread.sleep(polltime_url);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void waitForComInfo() {
		try {
			Thread.sleep(polltime_info);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 更新请求对象的状态信息
	 */
	private static boolean updateComUrlPart(ComUrl comUrl, int part) {
		boolean result = true;

		if (comUrl == null) {
			return false;
		}

		// 更新公司Url参数状态
		comUrl.setPart(part);
		comUrl.setTs(sdf.format(new Date()));
		if (1 != dbUtil.setComUrlByUuid(tb_company_url, comUrl)) {
			sendMail("数据库异常", "更新公司Url参数状态失败：" + comUrl.toString());
			return false;
		}

		return result;
	}

	private static boolean updateComUrlStatus(ComUrl comUrl, int status) {
		boolean result = true;

		if (comUrl == null) {
			return false;
		}

		// 更新公司Url参数status
		comUrl.setStatus(status);
		if (status == 1) {
			comUrl.setVersion(comUrl.getVersion() + 1);
		}
		comUrl.setTs(sdf.format(new Date()));
		if (1 != dbUtil.setComUrlByUuid(tb_company_url, comUrl)) {
			sendMail("数据库异常", "更新公司Url参数status失败：" + comUrl.toString());
			return false;
		}

		return result;
	}

	private static boolean updateComListStatus(ComList comList, int status) {
		boolean result = true;

		if (comList == null) {
			return false;
		}

		comList.setStatus(status);
		comList.setTs(sdf.format(new Date()));
		if (1 != dbUtil.setComListByCname(tb_company_list, comList)) {
			sendMail("数据库异常", "更新公司名录参数status失败：" + comList.toString());
			return false;
		}

		return result;
	}

	private static boolean updateBpInvestInfoPart(BpInvestInfo bpInvestInfo, int part) {
		boolean result = true;

		if (bpInvestInfo == null) {
			return false;
		}

		// 更新股东信息part
		bpInvestInfo.setPart(part);
		bpInvestInfo.setTs(sdf.format(new Date()));
		if (1 != dbUtil.setBpInvestInfo(bpInvestInfo)) {
			sendMail("数据库异常", "更新股东信息part参数状态失败：" + bpInvestInfo.toString());
			return false;
		}

		return result;
	}

	/*
	 * 获取任务响应的redis key
	 */
	public static String getRedisMasterKey(String task) {
		return task + ":Master";
	}

	public static String getRedisComListKey(String task) {
		return task + ":ComList";
	}

	public static String getRedisComUrlKey(String task) {
		return task + ":ComUrl";
	}

	public static String getRedisInvestInfoKey(String task) {
		return task + ":InvestInfo";
	}

	public static String getPageContent(Page page) {
		String content = null;

		try {
			WebResponse response = page.getWebResponse();
			int status = response.getStatusCode();
			if (status == 200) {
				content = response.getContentAsString();
			} else if (status == 403) {
				content = "403";
			} else {
				content = "未知错误";
			}

			// 关闭响应流
			response.cleanUp();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return content;
	}

	public static boolean checkComUrlContent(String content) {
		if (StringUtils.isBlank(content) || content.equals("未知错误")) {
			return false;
		}
		if (content.equals("403") || content.equals("please wait a moment")) {
			try {
				if (!comUrlIpLocked) {
					comUrlIP = InetAddress.getLocalHost().getHostAddress();
					sendMail("网络异常", comUrlIP + "该ip获取公司url页面已被封:" + content);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			comUrlIpLocked = true;
		} else {
			comUrlIpLocked = false;
		}

		return !comUrlIpLocked;
	}

	public static boolean checkComInfoContent(String content) {
		if (StringUtils.isBlank(content) || content.equals("未知错误")) {
			return false;
		}
		if (content.equals("403") || content.equals("please wait a moment")) {
			try {
				if (!comInfoIpLocked) {
					comInfoIP = InetAddress.getLocalHost().getHostAddress();
					sendMail("网络异常", comInfoIP + "该ip获取公司详情页面已被封:" + content);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			comInfoIpLocked = true;
		} else {
			comInfoIpLocked = false;
		}

		return !comInfoIpLocked;
	}

	public static boolean CompareCname(String pre_cname, String cname) {
		boolean result = false;

		if (StringUtils.isBlank(pre_cname) || StringUtils.isBlank(cname)) {
			return false;
		}

		String cname1 = BCConvert.qj2bj(pre_cname).replace("（", "(").replace("）", ")").trim();
		String cname2 = BCConvert.qj2bj(cname).replace("（", "(").replace("）", ")").trim();

		if (cname1.equals(cname2)) {
			result = true;
		}

		return result;
	}


	public static void sendMail(String subject, String message) {
		Jedis jedis = RedisUtils.getJedis();
		Mail.sendMail(jedis, task, email_exception_history, email_list, subject,message);
	}

	public static boolean checkMasterRunning() {
		Jedis jedis = null;

		try {
			jedis = RedisUtils.getJedis();
			String masterTag = jedis.get(getRedisMasterKey(task));
			if (StringUtils.isBlank(masterTag)) {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			returnRedis(jedis);
		}

		return true;
	}

	/*
	 * 程序入口，读取配置文件，进行初始化，执行任务
	 */
	public static void main(String[] args) {
		String configPath = "config/init.properties";

		 if (CommUtils.getValueFromProperties(configPath, "expire_second") != null){
			 expire_second = Integer.valueOf(CommUtils.getValueFromProperties(configPath, "expire_second"));
		 }

		if(CommUtils.getValueFromProperties(configPath, "redo_interval_second") != null){
			redo_interval_second = Integer.valueOf(CommUtils.getValueFromProperties(configPath, "redo_interval_second"));
		}

		// 获取数据库连接信息
		String jdbc_connector = CommUtils.getValueFromProperties(configPath, "jdbc_connector");
		if (StringUtils.isNotBlank(jdbc_connector)) {
			DbUtility.Conn = jdbc_connector;
		}
		String db_user = CommUtils.getValueFromProperties(configPath, "db_user");
		if (StringUtils.isNotBlank(db_user)) {
			DbUtility.User = db_user;
		}
		String db_passwd = CommUtils.getValueFromProperties(configPath, "db_passwd");
		if (StringUtils.isNotBlank(db_passwd)) {
			DbUtility.Passwd = db_passwd;
		}

		// 获取redis配置
		String redis_addr = CommUtils.getValueFromProperties(configPath, "redis_addr");
		String redis_port = CommUtils.getValueFromProperties(configPath, "redis_port");
		if (StringUtils.isNotBlank(redis_addr) && StringUtils.isNotBlank(redis_port)) {
			// 初始化redis
			RedisUtils.InitRedis(redis_addr, Integer.valueOf(redis_port));
		}
		// 邮箱发送列表
		email_list = CommUtils.getValueFromProperties(configPath, "email_list");
		if (StringUtils.isBlank(email_list)) {
			email_list = "xuxingyuan@champion.com";
		}

		// 异常发送历史记录
		email_exception_history = CommUtils.getValueFromProperties(configPath, "email_exception_history");
		if (StringUtils.isBlank(email_exception_history)) {
			email_exception_history = "email_exception_history";
		}

		// 获取公司名录信息
		tb_company_list = CommUtils.getValueFromProperties(configPath, "tb_company_list");
		if (StringUtils.isBlank(tb_company_list)) {
			tb_company_list = "company_list";
		}

		// 获取公司url信息
		tb_company_url = CommUtils.getValueFromProperties(configPath, "tb_company_url");
		if (StringUtils.isBlank(tb_company_url)) {
			tb_company_url = "company_url";
		}

		// 集群配置
		cluster = CommUtils.getValueFromProperties(configPath, "cluster");
		if (StringUtils.isBlank(cluster)) {
			cluster = "master";
		}

		// 爬取任务配置
		task = CommUtils.getValueFromProperties(configPath, "task");
		if (StringUtils.isBlank(task)) {
			task = "jsgsj-online-aopo";
		}

		// 爬取目标配置
		target = CommUtils.getValueFromProperties(configPath, "target");
		if (StringUtils.isBlank(target)) {
			target = "company_all";
		}

		// 线程数
		String strThreadnum = CommUtils.getValueFromProperties(configPath, "threadnum");
		if (StringUtils.isNotBlank(strThreadnum)) {
			threadnum = Integer.valueOf(strThreadnum);
		}

		// 任务轮询间隔时间
		String strPolltimeUrl = CommUtils.getValueFromProperties(configPath, "polltime_url");
		if (StringUtils.isNotBlank(strPolltimeUrl)) {
			polltime_url = Integer.valueOf(strPolltimeUrl);
		}

		String strPolltimeInfo = CommUtils.getValueFromProperties(configPath, "polltime_info");
		if (StringUtils.isNotBlank(strPolltimeInfo)) {
			polltime_info = Integer.valueOf(strPolltimeInfo);
		}

		is_second_master = CommUtils.getValueFromProperties(configPath, "is_second_master");
		if (StringUtils.isBlank(is_second_master)) {
			is_second_master = "false";
		}

		if (target.equals("company_list")) {
			// 执行公司名录分发任务
			doComListTask();

		} else if (target.equals("company_url")) {
			// 执行公司url获取任务
			doComUrlTask();

		} else if (target.equals("company_info")) {
			// 执行公司工商详细信息获取任务
			doComInfoTask();
			doInvestInfoTask();

		} else if (target.equals("company_all")) {
			doComListTask();
			doComUrlTask();
			doComInfoTask();
			doInvestInfoTask();
		}
	}

	/*
	 * 公司名录分派，1秒轮询一次名录，status=0的公司，放入redis名录队列 cluster中为master的执行该任务
	 */
	public static void doComListTask() {
		CountableThreadPool threadPool = new CountableThreadPool(1);

		threadPool.execute(new Runnable() {
			public void run() {
				while (true) {
					// 获取redis实例
					Jedis jedis = getRedis();

					try {
						if (cluster.equals("master") || (!checkMasterRunning() && is_second_master.equals("true"))) {
							// 读取公司名录
							List<ComList> lstComList = dbUtil.getComList(tb_company_list, true);

							// 保存待处理公司名录到redis集合
							for (ComList comList : lstComList) {
								String memberComList = JSON.toJSONString(comList);
								jedis.sadd(getRedisComListKey(task), memberComList);
							}

							if (cluster.equals("master")) {
								jedis.setex(getRedisMasterKey(task), 30, "ok");
							}
						}

						Thread.sleep(1000);

					} catch (Exception e) {
						e.printStackTrace();

					} finally {
						returnRedis(jedis);
					}
				}
			}
		});
	}

	/*
	 * 根据公司名录，搜索公司，抓取公司URL参数
	 */
	public static void doComUrlTask() {
		CountableThreadPool threadPool = new CountableThreadPool(1);

		threadPool.execute(new Runnable() {
			public void run() {
				CountableThreadPool threadPool = new CountableThreadPool(threadnum);
				String member = null;
				int timeCount = 0;

				while (true) {
					// 获取redis实例
					Jedis jedis = getRedis();

					try {
						if (comUrlIpLocked) {
							if (comUrlIP.equals(InetAddress.getLocalHost().getHostAddress())) {
								System.out.println("公司url页面访问已经被封...");
								for (int i = 0; i < 5; i++) {
									waitForComUrl();
								}
							}
							comUrlIpLocked = false;
						}

						if (threadPool.getThreadAlive() >= threadnum) {
							waitForComUrl();
							continue;
						}

						// 获取公司名录
						member = jedis.spop(getRedisComListKey(task));
						if (StringUtils.isBlank(member)) {
							System.out.println("无公司名录，请等待...");

							timeCount++;
							timeCount = timeCount % 5;// 5次后把status=2的重新放入redis

							if (timeCount == 0 && threadPool.getThreadAlive() == 0) {
								if (cluster.equals("master")
										|| (!checkMasterRunning() && is_second_master.equals("true"))) {
									List<ComList> lstComList = dbUtil.getComList(tb_company_list, false);

									for (ComList comList : lstComList) {
										String memberComList = JSON.toJSONString(comList);
										jedis.sadd(getRedisComListKey(task), memberComList);
									}
								}
							}
							waitForComUrl();
							continue;
						}

						final WebClient clientFinal = getClient();
						final ComList comListFinal = JSON.parseObject(member, ComList.class);

						threadPool.execute(new Runnable() {
							public void run() {
								Jedis jedis = getRedis();

								try {
									if (false == processComUrlTask(clientFinal, comListFinal)) {
										if(comListFinal.getPage_view() <= max_page_view){
											comListFinal.setPage_view(comListFinal.getPage_view() + 1);
											String memberComList = JSON.toJSONString(comListFinal);
											jedis.sadd(getRedisComListKey(task), memberComList);
										}
									}

								} catch (Exception e) {
									e.printStackTrace();

									if (jedis != null) {
										String memberComList = JSON.toJSONString(comListFinal);
										jedis.sadd(getRedisComListKey(task), memberComList);
									}

								} finally {
									returnRedis(jedis);
									returnClient(clientFinal);
								}
							}
						});

						waitForComUrl();

					} catch (Exception e) {
						e.printStackTrace();

						if (jedis != null) {
							if (StringUtils.isNotBlank(member)) {
								jedis.sadd(getRedisComListKey(task), member);
							}
						}

					} finally {
						returnRedis(jedis);
					}
				}
			}
		});
	}

	public static boolean processComUrlTask(WebClient client, ComList comList) {
		try {
			if (comList.getCname().length() < 3 || comList.getCname().toLowerCase().equals("null")) {
				return updateComListStatus(comList, -1);
			}

			// 获取搜索首页
			Page page = client.getPage(URL_HOME);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}

			// 获取cookie
			CookieManager cookieMgr = client.getCookieManager();
			Set<Cookie> cookieSet = cookieMgr.getCookies();
			String cookie = "";
			for (Cookie ck : cookieSet) {
				cookie += ck + ";";
			}
			cookie = cookie.substring(0, cookie.length() - 1);

			// 获取极验滑块参数页面
			WebRequest request = new WebRequest(new URL(URL_JIYAN_PARAM + "&t=" + new Date().getTime()),
					HttpMethod.GET);
			request.setAdditionalHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
			request.setAdditionalHeader("Cookie", cookie);
			page = client.getPage(request);
			content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("验证码异常", "获取极验滑块参数非JSON格式:" + comList.toString());
				return false;
			}
			String result = JSON.parseObject(content).getString("success");
			String gt = JSON.parseObject(content).getString("gt");
			String challenge = JSON.parseObject(content).getString("challenge");

			// 极验验证获取code
			String jiyan_home_url = "http://jiyanapi.c2567.com/";
			String jiyan_param_url = "shibie?gt=" + gt + "&challenge=" + challenge
					+ "&user=champion&pass=champion@23&return=json";
//			if ("0".equals(result)) {
			jiyan_param_url += "&model=3";
//			}
			String validate_url = jiyan_home_url + jiyan_param_url;

			// 做3次超时异常处理
			try {
				page = client.getPage(validate_url);

			} catch (Exception e1) {
				e1.printStackTrace();

				try {
					jiyan_home_url = "http://jiyanapi1.c2567.com/";
					validate_url = jiyan_home_url + jiyan_param_url;
					page = client.getPage(validate_url);
				} catch (Exception e2) {
					e2.printStackTrace();

					jiyan_home_url = "http://jiyanapi2.c2567.com/";
					validate_url = jiyan_home_url + jiyan_param_url;
					page = client.getPage(validate_url);
				}
			}
			content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("验证码异常", "极验验证获取结果非JSON格式:" + comList.toString());
				return false;
			}
			String status = JSON.parseObject(content).get("status").toString();
			if (!status.equals("ok")) {
				sendMail("验证码异常", "极验验证失败:" + comList.toString());
				return false;
			}
			challenge = JSON.parseObject(content).getString("challenge");
			String validate = JSON.parseObject(content).getString("validate");
			String seccode = validate + "|jordan";

			// 极验二次验证
			String validate_check_url = URL_JIYAN_CHECK + "&type=search&name="
					+ URLEncoder.encode(comList.getCname(), "utf-8") + "&geetest_challenge=" + challenge
					+ "&geetest_validate=" + validate + "&geetest_seccode=" + URLEncoder.encode(seccode, "utf-8");
			request = new WebRequest(new URL(validate_check_url), HttpMethod.GET);
			request.setAdditionalHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
			request.setAdditionalHeader("Cookie", cookie);
			page = client.getPage(request);
			content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("验证码异常", "极验二次验证结果非JSON格式:" + comList.toString());
				return false;
			}
			status = JSON.parseObject(content).get("status").toString();
			if (!status.equals("success")) {
				sendMail("验证码异常", "极验二次验证失败:" + comList.toString());
				return false;
			}
			String name = JSON.parseObject(content).getString("name");

			// 请求公司url
			String qyxx_url = URL_COM_URL + "&searchType=qyxx&pageNo=1&pageSize=10&name=" + name;
			request = new WebRequest(new URL(qyxx_url), HttpMethod.GET);
			request.setAdditionalHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
			request.setAdditionalHeader("Cookie", cookie);
			page = client.getPage(request);
			content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}

			// 处理页面内容
			String total = JSON.parseObject(content).getString("total");
			String error = JSON.parseObject(content).getString("ERROR");
			String items = JSON.parseObject(content).getString("items");
//			if (StringUtils.isNotBlank(total) || StringUtils.isNotBlank(error)) {
			if (StringUtils.isNotBlank(error)) {
				if (false == updateComListStatus(comList, -1)) {
					return false;
				}
				return true;
			}

			// 解析公司节点信息
			List<ComUrl> comUrlList = new ArrayList<ComUrl>();

			JSONArray qyxxJSONArray = JSON.parseArray(items);
			for (int i = 0; i < qyxxJSONArray.size(); i++) {
				JSONObject qyxxJSONObj = qyxxJSONArray.getJSONObject(i);

				String corp_org = qyxxJSONObj.getString("ORG");
				String corp_id = qyxxJSONObj.getString("ID");
				String corp_seq_id = qyxxJSONObj.getString("SEQ_ID");
				if (StringUtils.isBlank(corp_org) || StringUtils.isBlank(corp_id) || StringUtils.isBlank(corp_seq_id)) {
					continue;
				}
				String uuid = corp_org + "-" + corp_id + "-" + corp_seq_id;
				String cname = qyxxJSONObj.getString("CORP_NAME");
				String abnormal = qyxxJSONObj.getString("ABNORMAL");
				String admit_main = qyxxJSONObj.getString("ADMIT_MAIN");
				String reg_status = qyxxJSONObj.getString("CORP_STATUS");
				String reg_code = qyxxJSONObj.getString("REG_NO");
				String uni_scid = qyxxJSONObj.getString("UNI_SCID");
				String pname = qyxxJSONObj.getString("OPER_MAN_NAME");
				String reg_date = qyxxJSONObj.getString("START_DATE");

				// 只保留相等的
				if ((comList.getSearch_type() == 1 &&CompareCname(comList.getCname(), cname))
						|| (comList.getSearch_type() == 2 && (CompareCname(comList.getCname(), uni_scid)
								|| CompareCname(comList.getCname(), reg_code)))) {

					// 公司url对象
					ComUrl comUrl = new ComUrl();
					comUrl.setCorp_org(corp_org);
					comUrl.setCorp_id(corp_id);
					comUrl.setCorp_seq_id(corp_seq_id);
					comUrl.setUuid(uuid);
					comUrl.setPre_cname(comList.getCname());
					comUrl.setCname(cname);
					comUrl.setAdmit_main(admit_main);
					comUrl.setReg_status(reg_status);
					comUrl.setReg_code(reg_code);
					comUrl.setUni_scid(uni_scid);
					comUrl.setPname(pname);
					comUrl.setReg_date(reg_date);
					comUrl.setVersion(0);// 初始为0
					comUrl.setTs(sdf.format(new Date()));

					comUrlList.add(comUrl);
				}
			}

			if (comUrlList.size() > 0) {
				// 保存公司url到数据库
				if (1 != dbUtil.saveComUrlList(tb_company_url, comUrlList)) {
					sendMail("数据库异常", "保存公司url参数失败：" + comUrlList.toString());
					return false;
				}

				// 更新公司名录状态
				if (false == updateComListStatus(comList, 1)) {
					sendMail("数据库异常", "公司名录状态更新失败：" + comList.toString());
					return false;
				}
			} else {
				// 更新公司名录状态
				if (false == updateComListStatus(comList, -1)) {
					sendMail("数据库异常", "公司名录状态更新失败：" + comList.toString());
					return false;
				}
			}

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("公司url抓取异常", e.getMessage() + ">>>>>>" + comList.toString() + ">>>>>>" + URL_HOME);
			return false;
		}
	}

	/*
	 * 公司工商信息抓取任务
	 */
	public static void doComInfoTask() {
		CountableThreadPool threadPool = new CountableThreadPool(1);

		threadPool.execute(new Runnable() {
			public void run() {
				CountableThreadPool threadPool = new CountableThreadPool(threadnum);
				String member = null;
				int timeCount = 0;
				boolean stauts = true;

				while (true) {
					// 获取redis实例
					Jedis jedis = getRedis();

					try {
						if (comInfoIpLocked) {
							if (comInfoIP.equals(InetAddress.getLocalHost().getHostAddress())) {
								System.out.println("工商信息页面访问已被封...");
								for (int i = 0; i < 5; i++) {
									waitForComInfo();
								}
							}
							comInfoIpLocked = false;
						}

						if (threadPool.getThreadAlive() >= threadnum) {
							waitForComInfo();
							continue;
						}

						// 获取公司url参数
						member = jedis.spop(getRedisComUrlKey(task));
						if (StringUtils.isBlank(member)) {
							System.out.println("无公司url参数信息，请等待...");

							timeCount++;
							timeCount = timeCount % 5;

							if (timeCount == 0 && threadPool.getThreadAlive() == 0) {
								if (cluster.equals("master")
										|| (!checkMasterRunning() && is_second_master.equals("true"))) {
									System.out.println("获取status=0的公司url信息, 放入redis");
									// 5次后把status=0的放入redis
									List<ComUrl> lstComUrl = dbUtil.getComUrl(tb_company_url, true);

									for (ComUrl comUrl : lstComUrl) {
										String memberComUrl = JSON.toJSONString(comUrl);
										jedis.sadd(getRedisComUrlKey(task), memberComUrl);
									}

//									System.out.println("检查status=2的公司url信息是否超过120秒, 如果是重新放入redis");

//									程序启动时，将status=2的加入爬虫列表
									if(stauts) {
										List<ComUrl> lstComUrl2 = dbUtil.getComUrl(tb_company_url, false);

										for (ComUrl comUrl : lstComUrl2) {
//										if ((new Date().getTime() - sdf.parse(comUrl.getTs()).getTime()) / 1000 > redo_interval_second && (new Date().getTime() - sdf.parse(comUrl.getTs()).getTime()) / 1000 < expire_second) {
											String memberComUrl = JSON.toJSONString(comUrl);
											jedis.sadd(getRedisComUrlKey(task), memberComUrl);
//										}
										}
										stauts = false;
									}
								}
							}
							waitForComInfo();
							continue;
						}

						final WebClient clientFinal = getClient();
						final ComUrl comUrlFinal = JSON.parseObject(member, ComUrl.class);

						threadPool.execute(new Runnable() {
							public void run() {
								Jedis jedis = getRedis();

								try {
									if (false == processComInfoTask(clientFinal, comUrlFinal)) {
//										 每一个url最多访问次数：max_page_view
										if(comUrlFinal.getPage_view() <= max_page_view){
											comUrlFinal.setPage_view(comUrlFinal.getPage_view() + 1);
											String memberComUrl = JSON.toJSONString(comUrlFinal);
											jedis.sadd(getRedisComUrlKey(task), memberComUrl);
										}else {
											comUrlFinal.setStatus(-1);
											dbUtil.setComUrlByUuid(tb_company_url, comUrlFinal);
											ComList comList = new ComList();
											comList.setCname(comUrlFinal.getPre_cname());
											comList.setStatus(0);
											comList.setTs(comUrlFinal.getTs());
											if (1 != dbUtil.setComListByCname(tb_company_list, comList)) {
												sendMail("数据库异常", "更新公司名录参数status失败：" + comList.toString());
											}

										}
									}
								} catch (Exception e) {
									e.printStackTrace();

									if (jedis != null) {
										String memberComUrl = JSON.toJSONString(comUrlFinal);
										jedis.sadd(getRedisComUrlKey(task), memberComUrl);
									}
								} finally {
									returnRedis(jedis);
									returnClient(clientFinal);
								}
							}
						});

						waitForComInfo();

					} catch (Exception e) {
						e.printStackTrace();

						if (jedis != null) {
							if (StringUtils.isNotBlank(member)) {
								jedis.sadd(getRedisComUrlKey(task), member);
							}
						}
					} finally {
						returnRedis(jedis);
					}
				}
			}
		});
	}

	/*
	 * 公司工商详情信息抓取
	 */
	public static boolean processComInfoTask(WebClient client, ComUrl comUrl) {
		boolean result = true;
		if (comUrl.getStatus() == 1) {
			return true;
		}
		
		if (comUrl.getUuid().length() < 32) {
			return updateComUrlStatus(comUrl, -1);
		}

		try {
			switch (comUrl.getPart()) {
			case 0:// 基本信息处理开始
			{
				result = processBpBaseInfoQuery(client, comUrl);
				break;
			}
			case 1:// 股东信息处理开始
			{
				result = processBpInvestInfoQuery(client, comUrl);
				break;
			}
			case 2:// 高管信息处理开始
			{
				result = processBpPersonInfoQuery(client, comUrl);
				break;
			}
			case 3:// 分支机构信息处理开始
			{
				result = processBpBranchInfoQuery(client, comUrl);
				break;
			}
			case 4:// 清算信息处理开始
			{
				result = processBpClearInfoQuery(client, comUrl);
				break;
			}
			case 5:// 异常信息处理开始
			{
				result = processBpExceptionInfoQuery(client, comUrl);
				break;
			}
			case 6:// 严重违法信息处理开始
			{
				result = processBpBlackInfoQuery(client, comUrl);
			}
			}

			// 处理股东详情页面
			if (result && comUrl.getStatus() != -1) {
				boolean investOK = true;
				boolean investAllOK = true;

				List<BpInvestInfo> lstBpInvestInfo = dbUtil.getBpInvestInfoList(comUrl);
				for (BpInvestInfo bpInvestInfo : lstBpInvestInfo) {
					if (StringUtils.isNotBlank(bpInvestInfo.getDetail_url())) {
						waitForComInfo();

						investOK = processBpInvestDetailTask(client, bpInvestInfo);
						investAllOK = investAllOK & investOK;
						if (!investOK) {
							// 获取redis实例
							Jedis jedis = getRedis();

							try {
								String memberInvestInfo = JSON.toJSONString(bpInvestInfo);
								jedis.sadd(getRedisInvestInfoKey(task), memberInvestInfo);

							} catch (Exception e) {
								e.printStackTrace();

							} finally {
								returnRedis(jedis);
							}
						}
					}
				}

				if (investAllOK) {
					result = updateComUrlStatus(comUrl, 1);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("工商信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpBaseInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取工商基本信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?pageView=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id();
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取基本信息结果非JSON格式:" + comUrl.toString());
				return false;
			}
			// 企业工商信息不存在
			if (content.equals("{}")) {
				sendMail("工商信息异常", "企业工商信息不存在或url已失效:" + comUrl.toString());
				return updateComUrlStatus(comUrl, -1);
			}

			JSONObject jbxxJSONObj = JSON.parseObject(content);

			// 公司名
			String cname = jbxxJSONObj.getString("CORP_NAME");

			// 注册号|统一社会信用代码
			String reg_code = jbxxJSONObj.getString("REG_NO");

			// 公司类型
			String reg_type = jbxxJSONObj.getString("ZJ_ECON_KIND");

			// 法人|法定代表人|负责人|投资人|经营者|合伙人|委派代表
			String pname = jbxxJSONObj.getString("OPER_MAN_NAME");

			// 注册资本|成员出资总额
			String reg_capi = jbxxJSONObj.getString("REG_CAPI");

			// 注册资本类型
			String reg_capi_type = jbxxJSONObj.getString("CAPI_TYPE_NAME");

			// 注册日期|成立日期
			String reg_date = jbxxJSONObj.getString("START_DATE");

			// 住所|营业场所|经营场所
			String address = jbxxJSONObj.getString("ADDR");
			if (StringUtils.isBlank(address)) {
				address = jbxxJSONObj.getString("FARE_PLACE");
			}

			// 组成形式
			String compose_form = jbxxJSONObj.getString("INDIV_FORM_NAME");

			// 营业期限自|经营期限自|合伙期限自
			String business_date_from = jbxxJSONObj.getString("FARE_TERM_START");

			// 营业期限至|经营期限至|合伙期限至
			String business_date_to = jbxxJSONObj.getString("FARE_TERM_END");

			// 经营范围
			String business_scope = jbxxJSONObj.getString("FARE_SCOPE");

			// 登记机关
			String reg_authority = jbxxJSONObj.getString("BELONG_ORG");

			// 核准日期
			String approval_date = jbxxJSONObj.getString("CHECK_DATE");

			// 登记状态
			String reg_status = jbxxJSONObj.getString("CORP_STATUS");

			// 注销日期
			String writeoff_date = jbxxJSONObj.getString("WRITEOFF_DATE");

			// 吊销日期
			String revoke_date = jbxxJSONObj.getString("REVOKE_DATE");

			// 信息完整性校验
			if (StringUtils.isBlank(cname)) {
				sendMail("工商信息异常", "公司名称为空:" + comUrl.toString());
				Mail.sendMessage("工商信息异常", "公司名称为空:" + comUrl.toString());
			}
			if (StringUtils.isBlank(reg_code)) {
				sendMail("工商信息异常", "注册号为空:" + comUrl.toString());
				Mail.sendMessage("工商信息异常", "注册号为空:" + comUrl.toString());
				return false;
			}

			BpBaseInfo bpBaseInfo = new BpBaseInfo();
			bpBaseInfo.setUuid(comUrl.getUuid());
			bpBaseInfo.setCname(cname);
			bpBaseInfo.setReg_code(reg_code);
			bpBaseInfo.setReg_type(reg_type);
			bpBaseInfo.setPname(pname);
			bpBaseInfo.setReg_capi(reg_capi);
			bpBaseInfo.setReg_capi_type(reg_capi_type);
			bpBaseInfo.setReg_date(reg_date);
			bpBaseInfo.setAddress(address);
			bpBaseInfo.setCompose_form(compose_form);
			bpBaseInfo.setBusiness_date_from(business_date_from);
			bpBaseInfo.setBusiness_date_to(business_date_to);
			bpBaseInfo.setBusiness_scope(business_scope);
			bpBaseInfo.setReg_authority(reg_authority);
			bpBaseInfo.setApproval_date(approval_date);
			bpBaseInfo.setReg_status(reg_status);
			bpBaseInfo.setWriteoff_date(writeoff_date);
			bpBaseInfo.setRevoke_date(revoke_date);
			bpBaseInfo.setVersion(comUrl.getVersion() + 1);
			bpBaseInfo.setTs(sdf.format(new Date()));

			// 去掉url重复处理
			ComUrl comUrlTemp = dbUtil.getJSGSJComUrlByUuid(tb_company_url, comUrl.getUuid());
			if (comUrlTemp.getPart() > 0) {
				return true;
			}

			// 保存基本信息
			if (1 != dbUtil.saveBpBaseInfo(bpBaseInfo)) {
				sendMail("数据库异常", "公司基本信息保存失败:" + comUrl.toString());
				return false;
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 1);

			// 下一步处理股东信息
			if (result) {
				result = processBpInvestInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("基本信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpInvestInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取股东信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryGdcz=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id() + "&pageSize=10000&curPage=1";
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取股东信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析股东信息
			String data = JSON.parseObject(content).getString("data");
			List<BpInvestInfo> lstBpInvestInfo = new ArrayList<BpInvestInfo>();

			JSONArray gdczJSONArray = JSON.parseArray(data);
			for (int i = 0; i < gdczJSONArray.size(); i++) {
				JSONObject gdczJSONObj = gdczJSONArray.getJSONObject(i);

				String rn = gdczJSONObj.getString("RN");
				String pname = gdczJSONObj.getString("STOCK_NAME");
				String share_holder_type = gdczJSONObj.getString("STOCK_TYPE");
				String certi_type = gdczJSONObj.getString("IDENT_TYPE_NAME");
				String certi_code = gdczJSONObj.getString("IDENT_NO");
				String detail_url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryGdczGdxx=true"
						+ "&org=" + gdczJSONObj.getString("ORG") + "&id=" + gdczJSONObj.getString("ID") + "&admitMain="
						+ gdczJSONObj.getString("ADMIT_MAIN");

				BpInvestInfo bpInvestInfo = new BpInvestInfo();
				bpInvestInfo.setUuid(comUrl.getUuid());
				bpInvestInfo.setRn(rn);
				bpInvestInfo.setShare_holder_type(share_holder_type);
				bpInvestInfo.setPname(pname);
				bpInvestInfo.setCerti_type(certi_type);
				bpInvestInfo.setCerti_code(certi_code);
				bpInvestInfo.setDetail_url(detail_url);
				bpInvestInfo.setVersion(comUrl.getVersion() + 1);
				bpInvestInfo.setTs(sdf.format(new Date()));

				lstBpInvestInfo.add(bpInvestInfo);
			}

			if (lstBpInvestInfo.size() > 0) {
				// 保存股东信息
				if (1 != dbUtil.saveBpInvestInfoList(lstBpInvestInfo)) {
					sendMail("数据库异常", "股东信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 2);

			// 下一步处理高管信息
			if (result) {
				result = processBpPersonInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("股东信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpPersonInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取高管信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryZyry=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id();
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取高管信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析高管信息
			List<BpPersonInfo> lstBpPersonInfo = new ArrayList<BpPersonInfo>();

			JSONArray zyryJSONArray = JSON.parseArray(content);
			for (int i = 0; i < zyryJSONArray.size(); i++) {
				JSONObject zyryJSONObj = zyryJSONArray.getJSONObject(i);

				String person_name = zyryJSONObj.getString("PERSON_NAME");
				String position_name = zyryJSONObj.getString("POSITION_NAME");

				if (StringUtils.isNotBlank(person_name) || StringUtils.isNotBlank(position_name)) {
					BpPersonInfo bpPersonInfo = new BpPersonInfo();
					bpPersonInfo.setUuid(comUrl.getUuid());
					bpPersonInfo.setPerson_name(person_name);
					bpPersonInfo.setPosition_name(position_name);
					bpPersonInfo.setVersion(comUrl.getVersion() + 1);
					bpPersonInfo.setTs(sdf.format(new Date()));

					lstBpPersonInfo.add(bpPersonInfo);
				}
			}

			if (lstBpPersonInfo.size() > 0) {
				// 保存高管信息
				if (1 != dbUtil.saveBpPersonInfoList(lstBpPersonInfo)) {
					sendMail("数据库异常", "高管信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 3);

			// 下一步分支机构信息
			if (result) {
				result = processBpBranchInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("高管信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpBranchInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取分支机构信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryFzjg=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id();
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取分支机构信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析分支机构信息
			List<BpBranchInfo> lstBpBranchInfo = new ArrayList<BpBranchInfo>();

			JSONArray fzjgJSONArray = JSON.parseArray(content);
			for (int i = 0; i < fzjgJSONArray.size(); i++) {
				JSONObject fzjgJSONObj = fzjgJSONArray.getJSONObject(i);

				String branch_name = fzjgJSONObj.getString("DIST_NAME");
				String certi_code = fzjgJSONObj.getString("DIST_REG_NO");
				String reg_authority = fzjgJSONObj.getString("DIST_BELONG_ORG");

				if (StringUtils.isNotBlank(branch_name) || StringUtils.isNotBlank(certi_code)) {
					BpBranchInfo bpBranchInfo = new BpBranchInfo();
					bpBranchInfo.setUuid(comUrl.getUuid());
					bpBranchInfo.setCerti_code(certi_code);
					bpBranchInfo.setBranch_name(branch_name);
					bpBranchInfo.setReg_authority(reg_authority);
					bpBranchInfo.setVersion(comUrl.getVersion() + 1);
					bpBranchInfo.setTs(sdf.format(new Date()));

					lstBpBranchInfo.add(bpBranchInfo);
				}
			}

			if (lstBpBranchInfo.size() > 0) {
				// 保存分支机构信息
				if (1 != dbUtil.saveBpBranchInfoList(lstBpBranchInfo)) {
					sendMail("数据库异常", "分支机构信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 4);

			// 下一步处理清算信息
			if (result) {
				result = processBpClearInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("分支机构信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpClearInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取清算信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryQsxx=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id();
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取清算信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析清算信息
			JSONObject qsxxJSONObj = JSON.parseArray(content).getJSONObject(0);

			String account_man = qsxxJSONObj.getString("ACCOUNT_MAN");
			String account_member = qsxxJSONObj.getString("ACCOUNT_MEMBER");

			if (StringUtils.isNotBlank(account_man) || StringUtils.isNotBlank(account_member)) {
				BpClearInfo bpClearInfo = new BpClearInfo();
				bpClearInfo.setUuid(comUrl.getUuid());
				bpClearInfo.setAccount_man(account_man);
				bpClearInfo.setAccount_member(account_member);
				bpClearInfo.setVersion(comUrl.getVersion() + 1);
				bpClearInfo.setTs(sdf.format(new Date()));

				// 保存清算信息
				if (1 != dbUtil.saveBpClearInfo(bpClearInfo)) {
					sendMail("数据库异常", "清算信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 5);

			// 下一步处理异常信息
			if (result) {
				result = processBpExceptionInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("清算信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpExceptionInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取经营异常信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryJyyc=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id() + "&pageSize=10000&curPage=1";
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取经营异常信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析经营异常信息
			String data = JSON.parseObject(content).getString("data");
			List<BpExceptionInfo> lstBpExceptionInfo = new ArrayList<BpExceptionInfo>();

			JSONArray jyycJSONArray = JSON.parseArray(data);
			for (int i = 0; i < jyycJSONArray.size(); i++) {
				JSONObject jyycJSONObj = jyycJSONArray.getJSONObject(i);

				String rn = jyycJSONObj.getString("RN");
				String include_reason = jyycJSONObj.getString("FACT_REASON");
				String include_date = jyycJSONObj.getString("MARK_DATE");
				String include_authority = jyycJSONObj.getString("CREATE_ORG");
				String exclude_reason = jyycJSONObj.getString("REMOVE_REASON");
				String exclude_date = jyycJSONObj.getString("CREATE_DATE");
				String exclude_authority = jyycJSONObj.getString("YICHU_ORG");

				BpExceptionInfo bpExceptionInfo = new BpExceptionInfo();
				bpExceptionInfo.setUuid(comUrl.getUuid());
				bpExceptionInfo.setRn(rn);
				bpExceptionInfo.setInclude_reason(include_reason);
				bpExceptionInfo.setInclude_date(include_date);
				bpExceptionInfo.setInclude_authority(include_authority);
				bpExceptionInfo.setExclude_reason(exclude_reason);
				bpExceptionInfo.setExclude_date(exclude_date);
				bpExceptionInfo.setExclude_authority(exclude_authority);
				bpExceptionInfo.setVersion(comUrl.getVersion() + 1);
				bpExceptionInfo.setTs(sdf.format(new Date()));

				lstBpExceptionInfo.add(bpExceptionInfo);
			}

			if (lstBpExceptionInfo.size() > 0) {
				// 保存经营异常信息
				if (1 != dbUtil.saveBpExceptionInfoList(lstBpExceptionInfo)) {
					sendMail("数据库异常", "经营异常信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 6);

			// 下一步处理严重违法信息
			if (result) {
				result = processBpBlackInfoQuery(client, comUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("经营异常信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpBlackInfoQuery(WebClient client, ComUrl comUrl) {
		boolean result = true;

		try {
			// 获取严重违法信息
			String url = "http://www.jsgsj.gov.cn:58888/ecipplatform/publicInfoQueryServlet.json?queryYzwf=true"
					+ "&org=" + comUrl.getCorp_org() + "&id=" + comUrl.getCorp_id() + "&seqId="
					+ comUrl.getCorp_seq_id() + "&pageSize=10000&curPage=1";
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取严重违法信息结果非JSON格式:" + comUrl.toString());
				return false;
			}

			// 解析严重违法信息
			String data = JSON.parseObject(content).getString("data");
			List<BpBlackInfo> lstBpBlackInfo = new ArrayList<BpBlackInfo>();

			JSONArray yzwfJSONArray = JSON.parseArray(data);
			for (int i = 0; i < yzwfJSONArray.size(); i++) {
				JSONObject yzwfJSONObj = yzwfJSONArray.getJSONObject(i);

				String rn = yzwfJSONObj.getString("RN");
				String include_reason = yzwfJSONObj.getString("FACT_REASON");
				String include_date = yzwfJSONObj.getString("MARK_DATE");
				String include_authority = yzwfJSONObj.getString("MARK_ORG");
				String exclude_reason = yzwfJSONObj.getString("REMOVE_REASON");
				String exclude_date = yzwfJSONObj.getString("REMOVE_DATE");
				String exclude_authority = yzwfJSONObj.getString("REMOVE_ORG");

				BpBlackInfo bpBlackInfo = new BpBlackInfo();
				bpBlackInfo.setUuid(comUrl.getUuid());
				bpBlackInfo.setRn(rn);
				bpBlackInfo.setInclude_reason(include_reason);
				bpBlackInfo.setInclude_date(include_date);
				bpBlackInfo.setInclude_authority(include_authority);
				bpBlackInfo.setExclude_reason(exclude_reason);
				bpBlackInfo.setExclude_date(exclude_date);
				bpBlackInfo.setExclude_authority(exclude_authority);
				bpBlackInfo.setVersion(comUrl.getVersion() + 1);
				bpBlackInfo.setTs(sdf.format(new Date()));

				lstBpBlackInfo.add(bpBlackInfo);
			}

			if (lstBpBlackInfo.size() > 0) {
				// 保存严重违法信息
				if (1 != dbUtil.saveBpBlackInfoList(lstBpBlackInfo)) {
					sendMail("数据库异常", "严重违法信息保存失败:" + comUrl.toString());
					return false;
				}
			}

			// 更新公司Url参数状态
			result = updateComUrlPart(comUrl, 7);

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("严重违法信息抓取异常", e.getMessage() + ">>>>>>" + comUrl.toString());
			result = false;
		}
		return result;
	}

	/*
	 * 公司股东详情信息抓取任务
	 */
	public static void doInvestInfoTask() {
		CountableThreadPool threadPool = new CountableThreadPool(1);

		threadPool.execute(new Runnable() {
			public void run() {
				CountableThreadPool threadPool = new CountableThreadPool(threadnum);
				String member = null;

				while (true) {
					// 获取redis实例
					Jedis jedis = getRedis();

					try {
						if (comInfoIpLocked) {
							if (comInfoIP.equals(InetAddress.getLocalHost().getHostAddress())) {
								System.out.println("股东详情页面访问已被封...");
								for (int i = 0; i < 5; i++) {
									waitForComInfo();
								}
							}
							comInfoIpLocked = false;
						}

						if (threadPool.getThreadAlive() >= threadnum) {
							waitForComInfo();
							continue;
						}

						// 获取公司bpInvestInfo
						member = jedis.spop(getRedisInvestInfoKey(task));
						if (StringUtils.isBlank(member)) {
							System.out.println("无股东信息，请等待...");

							waitForComInfo();
							continue;
						}

						final WebClient clientFinal = getClient();
						final BpInvestInfo bpInvestInfo = JSON.parseObject(member, BpInvestInfo.class);

						threadPool.execute(new Runnable() {
							public void run() {
								// 获取redis实例
								Jedis jedis = getRedis();

								try {
									if (false == processBpInvestDetailTask(clientFinal, bpInvestInfo)) {
										String memberInvestInfo = JSON.toJSONString(bpInvestInfo);
										jedis.sadd(getRedisInvestInfoKey(task), memberInvestInfo);
									}

								} catch (Exception e) {
									e.printStackTrace();

									if (jedis != null) {
										String memberInvestInfo = JSON.toJSONString(bpInvestInfo);
										jedis.sadd(getRedisInvestInfoKey(task), memberInvestInfo);
									}
								} finally {
									returnRedis(jedis);
									returnClient(clientFinal);
								}
							}
						});

						waitForComInfo();

					} catch (Exception e) {
						e.printStackTrace();

						if (jedis != null) {
							if (StringUtils.isNotBlank(member)) {
								jedis.sadd(getRedisInvestInfoKey(task), member);
							}
						}

					} finally {
						returnRedis(jedis);
					}
				}
			}
		});
	}

	/*
	 * 股东详细信息爬虫抓取
	 */
	public static boolean processBpInvestDetailTask(WebClient client, BpInvestInfo bpInvestInfo) {
		boolean result = true;

		try {
			switch (bpInvestInfo.getPart()) {
			case 0:// 从认缴额开始
			{
				result = processBpInvestSubscribedQuery(client, bpInvestInfo);
				break;
			}
			case 1:// 从实缴额开始
			{
				result = processBpInvestPaidQuery(client, bpInvestInfo);
				break;
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendMail("股东详情信息抓取异常", e.getMessage() + ">>>>>>" + bpInvestInfo.toString());
			result = false;

		}
		return result;
	}

	public static boolean processBpInvestSubscribedQuery(WebClient client, BpInvestInfo bpInvestInfo) {
		boolean result = true;

		try {
			// 获取认缴出资信息
			String url = bpInvestInfo.getDetail_url() + "&type=rj";
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("股东详情信息异常", "获取认缴出资信息结果非JSON格式:" + bpInvestInfo.toString());
				return false;
			}

			// 解析认缴出资信息
			String data = JSON.parseObject(content).getString("data");
			List<BpInvestSubscribed> lstBpInvestSubscribed = new ArrayList<BpInvestSubscribed>();

			JSONArray rjJSONArray = JSON.parseArray(data);
			for (int i = 0; i < rjJSONArray.size(); i++) {
				JSONObject rjJSONObj = rjJSONArray.getJSONObject(i);

				// 认缴出资信息解析
				String subscribed_mode = rjJSONObj.getString("INVEST_TYPE_NAME");
				String subscribed_amount = rjJSONObj.getString("SHOULD_CAPI");
				String subscribed_date = rjJSONObj.getString("SHOULD_CAPI_DATE");

				BpInvestSubscribed bpInvestSubscribed = new BpInvestSubscribed();
				bpInvestSubscribed.setUuid(bpInvestInfo.getUuid());
				bpInvestSubscribed.setDetail_url(bpInvestInfo.getDetail_url());
				bpInvestSubscribed.setSubscribed_mode(subscribed_mode);
				bpInvestSubscribed.setSubscribed_amount(subscribed_amount);
				bpInvestSubscribed.setSubscribed_date(subscribed_date);
				bpInvestSubscribed.setVersion(bpInvestInfo.getVersion());
				bpInvestSubscribed.setTs(sdf.format(new Date()));

				lstBpInvestSubscribed.add(bpInvestSubscribed);
			}

			if (lstBpInvestSubscribed.size() > 0) {
				// 保存认缴出资信息
				if (1 != dbUtil.saveBpInvestSubscribedList(lstBpInvestSubscribed)) {
					sendMail("数据库异常", "认缴出资信息保存失败:" + bpInvestInfo.toString());
					return false;
				}
			}

			// 更新股东信息part参数状态
			result = updateBpInvestInfoPart(bpInvestInfo, 1);

			// 下一步处理实缴出资信息
			if (result) {
				result = processBpInvestPaidQuery(client, bpInvestInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("认缴出资信息抓取异常", e.getMessage() + ">>>>>>" + bpInvestInfo.toString());
			result = false;
		}
		return result;
	}

	public static boolean processBpInvestPaidQuery(WebClient client, BpInvestInfo bpInvestInfo) {
		boolean result = true;

		try {
			// 获取实缴出资信息
			String url = bpInvestInfo.getDetail_url() + "&type=sj";
			WebRequest request = new WebRequest(new URL(url));
			request.setCharset("utf-8");
			Page page = client.getPage(request);
			String content = getPageContent(page);
			if (false == checkComUrlContent(content)) {
				return false;
			}
			if (!JsonUtils.isJSONArrayStr(content)) {
				sendMail("工商信息异常", "获取实缴出资信息结果非JSON格式:" + bpInvestInfo.toString());
				return false;
			}

			// 解析实缴出资信息
			String data = JSON.parseObject(content).getString("data");
			List<BpInvestPaid> lstBpInvestPaid = new ArrayList<BpInvestPaid>();

			JSONArray sjJSONArray = JSON.parseArray(data);
			for (int i = 0; i < sjJSONArray.size(); i++) {
				JSONObject sjJSONObj = sjJSONArray.getJSONObject(i);

				// 实缴出资信息解析
				String paid_mode = sjJSONObj.getString("INVEST_TYPE_NAME");
				String paid_amount = sjJSONObj.getString("REAL_CAPI");
				String paid_date = sjJSONObj.getString("REAL_CAPI_DATE");

				BpInvestPaid bpInvestPaid = new BpInvestPaid();
				bpInvestPaid.setUuid(bpInvestInfo.getUuid());
				bpInvestPaid.setDetail_url(bpInvestInfo.getDetail_url());
				bpInvestPaid.setPaid_mode(paid_mode);
				bpInvestPaid.setPaid_amount(paid_amount);
				bpInvestPaid.setPaid_date(paid_date);
				bpInvestPaid.setVersion(bpInvestInfo.getVersion());
				bpInvestPaid.setTs(sdf.format(new Date()));

				lstBpInvestPaid.add(bpInvestPaid);
			}

			if (lstBpInvestPaid.size() > 0) {
				// 保存实缴出资信息
				if (1 != dbUtil.saveBpInvestPaidList(lstBpInvestPaid)) {
					sendMail("数据库异常", "实缴出资信息保存失败:" + bpInvestInfo.toString());
					return false;
				}
			}

			// 更新股东信息part参数状态
			result = updateBpInvestInfoPart(bpInvestInfo, 2);

		} catch (Exception e) {
			e.printStackTrace();
			sendMail("实缴出资信息抓取异常", e.getMessage() + ">>>>>>" + bpInvestInfo.toString());
			result = false;
		}

		return result;
	}

}
