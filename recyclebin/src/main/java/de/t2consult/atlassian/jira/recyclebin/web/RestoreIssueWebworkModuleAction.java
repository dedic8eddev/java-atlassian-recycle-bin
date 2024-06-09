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
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
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

import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToRestoreIssues;

public class    RestoreIssueWebworkModuleAction extends JiraWebActionSupport {
    private final IssueManager issueManager;
    private final IssueIndexingService indexingService;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private static final Logger log = LoggerFactory.getLogger(RecycleIssueWebworkModuleAction.class);

    @Inject
    public RestoreIssueWebworkModuleAction(
            @ComponentImport IssueManager issueManager, @ComponentImport IssueIndexingService indexingService,
            @ComponentImport JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport UserProjectHistoryManager userProjectHistoryManager)
    {
        this.issueManager = issueManager;
        this.indexingService = indexingService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userProjectHistoryManager = userProjectHistoryManager;
    }


    @Override
    public String doExecute() throws Exception {

        Long issueId = getIssueId();
        if (issueId==null){
            return super.doExecute();
        }

        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        ProjectPermissionKey projectPermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.restoreIssueProject");
        if (!IsAllowedToRestoreIssues(user,currentProject, projectPermissionKey, permissionManager)) {
            return super.doExecute();
        }

        MutableIssue issueObject = issueManager.getIssueObject(issueId);
        List<ChangeHistory> changes = ComponentAccessor.getChangeHistoryManager().getChangeHistories(issueObject);

        if (issueObject.isSubTask()) {

            Object previousParentId = getLastValueForField(changes,"Parent Issue");

            if (previousParentId == null) {
                log.error("Previous parent for subtask cannot be determined. Issue cannot be restored.");
                return returnComplete(getIssueUrl());
            }
            MutableIssue previousParentObject = issueManager.getIssueObject(previousParentId.toString());
            if (previousParentObject == null) {
                log.error("Previous parent object does not exist. Issue cannot be restored.");
                return returnComplete(getIssueUrl());
            }
            SubTaskMoveHelper.ChangeParentIssue(issueObject,previousParentObject,getLoggedInUser());

        }else{
                if (changes.size() >= 1) {

                    Object previousLevelObj =  getLastValueForField(changes,"security");

                    Long previousLevel = previousLevelObj != null ? Long.parseLong(previousLevelObj.toString()) : null;

                    issueObject.setSecurityLevelId(previousLevel);
                } else {
                    issueObject.setSecurityLevelId(null);
                }
            }

        issueManager.updateIssue(getLoggedInUser(), issueObject, EventDispatchOption.ISSUE_UPDATED, true);
        indexingService.reIndex(issueManager.getIssueObject(issueObject.getId()));

        return returnComplete(getIssueUrl());

    }

    private Object getLastValueForField( List<ChangeHistory> changes,String field){
        if (changes.size()<1){
            return null;
        }
        
        ChangeHistory lastChange = changes.get(changes.size() - 1);
        List<GenericValue> lastItems = lastChange.getChangeItems();

        for (GenericValue changeItem : lastItems) {

            if (changeItem.get("field").equals(field)){
                return changeItem.get("oldvalue");
            }
        }
        return null;
    }

    public String getIssueUrl(){

        Long issueId=getIssueId();
        Issue issueObject = issueManager.getIssueObject(issueId);
        return CommonUtils.getBaseUrl()+"/browse/"+issueObject.getKey();
    }


    public Long getIssueId(){

        String stringId=getHttpRequest().getParameter("id");
        if (stringId==null){
            return null;
        }
        return Long.parseLong(stringId);
    }

}
