package Benmu.jira.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class JIRAUtil {
    private static JiraRestClient restClient;
	private static Map<String, BasicProject> allProjectMap = new HashMap<String, BasicProject>();
	private static List<String> projectKeyList = new ArrayList<String>();
	private static String projectQuerySet;
	private static Map<String, List<String>> issueMap = new HashMap<String, List<String>>();
	private static int totalIssue = 0;
	private final static int MAXRESULTS = 1000;
	private static Logger logger = Logger.getLogger(JIRAUtil.class);

    public static void init(String user, String password, String uri) throws InterruptedException, ExecutionException {
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri;
        try {
            jiraServerUri = new URI(uri);
            restClient = (JiraRestClient) factory.createWithBasicHttpAuthentication(jiraServerUri, user, password);
            logger.info("成功连接JARI服务器：" + jiraServerUri);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
    }
    
    // 通过JQL获取问题
    public static SearchResult findIssuesByJQL(String jql) {
        SearchRestClient searchClient = restClient.getSearchClient();
        // Jira的REST API规定，每次通过JQL查询时，都要指定返回结果集的大小，默认是只返回前100条，此处设为MAXRESULTS，详情查看searchJql()方法的声明。
        SearchResult results = searchClient.searchJql(jql, MAXRESULTS, 0).claim();
        return results;
    }
    
    // 得到所有项目的BasicProject信息
    public static void updateAllProjectList() {
        try {
            Promise<Iterable<BasicProject>> list = restClient.getProjectClient().getAllProjects();
            Iterable<BasicProject> a = list.get();
            Iterator<BasicProject> it = a.iterator();
            while (it.hasNext()) {
            	BasicProject bp = it.next();
            	allProjectMap.put(bp.getKey(), bp);
            }
            return;
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        }
    }

    // 根据配置文件，更新需要统计的Project Key列表
    public static void updateProjectKeyList() {
    	try {
    		if (Configuration.getProjectList().size() == 0) {
	            Promise<Iterable<BasicProject>> projectList = restClient.getProjectClient().getAllProjects();
	            Iterable<BasicProject> a = projectList.get();
	            Iterator<BasicProject> it = a.iterator();
	            while (it.hasNext()) {
	            	projectKeyList.add(it.next().getKey());
	            }
    		} else {
    			projectKeyList = Configuration.getProjectList();
    		}
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        }
    	return;
    }
    
    public static void makeProjectQuerySet() {
		projectQuerySet = " AND project in ( ";
		int i = 0;
		for (; i < Configuration.getProjectList().size() - 1; i++) {
			String projectKey = Configuration.getProjectList().get(i);
			projectQuerySet += projectKey;
			projectQuerySet += ", ";
		}
		projectQuerySet += Configuration.getProjectList().get(i);
		projectQuerySet += " )";
	}
    
    // 更新需要统计的Issue列表
    public static void updateIssueMap() {
		for (Iterator<String> iterator = JIRAUtil.getProjectKeyList().iterator(); iterator.hasNext();) {
			String projectKey = (String) iterator.next();
			List<String> issueList = new ArrayList<String>();
			String jql = "createdDate > \"" + Configuration.getValMap().get("beginDate") + "\"" + projectQuerySet;
			logger.info("查询语句：" + jql);
			SearchResult result = JIRAUtil.findIssuesByJQL(jql);
			Iterable<BasicIssue> issues = result.getIssues();
			Iterator<BasicIssue> it = issues.iterator();
			while (it.hasNext()) {
				issueList.add(it.next().getKey());
				JIRAUtil.addTotalIssue(1);
			}
			JIRAUtil.getIssueMap().put(projectKey, issueList);
		}
	}
    
    // 通过Project KEY获取Project
    public static void findProject(String porjectKEY) throws InterruptedException, ExecutionException {
        try {
            Project project = restClient.getProjectClient().getProject(porjectKEY).get();
            System.out.println(project);
        } finally {
        }
    }

    // 通过KEY获取Issue的完整信息
    public static Issue findIssueByIssueKey(String issueKEY) throws InterruptedException, ExecutionException {
        try {
            Promise<Issue> list = restClient.getIssueClient().getIssue(issueKEY);
            Issue issue = list.get();
            return issue;
        } finally {
        }
    }
    // 通过KEY获取问题
    public static Issue findBasicIssueByIssueKey(String issueKey) {
        SearchRestClient searchClient = restClient.getSearchClient();
        String jql = "issuekey = \"" + issueKey + "\"";
        SearchResult results = searchClient.searchJql(jql).claim();
        return (Issue) results.getIssues().iterator().next();
    }
   
    // 通过Project KEY获取BasicIssue
    public static SearchResult findBasicIssueByProjectKey(String projectKey) {
        SearchRestClient searchClient = restClient.getSearchClient();
        String jql = "project = \"" + projectKey + "\"";
        SearchResult results = searchClient.searchJql(jql, 1000, 0).claim();
        return results;
    }

	public static void addTotalIssue(int step) {
		JIRAUtil.totalIssue += step;
	}
	
	// getters and setters
    public static JiraRestClient getRestClient() {
		return restClient;
	}
	public static int getTotalIssue() {
		return totalIssue;
	}

	public static List<String> getProjectKeyList() {
		return projectKeyList;
	}

	public static Map<String, BasicProject> getAllProjectMap() {
		return allProjectMap;
	}

	public static Map<String, List<String>> getIssueMap() {
		return issueMap;
	}

	public static String getProjectQuerySet() {
		return projectQuerySet;
	}
	
	
}