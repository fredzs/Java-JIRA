package Benmu.jira.common;

import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * 发送邮件的测试程序
 * 
 * @author lwq
 * 
 */
public class EmailUtil {
	private static Logger logger = Logger.getLogger(EmailUtil.class);
    public void sendEmail(String subject, String content) throws MessagingException {
        // 配置发送邮件的环境属性
        final Properties props = new Properties();
        
        /*
         * 可用的属性： mail.store.protocol / mail.transport.protocol / mail.host /
         * mail.user / mail.from
         */
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.host", "smtp.163.com");
        props.put("mail.smtp.host", Configuration.getValMap().get("emailSMTP"));
        //props.put("mail.smtp.port", "465");
        // 发件人的账号
        props.put("mail.user", Configuration.getValMap().get("emailFrom"));
        // 访问SMTP服务时需要提供的密码
        props.put("mail.password", Configuration.getValMap().get("emailPassword"));

        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        
        // 设置优先级
        message.setHeader("X-Priority", "1"); 
        // 设置发件人
        InternetAddress form = new InternetAddress(
                props.getProperty("mail.user"));
        message.setFrom(form);
        
        // 设置收件人
        //InternetAddress[] to = new InternetAddress("zhangsheng@benmu-health.com;chentianqi@benmu-health.com");
        List<String> toList = Configuration.getEmailToList();
        InternetAddress[] toArray = new InternetAddress[toList.size()];
        for (int i = 0; i < toList.size(); i++){ 
        	toArray[i] = new InternetAddress(toList.get(i)); 
        } 
        message.setRecipients(RecipientType.TO, toArray);

        // 设置抄送
        List<String> ccList = Configuration.getEmailCcList();
        if (!ccList.get(0).equals("")) {
        	InternetAddress[] ccArray = new InternetAddress[ccList.size()];
	        for (int i = 0; i < ccList.size(); i++){ 
	        	ccArray[i] = new InternetAddress(ccList.get(i)); 
	        } 
	        message.setRecipients(RecipientType.CC, ccArray);
        }

        // 设置密送，其他的收件人不能看到密送的邮件地址
        //InternetAddress bcc = new InternetAddress("aaaaa@163.com");
        //message.setRecipient(RecipientType.CC, bcc);

        // 设置邮件标题
        message.setSubject(subject);

        // 设置邮件的内容体
        message.setContent(content, "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
        logger.info("发送邮件成功。");
        logger.info("收件人为：" + toList + "抄送人为：" + ccList);
    }
}
//
//import java.util.Calendar;
//import java.util.Properties;
//
//import javax.mail.Authenticator;
//import javax.mail.MessagingException;
//import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.Message.RecipientType;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//
//public class EmailUtil {
//	@SuppressWarnings("static-access")
//	public static void sendMessage(String smtpHost, String from, String fromUserPassword, String to, String subject, String messageText, String messageType) throws MessagingException {
//		// 第一步：配置javax.mail.Session对象
//		System.out.println("为" + smtpHost + "配置mail session对象");
//		
//		Properties props = new Properties();
//		props.put("mail.smtp.host", smtpHost);
//		//props.put("mail.smtp.starttls.enable","true");//使用 STARTTLS安全连接
//		props.put("mail.smtp.port", "465");			 //google使用465或587端口
//		props.put("mail.smtp.auth", "true");		// 使用验证
//		//props.put("mail.debug", "true");
//		Session mailSession = Session.getInstance(props,new MyAuthenticator(from,fromUserPassword));
//
//		// 第二步：编写消息
//		System.out.println("编写消息from——to:" + from + "——" + to);
//
//		InternetAddress fromAddress = new InternetAddress(from);
//		InternetAddress toAddress = new InternetAddress(to);
//
//		MimeMessage message = new MimeMessage(mailSession);
//
//		message.setFrom(fromAddress);
//		message.addRecipient(RecipientType.TO, toAddress);
//
//		message.setSentDate(Calendar.getInstance().getTime());
//		message.setSubject(subject);
//		message.setContent(messageText, messageType);
//
//		// 第三步：发送消息
//		Transport transport = mailSession.getTransport("smtp");
//		transport.connect(smtpHost,"zhangsheng", fromUserPassword);
//		transport.send(message, message.getRecipients(RecipientType.TO));
//		System.out.println("message yes");
//	}
//
//	public static void main(String[] args) {
//		try {
//			EmailUtil.sendMessage("smtp.exmail.qq.com", "zhangsheng@benmu-health.com",
//					"Fred1234", "fred_zs@163.com", "nihao",
//					"---------------wrwe-----------",
//					"text/html;charset=gb2312");
//		} catch (MessagingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
//class MyAuthenticator extends Authenticator{
//	String userName="";
//	String password="";
//	public MyAuthenticator(){
//		
//	}
//	public MyAuthenticator(String userName,String password){
//		this.userName=userName;
//		this.password=password;
//	}
//	 protected PasswordAuthentication getPasswordAuthentication(){   
//		return new PasswordAuthentication(userName, password);   
//	 } 
//}
