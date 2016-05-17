package Benmu.jira;

import Benmu.jira.Service;
import Benmu.jira.common.Configuration;
import Benmu.jira.common.Email;
import Benmu.jira.common.JIRAUtil;

import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import javax.mail.MessagingException;


public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException, MessagingException, ParseException {
    	//DOMConfigurator.configureAndWatch("log4j.xml");
    	// log4j.xml文件并不会被一起打包，运行jar包前应该将其拷贝至jar包同级目录下。
    	String path = Main.class.getClass().getResource("/").getPath();  
        System.out.println("log4j configfile path =" + path + "log4j.xml");

        // 对应的配置文件为config.properties，默认情况下该文件应该处于classes下，当指定包名Benmu.jira和资源名config时，可以指定包内任意路径，如下所示。
        //Configuration.getAllMessage("Benmu.jira.config");
        // 按照以下的配置，config.properties文件不会被一起打包，运行jar包前应该将其拷贝至jar包同级目录下。
        Configuration.getAllMessage("config");
        System.out.println("jira configfile path =" + path + "config.properties");
    	JIRAUtil.init(Configuration.getValMap().get("jiraUserName"), Configuration.getValMap().get("jiraPassword"), Configuration.getValMap().get("jiraBaseURL"));
    	
    	Service service = new Service();
    	service.init();
	    service.showTotalInfo();
    	service.showSomedayCreatedIssues();
    	service.showSomedayResolvedIssues();
    	service.showSomedayClosedIssues();
    	service.showUnsolvedAssignee();
    	Email email = new Email();
    	email.sendDaily();
    	System.exit(0); 
    	return;
    }
}