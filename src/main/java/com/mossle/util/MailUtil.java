package com.mossle.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailUtil {

	public static final String CONTENT_PART_TYPE = "text/html;charset=utf-8";
	//发送邮件协议
	public static final  String PROTOCOL = "smtp";
	//是否经过认证 
	public static final  String AUTH ="true";
	//邮箱服务器地址
	public static final String POP3SERVER = "mail.rolmex.cn";
	//协议
	public static final	String pop3_PROTOCOL = "pop3";
	
	/**
	 * 
	 * @param host 邮件服务器地址
	 * @param protocol 发送邮件协议
	 * @param auth 是否经过认证 
	 * @param userName 用户名
	 * @param passWord	密码
	 * @param toUserName 接收人账号，多个账号用“,”隔开
	 * @param ccUserName 抄送人账号，多个账号用“,”隔开
	 * @param bccUserName 密送人账号，多个账号用“,”隔开
	 * @param subject 标题
	 * @param SendHtml html格式正文
	 * @param files 附件
	 * 
	 * @方法名: sendEmail  
	 * @功能描述: 发送邮件
	 * @作者  ckx
	 * @日期 2018年12月04日
	 */
	public static boolean sendEmail(String host,String userName,String passWord,String toUserName,String ccUserName,String bccUserName,String subject,String sendHtml,File [] files){
		/**
		 * 1、通过session创建邮件的配置信息
		 */
		try {
		// 创建配置对象
		Properties props = new Properties();
		//邮件服务器地址
		props.setProperty("mail.host", host);
		//发送邮件协议
		props.setProperty("mail.transport.protocol", PROTOCOL);
		// 要经过认证
		props.setProperty("mail.smtp.auth", AUTH);
		// 创建session
		Session session = Session.getInstance(props);
		// 使我们在发送邮件的过程中，看到邮件发送的状态信息
		session.setDebug(false);
		/**
		 * 2、创建代表邮件内容的Message对象（JavaMail创建的邮件是基于MIME协议的）
		 */
		Message msg = new MimeMessage(session);
		// 设置发件人
		msg.setFrom(new InternetAddress(userName));
		// 设置标题
		msg.setSubject(subject);
		// 设置收件人
		if(null != toUserName && !toUserName.isEmpty()){
			InternetAddress[] internetAddressTo = new InternetAddress().parse(toUserName);
			msg.setRecipients(Message.RecipientType.TO, internetAddressTo);
		}
		// 设置抄送人
		if(null != ccUserName && !ccUserName.isEmpty()){
			InternetAddress[] internetAddressCc = new InternetAddress().parse(ccUserName);
			msg.setRecipients(Message.RecipientType.CC, internetAddressCc);
		}
		// 设置密送人
		if(null != ccUserName && !ccUserName.isEmpty()){
			InternetAddress[] internetAddressBCC = new InternetAddress().parse(bccUserName);
			msg.setRecipients(Message.RecipientType.BCC, internetAddressBCC);
		}
		// 设置正文
		// 复合的内容，包含附件和正文
		Multipart multipart = new MimeMultipart();
		// ① 正文部分
		BodyPart bodyPart = new MimeBodyPart();
		// 正文部分和msg.setContent
		bodyPart.setContent(sendHtml, CONTENT_PART_TYPE);
		multipart.addBodyPart(bodyPart);
		// ② 附件部分
		// DataHandler FileDataSource，参数File对象
		 // 添加附件的内容
        if (null != files && files.length != 0) {
        	for (File file : files) {
        		bodyPart = new MimeBodyPart();
        		DataSource source = new FileDataSource(file);
        		bodyPart.setDataHandler(new DataHandler(source));
        		//MimeUtility.encodeWord可以避免文件名乱码
        		// 文件名设置bodyPart.setFileName，不然文件名就不你控制的
        		bodyPart.setFileName(MimeUtility.encodeWord(file.getName()));
                multipart.addBodyPart(bodyPart);
			}
        }
		// 把multipart放入到message对象当中
		msg.setContent(multipart);
		/**
		 * 3、创建Transport对象、连接服务器、发送Message、关闭连接
		 */
		Transport tran = session.getTransport();
		tran.connect(host, userName, passWord);
		// 获取到message对象中的收件人信息，msg.getAllRecipients()
		tran.sendMessage(msg, msg.getAllRecipients());
		// 关闭连接
		tran.close();
		return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 检验用户名，密码是否正确
	 * @param mailServer
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public static boolean checkedPassword(String mailServer,String userName,String passWord){
 		
 		// 连接pop3服务器的主机名、协议、用户名、密码

 		// 创建一个有具体连接信息的Properties对象
 		Properties props = new Properties();
 		props.setProperty("mail.store.protocol", pop3_PROTOCOL);//PROTOCOL
 		props.setProperty("mail.pop3.host", mailServer);
 		
 		Session session = Session.getInstance(props);
 		session.setDebug(false);

 		// 利用Session对象获得Store对象，并连接pop3服务器
		try {
			Store store = session.getStore();
			store.connect(mailServer, userName, passWord);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 适用于腾讯个人邮箱验证
	 * @param host
	 *            SMTP服务端地址,如qq邮箱为smtp.qq.com
	 * @param email
	 *            邮箱名
	 * @param password
	 *            邮箱注册码(非登录名,具体需根据邮箱到官网申请)
	 * @return 如果可用返回true
	 * @throws MessagingException
	 */
	public static boolean checkEmail(String host, String email, String password)
			throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.debug", "true");
		final String smtpPort = "465";
		props.setProperty("mail.smtp.port", smtpPort);
		props.setProperty("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.socketFactory.port", smtpPort);
		Session session = Session.getDefaultInstance(props);
		session.setDebug(false);
		Transport transport = session.getTransport();
		try {
			transport.connect(email, password);
			return true;
		} catch (MessagingException e) {
			return false;
		} finally {
			transport.close();
		}
	}
	
	
	public static void main(String[] args) {
		
		boolean checkedPassword = checkedPassword("pop.163.com","dwcx_test@163.com", "dw111111");
		System.out.println(checkedPassword);
		/*File file = new File("E:\\testexcel\\麦联-流程改进项目计划.xls");
		File[] files = {file};*/
		/*String host = "smtp.163.com";
		String userName = "chenkaixuan_dw@163.com";
		String passWord = "dw111111";
		String toUserName = "";
		String ccUserName = "";
		String bccUserName = "";
		String subject = "这是标题";
		String sendHtml = "正文内容";
		sendEmail(host, userName, passWord, toUserName, ccUserName, bccUserName, subject, sendHtml, null);*/
		
		
		/*List<String> list = new ArrayList<String>();
		
		list.add("594486653@qq.com");
		list.add("1342898610@qq.com");
		list.add("382985196@qq.com");
		list.add("641426490@qq.com");
		list.add("llfrog@sohu.com");
		list.add("lileipj521@sohu.com");
		list.add("lileipj521@qq.com");
		list.add("13611087540@163.com");
		list.add("83748592@qq.com");
		list.add("lilei@rolmex.cn");
		list.add("314025035@qq.com");
		list.add("648435239@qq.com");
		list.add("lindyhegang@163.com");
		list.add("15811549066@163.com");
		list.add("594411186653@qq.com");
		list.add("594486777653@qq.com");
		list.add("5944899996653@qq.com");
		list.add("songjie@rolmex.cn");
		list.add("469386516@qq.com");
		list.add("chenkaixuan_dw@163.com");
		list.add("dwcx_test@163.com");
		list.add("1623608455@qq.com");
		list.add("870098722@qq.com");
		list.add("343258405@qq.com");
		list.add("1158037769@qq.com");
		list.add("32037535@qq.com");
		list.add("763180824@qq.com");
		list.add("594486688853@qq.com");
		list.add("5944866522223@qq.com");
		list.add("5944222286653@qq.com");
		
		
		
		
		
		int fail = 0;
		int secc = 0;
		for (String string : list) {
			String host = "mail.rolmex.cn";
			String userName = "dwcx_hr@rolmex.cn";
			String passWord = "rm666666";
			String toUserName = string;
			String ccUserName = "";
			String bccUserName = "";
			String subject = "测试邮件";
			String sendHtml = "正文内容";
			boolean boo = sendEmail(host, userName, passWord, toUserName, ccUserName, bccUserName, subject, sendHtml, null);
			if(!boo){
				fail++;
				continue;
			}else{
				secc++;
			}
		}
		System.out.println("[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]"+fail);
		System.out.println("[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]"+secc);
		*/
		
		
	}
}
