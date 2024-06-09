package de.t2consult.atlassian.jira.recyclebin.web;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.index.IssueIndexingService;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import de.t2consult.atlassian.jira.recyclebin.CommonUtils;
import de.t2consult.atlassian.jira.recyclebin.impl.SubTaskMoveHelper;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToDeleteIssues;

public class DeleteIssueWebworkModuleAction extends JiraWebActionSupport {
    private final IssueManager issueManager;

    private final IssueIndexingService indexingService;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private static final Logger log = LoggerFactory.getLogger(RecycleIssueWebworkModuleAction.class);

    @Inject
    public DeleteIssueWebworkModuleAction(
            @ComponentImport IssueManager issueManager, @ComponentImport IssueIndexingService indexingService,
            @ComponentImport JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport UserProjectHistoryManager userProjectHistoryManager )
    {
        this.issueManager = issueManager;
        this.indexingService = indexingService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userProjectHistoryManager = userProjectHistoryManager;

    }

    public String getBaseUrl(){
        return CommonUtils.getBaseUrl();
    }

    @Override
    public String doExecute() throws Exception {

        Long issueId = getIssueId();
        
        if (issueId == null) {
            return super.doExecute();
        }

        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        if (!IsAllowedToDeleteIssues(user,currentProject, ProjectPermissions.DELETE_ISSUES,permissionManager)) {
            return super.doExecute();
        }

        Issue issueObject = issueManager.getIssueObject(issueId);
        issueManager.deleteIssueNoEvent(issueObject);

        return returnComplete(CommonUtils.getBaseUrl());
    }

    public Long getIssueId(){

        String stringId=getHttpRequest().getParameter("id");
        if (stringId==null){
            return null;
        }
        return Long.parseLong(stringId);
    }

}
