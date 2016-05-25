package Benmu.jira.common;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.domain.BasicPriority;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;

public class SimpleIssue {
	public SimpleIssue(BasicProject project, BasicPriority priority, String key, String summary, BasicUser assignee, DateTime creationDate, BasicStatus status) {
		this.project = project;
		this.priority = priority;
		this.key = key;
		this.summary = summary;
		this.assignee = assignee;
		this.creationDate =creationDate;
		this.status = status;
	}
	
	public SimpleIssue(Issue issue) {
		this.project = issue.getProject();
		this.priority = issue.getPriority();
		this.key = issue.getKey();
		this.summary = issue.getSummary();
		this.assignee = issue.getAssignee();
		this.creationDate =issue.getCreationDate();
		this.status = issue.getStatus();
	}
	
	private BasicProject project;
	private BasicPriority priority;
	private String key;
	private String summary;
	private BasicUser assignee;
	private DateTime creationDate;
	private BasicStatus status;
	public BasicProject getProject() {
		return project;
	}
	public void setProject(BasicProject project) {
		this.project = project;
	}
	public BasicPriority getPriority() {
		return priority;
	}
	public void setPriority(BasicPriority priority) {
		this.priority = priority;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public BasicUser getAssignee() {
		return assignee;
	}
	public void setAssignee(BasicUser assignee) {
		this.assignee = assignee;
	}
	public DateTime getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}
	public BasicStatus getStatus() {
		return status;
	}
	public void setStatus(BasicStatus status) {
		this.status = status;
	}
	
	
}

