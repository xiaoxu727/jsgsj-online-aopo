package com.champion.jsgsj_online_aopo;

import com.champion.webspider.utils.CommUtils;
import com.champion.webspider.utils.RedisUtils;
import redis.clients.jedis.Jedis;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Mail {
	public static final String ENCODEING = "UTF-8";
	private String sender; // 发件人的邮箱
	private String receiver; // 收件人的邮箱
	private String name; // 发件人昵称
	private String username; // 账号
	private String password; // 密码
	private String subject; // 主题
	private String message; // 信息(支持HTML)
	private String filePath; // 文件路径

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public static void sendMail(final Mail mail) throws AddressException, MessagingException {

		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "smtp.qiye.163.com");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.auth", "true");

		Session session = Session.getDefaultInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mail.getUsername(), mail.getPassword());
			}
		});
		session.setDebug(false);

		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(mail.getName()));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getReceiver(), false));
		msg.setSubject(mail.getSubject());
		msg.setText(mail.getMessage());
		msg.setSentDate(new Date());
		if (mail.getFilePath() != null && !"".equals(mail.getFilePath())) {
			msg.setFileName(mail.getFilePath());
		}
		Transport.send(msg);
	}

	public static void sendMessage(String type, String message) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		System.out.println(sdf.format(new Date()) + ":" + type + ">>>>>>" + message);
		CommUtils.SaveError(sdf.format(new Date()) + ":" + type + ">>>>>>" + message);
	}

	public static void sendMail(Jedis jedis, String task, String email_exception_history, String email_list, String subject, String message) {
		if (message.contains("Read timed out")) {
			return;
		}

		String task_email_exception_history = task + ":" + email_exception_history;
//		Jedis jedis = RedisUtils.getJedis();
		try {
//			每一个错误只发送一次邮件，一个小时更新一次
			if (!jedis.exists(task_email_exception_history)) {
				jedis.sadd(task_email_exception_history, "test");
				jedis.expire(task_email_exception_history, 3600);
			}
			if (!jedis.sismember(task_email_exception_history, message)) {
				Mail mail = new Mail();
				mail.setUsername("message@champion-credit.com");
				mail.setName("message@champion-credit.com");
				mail.setPassword("WinChampion2017");
				mail.setReceiver(email_list);
				mail.setSubject(subject);
				mail.setMessage(task + "_" + message);
				Mail.sendMail(mail);
				jedis.sadd(task_email_exception_history, message);
			}
			sendMessage(subject, message);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			RedisUtils.returnResource(jedis);
		}
	}

}
