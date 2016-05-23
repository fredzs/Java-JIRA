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
        content += "<br /><a href='" + issuesLink + "'>" + "点击链接查看详情。</a>";
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
    		showAllClosedIssues();
    	} catch (ExecutionException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (InterruptedException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
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
		content += "<br />（以上统计信息起止日期为：" + Configuration.getValMap().get("beginDate") + " - " + Configuration.getValMap().get("endDate") + "）<br />";
		Email.appendContent(content);
	}
	
	public void showSomedayCreatedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
		String JQL = "created>=\"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet();
		logger.info("查询语句：" + JQL);
		Iterable<BasicIssue> issueList = JIRAUtil.findIssuesByJQL(JQL).getIssues();
		Iterator<BasicIssue> it = issueList.iterator();
		List<String> todayCreationIssuesKeyList = new ArrayList<String>();
		List<String> todayCreationIssuesNameList = new ArrayList<String>();
        while (it.hasNext()) {
        	BasicIssue basicIssue = it.next();
        	todayCreationIssuesKeyList.add(basicIssue.getKey());
        	Issue issue = JIRAUtil.findIssueByIssueKey(basicIssue.getKey());
        	todayCreationIssuesNameList.add(issue.getSummary());
        }
        Email.appendContent("<h2>二、今日新增的问题</h2>");
        Email.appendContent(makeContentForIssues("今日新增加的ISSUE有", todayCreationIssuesKeyList, todayCreationIssuesNameList, JQL));
	}
	
	public void showSomedayResolvedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
	    String JQL = "resolved>=\"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet();
		logger.info("查询语句：" + JQL);
		Iterable<BasicIssue> issueList = JIRAUtil.findIssuesByJQL(JQL).getIssues();
		Iterator<BasicIssue> it = issueList.iterator();
		List<String> todayResolvedIssuesKeyList = new ArrayList<String>();
		List<String> todayResolvedIssuesNameList = new ArrayList<String>();
		while (it.hasNext()) {
        	BasicIssue basicIssue = it.next();
        	todayResolvedIssuesKeyList.add(basicIssue.getKey());
        	Issue issue = JIRAUtil.findIssueByIssueKey(basicIssue.getKey());
        	todayResolvedIssuesNameList.add(issue.getSummary());
        }
        Email.appendContent("<h2>三、今日解决的问题</h2>");
        Email.appendContent(makeContentForIssues("今日解决的ISSUE有", todayResolvedIssuesKeyList, todayResolvedIssuesNameList, JQL));
	}
	
	public void showSomedayClosedIssues() throws InterruptedException, ExecutionException {
		// 下方的这个projectQuerySet是根据配置文件中的projectList构造的，目的是减小统计范围。
	    String JQL = "status changed to closed on \"" + Configuration.getValMap().get("endDate") + "\"" + JIRAUtil.getProjectQuerySet();
		logger.info("查询语句：" + JQL);
		SearchResult searchResult = JIRAUtil.findIssuesByJQL(JQL);
		Iterable<BasicIssue> issueList = searchResult.getIssues();
		Iterator<BasicIssue> it = issueList.iterator();
		List<String> todayClosedIssuesKeyList = new ArrayList<String>();
		List<String> todayClosedIssuesNameList = new ArrayList<String>();
		boolean nothingFound = true; 
		while (it.hasNext()) {
        	BasicIssue basicIssue = it.next();
        	nothingFound = false;
        	todayClosedIssuesKeyList.add(basicIssue.getKey());
        	Issue issue = JIRAUtil.findIssueByIssueKey(basicIssue.getKey());
        	todayClosedIssuesNameList.add(issue.getSummary());
        }
        String timeSpent = calculateTimeSpent(searchResult.getIssues().iterator());
        Email.appendContent("<h2>四、今日关闭的问题</h2>");
        Email.appendContent(makeContentForIssues("今日关闭的ISSUE有", todayClosedIssuesKeyList, todayClosedIssuesNameList, JQL));
        if (!nothingFound) {
        	Email.appendContent("<br />平均花费时间为" + timeSpent + "。");
		}	
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
        content += "<br />（以上统计信息起止日期为：" + Configuration.getValMap().get("beginDate") + " - " + Configuration.getValMap().get("endDate") + "）<br />";
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