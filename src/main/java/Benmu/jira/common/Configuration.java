package Benmu.jira.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Configuration {
	private static Map<String, String> valMap = new HashMap<String, String>();
	private static List<String> emailToList = new ArrayList<String>();
	private static List<String> emailCcList = new ArrayList<String>();
	private static List<String> projectList = new ArrayList<String>();
	public static Map<String, String> getAllMessage(String propertyName) {  
	    // 获得资源包  
	    ResourceBundle rb = ResourceBundle.getBundle(propertyName.trim(), Locale.getDefault());  
	    // 通过资源包拿到所有的key  
	    Enumeration<String> allKey = rb.getKeys();  
	    // 遍历key 得到 value  
	    while (allKey.hasMoreElements()) {  
	        String key = allKey.nextElement();  
	        String value = (String) rb.getString(key);  
	        valMap.put(key, value);
	    }
	    // 收件人和抄送人都支持多个，用分号分割
	    String to=valMap.get("emailTo");
	    String[] toArray=to.split(";");
	    Collections.addAll(emailToList, toArray);

	    String cc=valMap.get("emailCc");
	    String[] ccArray=cc.split(";");
	    Collections.addAll(emailCcList, ccArray);
	    
	    String pl=valMap.get("projectList"); 
	    String[] plArray=pl.split(";"); 
	    Collections.addAll(projectList, plArray);
	    
	    if (valMap.get("endDate").equals("")) {
	    	Date date = new Date();
	    	SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		    String endDate=df.format(date);
			valMap.put("endDate", endDate);
		}
	    
	    return valMap;  
	}

	public static Map<String, String> getValMap() {
		return valMap;
	}

	public static List<String> getEmailToList() {
		return emailToList;
	}

	public static List<String> getEmailCcList() {
		return emailCcList;
	}

	public static List<String> getProjectList() {
		return projectList;
	}  
	
}
