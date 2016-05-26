package Benmu.jira;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;

import Benmu.jira.common.*;
public class Service {
	private static Logger logger = Logger.getLogger(Service.class);
	
	public void init() {
		JIRAUtil.updateAllProjectList();
		JIRAUtil.updateProjectKeyList();
		// 当没有在配置文件中指定查询的project时，默认查询全部project。
		if (Configuration.getProjectList().size() != 0) {
			JIRAUtil.makeProjectQuerySet();
		}
		JIRAUtil.updateIssueMap();
		logger.info("完成初始化，更新了需要统计的Project和Issue列表。");
	}
	
	private String makeContentForSimpleIssues(String title, List<SimpleIssue> simpleIssues, String JQL){
		String content;
		int total = simpleIssues.size();
		String issuesLink = Configuration.getValMap().get("jiraBaseURL") + "issues/?jql=";
		content = title + total + "个， <a href='" + issuesLink + JQL + "'>" + "点击链接跳转至Jira查看详情。</a><br />";
		content += "<br />";
		if (total > 0) {
			content += "<table border=\"1\" style= \"border-collapse: collapse; border-color: #BCD1E6;\"><tbody><tr style= \"border-color: #9AA2A9;\">";
			content += "<td width=\"50\" align=\"center\"><B>序号</B></td>";
			content += "<td width=\"80\" align=\"center\"><B>优先级</B></td>";
			content += "<td width=\"500\" align=\"center\"><B>标题</B></td>";
			content += "<td width=\"120\" align=\"center\"><B>创建时间</B></td>";
			content += "<td width=\"80\" align=\"center\"><B>经办人</B></td>";
			content += "<td width=\"80\" align=\"center\"><B>状态</B></td></tr>";
			String browseLink = Configuration.getValMap().get("jiraBaseURL") + "browse/";
	        for (int i = 0; i < total; i++) {
	        	SimpleIssue simpleIssue = simpleIssues.get(i);
	        	
	        	// 序号：
	        	String link = browseLink + simpleIssue.getKey();
	        	int num = i + 1;
	        	content += "<tr><td align=\"center\">" + num + "</td>";
	        	
	        	// 优先级：
	        	content += "<td align=\"center\">" + simpleIssue.getPriority().getName() + "</td>";
	
	        	// 标题：
	        	content += "<td width=\"500\" ><a href='" + link + "'>";
	        	content += "[" + simpleIssue.getKey() + "]";
	        	String summary = simpleIssue.getSummary();
	        	if (summary.length() <= 25) {
	        		content += summary + "\n<br />\n<br />";
				} else if (summary.length() <= 50) {
	        		content += summary;
				} else {
					content += summary.substring(0, 50) + "...";
				}
	        	content += "</a></td>";
	        	
	        	// 创建时间：
		    	SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
				String createDate=df.format(simpleIssue.getCreationDate().toDate());
	        	content += "<td align=\"center\">" + createDate + "</td>";
	        	
	        	// 经办人：
	        	String userLink = issuesLink + "assignee= " + simpleIssue.getAssignee().getName() + " AND " + JQL;
	        	content += "<td align=\"center\"><a href='" + userLink + "'>" + simpleIssue.getAssignee().getDisplayName() + "</a></td>";
	        	
	        	// 状态：
	        	String status = simpleIssue.getStatus().getName();
	        	if (status.equals("开始") || status.equals("进行中")) {
	        		content += "<td align=\"center\">" + "待解决" + "</td>";
				} else if (status.equals("关闭")) {
					content += "<td align=\"center\">" + "已关闭" + "</td>";
				} else {
					content += "<td align=\"center\">" + "已解决" + "</td>";
				}
	        	
	        	content += "</tr>";
			}
	        content += "</tbody></table>";
		}
        return content;
	}
	
	private List<SimpleIssue> makeSimpleIssuesFromJQL(String JQL) throws InterruptedException, ExecutionException{
		Iterable<BasicIssue> issueList = JIRAUtil.findIssuesByJQL(JQL).getIssues();
		Iterator<BasicIssue> it = issueList.iterator();
		List<SimpleIssue> simpleIssues = new ArrayList<SimpleIssue>();
        while (it.hasNext()) {
        	BasicIssue basicIssue = it.next();
        	Issue issue = JIRAUtil.findIssueByIssueKey(basicIssue.getKey());
        	SimpleIssue simpleIssue = new SimpleIssue(issue);
        	simpleIssues.add(simpleIssue);
        }
        return simpleIssues;
	}
	
	private String makeContentForIssues(String title, List<String> IssuesKeyList, List<String> IssuesNameList, String JQL){
		String content;
		int todayTotal = IssuesKeyList.size();
		content = title + todayTotal + "个。" + "<br />";
        for (int i = 0; i < todayTotal; i++) {
        	String link = Configuration.getValMap().get("jiraBaseURL") + "browse/" + IssuesKeyList.get(i);
        	content += i+1 + ".";
        	content += "<a href='" + link + "'>";
        	content += "[" + IssuesKeyList.get(i) + "]" + IssuesNameList.get(i) ;
        	content += "</a>";
        	content += "<br />";
		}
        String issuesLink = Configuration.getValMap().get("jiraBaseURL") + "issues/?jql=" + JQL;
        content += "<br /><a href='" + issuesLink + "'>" + "点击链接查看详情。</a><br />";
        return content;
	}
	
	public void showTotalInfo() {
		String content;
		content = "<h2>一、问题总数统计</h2>";
		content += "当前JIRA系统中共有项目" + JIRAUtil.getAllProjectMap().size() + "个，详情请见：";
        String link = Configuration.getValMap().get("jiraBaseURL") + "secure/BrowseProjects.jspa";
        content += "<a href='" + link + "'>";
    	content += "点击链接查看所有项目。";
    	content += "</a>";
    	content += "<br />";
    	content += "需要统计的项目有" + JIRAUtil.getProjectKeyList().size() + "个，详情如下：<br />";
    	content += "<br />";
    	Email.appendContent(content);
    	try {
    		showAllTotalIssues();
    	} catch (ExecutionException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (InterruptedException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
	}
	
	public void showAllTotalIssues() throws ExecutionException, InterruptedException{
		int count = 0;
		double percent = 0;
		String content = "<h2>一、问题总数统计</h2>";;
		content += "<table border=\"1\" style= \"border-collapse: collapse; border-color: #BCD1E6;\"><tbody><tr style= \"border-color: #9AA2A9;\">";
		content += "<td width=\"120\" align=\"center\"><B>项目名称</B></td>";
		content += "<td width=\"120\" align=\"center\"><B>项目Issue总数</B></td>";
		content += "<td width=\"150\" align=\"center\"><B>待解决（百分比）</B></td>";
		content += "<td width=\"150\" align=\"center\"><B>已解决（百分比）</B></td>";
		content += "<td width=\"150\" align=\"center\"><B>已关闭（百分比）</B></td>";
		content += "<td width=\"100\" align=\"center\"><B>今日新建</B></td>";
		content += "<td width=\"100\" align=\"center\"><B>今日解决</B></td>";
		content += "<td width=\"100\" align=\"center\"><B>今日关闭</B></td></tr>";
		NumberFormat nt = NumberFormat.getPercentInstance();
	    nt.setMinimumFractionDigits(2);
	    
		for (int i = 0; i < JIRAUtil.getProjectKeyList().size(); i++) {
			String projectKey = JIRAUtil.getProjectKeyList().get(i);
			
			// 项目名称：
			String link = Configuration.getValMap().get("jiraBaseURL") + "browse/" + projectKey + "/?selectedTab=com.atlassian.jira.jira-projects-plugin:issues-panel";
			content += "<tr><td align=\"center\"><a href='" + link + "'>";
			String projectName = JIRAUtil.getAllProjectMap().get(projectKey).getName();
			//content += "[" + projectKey + "]" ;
			content += projectName;
			content += "</a></td>";
			
			// 项目问题总数：
			int totalIssues = JIRAUtil.getIssueMap().get(projectKey).size();
			content += "<td align=\"center\">" + totalIssues + "</td>";
			
			// 待解决（百分比）：
			String JQL1 = "status in (open, \"In Progress\") AND project = \"" + projectKey + "\" AND createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"";
			logger.info("待解决，查询语句：" + JQL1);
			SearchResult result1 = JIRAUtil.findIssuesByJQL(JQL1);
			count = result1.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "（" + nt.format(percent) + "）</td>";
        	
        	// 已解决（百分比）
        	String JQL2 = "status in (Resolved) AND project = \"" + projectKey + "\" AND createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"";
			logger.info("已解决，查询语句：" + JQL2);
			SearchResult result2 = JIRAUtil.findIssuesByJQL(JQL2);
			count = result2.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "（" + nt.format(percent) + "）</td>";
        	
        	// 已关闭（百分比）
        	String JQL3 = "status in (Closed) AND project = \"" + projectKey + "\" AND createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"";
			logger.info("已关闭，查询语句：" + JQL3);
			SearchResult result3 = JIRAUtil.findIssuesByJQL(JQL3);
			count = result3.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "（" + nt.format(percent) + "）</td>";
        	
        	// 今日新建：
        	String JQL4 = "project = \"" + projectKey + "\" AND createdDate > \"" + Configuration.getValMap().get("endDate") + "\"";
			logger.info("今日新建，查询语句：" + JQL4);
			SearchResult result4 = JIRAUtil.findIssuesByJQL(JQL4);
			count = result4.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "</td>";
        	
        	// 今日解决
        	String JQL5 = "status in (Resolved) AND project = \"" + projectKey + "\" AND resolutiondate > \"" + Configuration.getValMap().get("endDate") + "\"";
			logger.info("今日解决，查询语句：" + JQL5);
			SearchResult result5 = JIRAUtil.findIssuesByJQL(JQL5);
			count = result5.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "</td>";
        	
        	
        	// 今日关闭
        	String JQL6 = "status in (Closed) AND project = \"" + projectKey + "\" AND resolutiondate > \"" + Configuration.getValMap().get("endDate") + "\"";
			logger.info("今日关闭，查询语句：" + JQL6);
			SearchResult result6 = JIRAUtil.findIssuesByJQL(JQL6);
			count = result6.getTotal();
			percent = (double) count / totalIssues;
        	content += "<td align=\"center\">" + count + "</td>";
        	
        	content += "</tr>";
        	
		}
		content += "</tbody></table>";
		
		Email.appendContent(content);
	}
	
	public void showAllClosedIssues() throws ExecutionException, InterruptedException{
		int total = 0, totalAll = 0;
		double percent = 0;
		//String log = ""; 
		String content = "<table><tbody><tr><td align=\"center\"><B>项目名称</B></td><td width=\"200\" align=\"center\"><B>项目Issue总数</B></td><td width=\"200\" align=\"center\"><B>已关闭（百分比）</B></td><td width=\"100\" align=\"center\"><B>平均花费时间</B></td></tr>";
		NumberFormat nt = NumberFormat.getPercentInstance();
	    nt.setMinimumFractionDigits(2);
	    
		for (int i = 0; i < JIRAUtil.getProjectKeyList().size(); i++) {
			String projectKey = JIRAUtil.getProjectKeyList().get(i);
			String JQL = "status = closed AND project = \"" + projectKey + "\" AND createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"";
			logger.info("查询语句：" + JQL);
			SearchResult result = JIRAUtil.findIssuesByJQL(JQL);
			total = result.getTotal();
			percent = (double) total / JIRAUtil.getIssueMap().get(projectKey).size();
			totalAll += total;
						
			String link = Configuration.getValMap().get("jiraBaseURL") + "browse/" + projectKey + "/?selectedTab=com.atlassian.jira.jira-projects-plugin:issues-panel";
        	content += "<tr><td><a href='" + link + "'>";
        	String projectName = JIRAUtil.getAllProjectMap().get(projectKey).getName();
        	content += "[" + projectKey + "]" + projectName;
        	content += "</a></td>";
        	content += "<td align=\"center\">" + JIRAUtil.getIssueMap().get(projectKey).size() + "</td><td align=\"center\">" + total + "（" + nt.format(percent) + "）</td>";
        	String timeSpent = calculateTimeSpent(result.getIssues().iterator());
        	content += "</td><td align=\"center\">" + timeSpent + "</td>";
        	content += "</tr>";
		}
		if (JIRAUtil.getProjectKeyList().size() > 1) {
			percent = (double) totalAll / JIRAUtil.getTotalIssue();
			content += "<tr><td align=\"center\"><font color=red>总计</font></td><td align=\"center\"><font color=red>" + JIRAUtil.getTotalIssue() + "</font></td><td align=\"center\"><font color=red>" + totalAll + "（" + nt.format(percent) + "）</font></td><td></td></tr>";
		}
		content += "</tbody></table>";
		//content += "<br />（以上统计信息起止日期为：" + Configuration.getValMap().get("beginDate") + " - " + Configuration.getValMap().get("endDate") + "）<br />";
		Email.appendContent(content);
	}
	
	public void showUnresolvedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
		String JQL = "status in (open, \"In Progress\") AND created>=\"" + Configuration.getValMap().get("beginDate") + "\"" + JIRAUtil.getProjectQuerySet() + "ORDER BY created DESC";
		logger.info("目前待解决的问题，查询语句：" + JQL);
		Email.appendContent("<h2>二、目前待解决的问题</h2>");
		Email.appendContent(makeContentForSimpleIssues("目前待解决的问题共有", makeSimpleIssuesFromJQL(JQL), JQL));
	}
	
	public void showSomedayCreatedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
		String JQL = "created >= \"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet() + "ORDER BY created DESC";
		logger.info("今日新增的问题，查询语句：" + JQL);
        Email.appendContent("<h2>三、今日新增的问题</h2>");
        Email.appendContent(makeContentForSimpleIssues("今日新增的问题有", makeSimpleIssuesFromJQL(JQL), JQL));
	}
	
	public void showSomedayResolvedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
	    String JQL = "status in (Resolved) AND resolutiondate > \"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet() + "ORDER BY created DESC";
		logger.info("今日解决的问题，查询语句：" + JQL);
        Email.appendContent("<h2>四、今日解决的问题（即开发已修复待测试）</h2>");
        Email.appendContent(makeContentForSimpleIssues("今日解决的问题有", makeSimpleIssuesFromJQL(JQL), JQL));
	}
	
	public void showSomedayClosedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
	    String JQL = "status in (Closed) AND resolutiondate > \"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet() + "ORDER BY created DESC";
		logger.info("今日关闭的问题，查询语句：" + JQL);
        //String timeSpent = calculateTimeSpent(searchResult.getIssues().iterator());
        Email.appendContent("<h2>五、今日关闭的问题（即已上线或者不解决关闭）</h2>");
        Email.appendContent(makeContentForSimpleIssues("今日关闭的问题有", makeSimpleIssuesFromJQL(JQL), JQL));
        /*if (!nothingFound) {
        	Email.appendContent("<br />平均花费时间为" + timeSpent + "。");
		}*/	
	}
	
	public String calculateTimeSpent(Iterator<BasicIssue> it) {
		String timeSpent = "";
		long spentTime = 0;
		int total = 0;
		while (it.hasNext()) {
			String issueKey = it.next().getKey();
			try {
				Issue issue = JIRAUtil.findIssueByIssueKey(issueKey);
				String resolution = (String )issue.getField("resolutiondate").getValue();
				resolution = resolution.substring(0,19).replace('T', ' ');
				SimpleDateFormat df1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	Date resolutionDate = df1.parse(resolution);
		    	DateTime creationDate = issue.getCreationDate();
		    	long resolutionTime = resolutionDate.getTime();
		    	spentTime += resolutionTime - creationDate.getMillis();
		    	total++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		if (total > 0) {
			spentTime = spentTime / total;
			long minute = spentTime / 1000 / 60;
			long day = minute / 60 / 24;
			long hour = minute % ( 60 * 24 ) / 60;
			timeSpent = day + "天" + hour + "小时";
		} 
		return timeSpent;
	}
	
	@SuppressWarnings("rawtypes")
	public void showUnsolvedAssignee() throws InterruptedException, ExecutionException{
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
		String baseLink = Configuration.getValMap().get("jiraBaseURL");
		String JQL = "status not in (Closed,Done,Resolved) " + JIRAUtil.getProjectQuerySet() + " AND createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"";
		logger.info("查询语句：" + JQL);
		Iterable<BasicIssue> issueList = JIRAUtil.findIssuesByJQL(JQL).getIssues();
		
		Iterator<BasicIssue> it = issueList.iterator();
		Map<String, Integer> assigneeCount = new HashMap<String, Integer>();
		Map<String, String> assigneeDisplayName = new HashMap<String, String>();
		List<String> assigneeList = new ArrayList<String>();
		logger.info("[bonus!]漫长的等待，不过是为了更好地相遇...");
		int unassigned = 0;
		
		// 由于使用了Jira官方的RESTful API，可定制性较差，所以这个循环消耗的时间较长，遍历全部project需要大概2.5min，只便利BUGG大概0.5min。
        while (it.hasNext()) {
        	// Issue这个类的成员比较多，getIssue()这个方法较为耗时。
        	Issue issue = JIRAUtil.findIssueByIssueKey(it.next().getKey());
        	BasicUser assignee = issue.getAssignee();
        	if (assignee != null) {
        		assigneeList.add(issue.getAssignee().getName());
        		assigneeDisplayName.put(issue.getAssignee().getName(), issue.getAssignee().getDisplayName());
        	} else
        		unassigned++;
        }
        Iterator<String> itt = assigneeList.iterator();
        while (itt.hasNext()) {
        	String name = itt.next();
        	if (assigneeCount.containsKey(name)) {
				assigneeCount.put(name, assigneeCount.get(name) + 1);
			} else {
				assigneeCount.put(name, 1);
			}
		}

        String content = "<table><tbody><tr><td align=\"center\"><B>经办人</B></td><td width=\"100\" align=\"center\"><B>问题数</B></td></tr>";
        if (unassigned > 0) {
        	content += "<tr><td align=\"center\">未分配</td><td width=\"100\" align=\"center\">" + unassigned + "</td></tr>";
		}
        Map.Entry[] entries= getSortedHashtableByValue(assigneeCount);
        for (int i = 0; i < entries.length; i++) {
        	String userLink = baseLink + "secure/ViewProfile.jspa?name=" + entries[i].getKey();
        	content += "<tr><td align=\"center\"><a href='" + userLink + "'>";
        	content += assigneeDisplayName.get(entries[i].getKey());
        	content += "</a></td>";
        	String issueLink = baseLink + "issues/?jql=resolution = Unresolved" + JIRAUtil.getProjectQuerySet() + "AND assignee = " + entries[i].getKey();
        	content += "<td align=\"center\"><a href='" + issueLink + "'>";
        	content += entries[i].getValue();
        	content += "</a></td>";
        	
		}
        String issuesLink = baseLink + "issues/?jql=" + JQL;
        content += "<tr><td align=\"center\"><font color=red>总计</font></td><td align=\"center\"><a href='" + issuesLink + "'>";
        content += "<font color=red>" + assigneeList.size() + "</font></a></td>";
        content += "</tr></tbody></table>";
        //content += "<br />（以上统计信息起止日期为：" + Configuration.getValMap().get("beginDate") + " - " + Configuration.getValMap().get("endDate") + "）<br />";
        Email.appendContent("<h2>五、“未解决”问题经办人排行</h2>");
		Email.appendContent(content);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map.Entry[] getSortedHashtableByValue(Map map) {
		Set set = map.entrySet();
		Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
		Arrays.sort(entries, new Comparator() {
		public int compare(Object arg0, Object arg1) {
		Long key1 = Long.valueOf(((Map.Entry) arg0).getValue().toString());
		Long key2 = Long.valueOf(((Map.Entry) arg1).getValue().toString());
		return key2.compareTo(key1);
		}
		});
		return entries;
	}
}