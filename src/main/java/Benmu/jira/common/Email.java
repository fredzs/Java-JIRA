package Benmu.jira.common;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

public class Email {
	private static String subject = "";
	private static String content = "";
	private static Logger logger = Logger.getLogger(Email.class);
	public void sendDaily() throws MessagingException{
		EmailUtil email = new EmailUtil();
	    subject = "JIRA每日通知邮件_" + Configuration.getValMap().get("endDate");
        email.sendEmail(subject, content);
		logger.debug(content);
	}

	public static String getSubject() {
		return subject;
	}

	public static void setSubject(String subject) {
		Email.subject = subject;
	}

	public static String getContent() {
		return content;
	}
	
	public static void setContent(String content) {
		Email.content = content;
	}
	
	public static void appendContent(String content) {
		Email.content += content;
	}
}
