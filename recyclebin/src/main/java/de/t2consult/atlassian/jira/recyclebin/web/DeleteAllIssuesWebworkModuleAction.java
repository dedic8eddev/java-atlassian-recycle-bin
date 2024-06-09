package de.t2consult.atlassian.jira.recyclebin.web;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.index.IssueIndexingService;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper;
import de.t2consult.atlassian.jira.recyclebin.CommonUtils;
import de.t2consult.atlassian.jira.recyclebin.impl.SubTaskMoveHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserProjectHistoryManager;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToDeleteIssues;
import static de.t2consult.atlassian.jira.recyclebin.reports.RecycledIssuesReport.getIssues;
import static de.t2consult.atlassian.jira.recyclebin.web.RecycleIssueWebworkModuleAction.getDeletedHolderIssue;

public class DeleteAllIssuesWebworkModuleAction extends JiraWebActionSupport {
    private final IssueManager issueManager;
    private final IssueIndexingService indexingService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final SearchService searchService;

    private static final Logger log = LoggerFactory.getLogger(RecycleIssueWebworkModuleAction.class);

    @Inject
    public DeleteAllIssuesWebworkModuleAction(@ComponentImport IssueManager issueManager, @ComponentImport SearchService searchService, @ComponentImport IssueIndexingService indexingService, @ComponentImport JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport UserProjectHistoryManager userProjectHistoryManager)
    {
        this.issueManager = issueManager;
        this.indexingService = indexingService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.searchService = searchService;
    }


    @Override
    public String doExecute() throws Exception {
        List<Issue> issues = this.getRecycledIssues();

        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        if (!IsAllowedToDeleteIssues(user,currentProject, ProjectPermissions.DELETE_ISSUES,permissionManager)) {
            return super.doExecute();
        }

        for (Issue issue: issues) {
            Issue i = this.issueManager.getIssueObject(issue.getId());
            this.issueManager.deleteIssueNoEvent(i);
        }

        return returnComplete(CommonUtils.getBaseUrl());
    }

    private List<Issue> getRecycledIssues() throws SearchException {
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        Long deletedIssueSecurityLevelId = PermissionHelper.GetDeletedIssuesPermissionSchemeId();
        List<Issue> issues = getIssues(currentProject.getId(), deletedIssueSecurityLevelId, this.jiraAuthenticationContext, this.searchService);

        return issues;
    }

    public Long getIssueId(){
        String stringId = getHttpRequest().getParameter("id");
        if (stringId==null){
            return null;
        }
        return Long.parseLong(stringId);
    }

    public int getIssueCount() throws SearchException {
        List<Issue> issues = this.getRecycledIssues();
        return issues.size();
    }
}
