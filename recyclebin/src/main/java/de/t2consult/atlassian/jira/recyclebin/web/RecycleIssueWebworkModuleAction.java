package de.t2consult.atlassian.jira.recyclebin.web;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.IssueIndexingService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import de.t2consult.atlassian.jira.recyclebin.CommonUtils;
import de.t2consult.atlassian.jira.recyclebin.impl.SubTaskMoveHelper;
import de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

@Scanned
public class RecycleIssueWebworkModuleAction extends JiraWebActionSupport
{
    private final IssueManager issueManager;
    private final IssueIndexingService indexingService;
    private final SearchService searchService;

    private static final Logger log = LoggerFactory.getLogger(RecycleIssueWebworkModuleAction.class);

    @Inject
    public RecycleIssueWebworkModuleAction(@ComponentImport IssueManager issueManager, @ComponentImport IssueIndexingService indexingService, @ComponentImport SearchService searchService )
    {
        this.issueManager = issueManager;
        this.indexingService = indexingService;
        this.searchService = searchService;

    }


    @Override
    public String doExecute() throws Exception {

        Long issueId = getIssueId();
        if (issueId==null){
            return super.doExecute();
        }
        MutableIssue issueObject = issueManager.getIssueObject(issueId);


        if(!issueObject.isSubTask()) {
            issueObject.setSecurityLevelId(PermissionHelper.GetDeletedIssuesPermissionSchemeId());
        }
        else{
            MutableIssue parentIssue=issueManager.getIssueObject(issueObject.getParentId());

            List<Issue> deletedHolders = getIssues(parentIssue.getProjectId(),getLoggedInUser(),searchService);
            Issue subTaskIssueHolder = deletedHolders.size()>0 ? deletedHolders.get(0) : createIssue(parentIssue.getProjectId(),parentIssue.getIssueType(),PermissionHelper.GetDeletedIssuesPermissionSchemeId());

            log.warn("Setting parent "+subTaskIssueHolder.getId()+ " for "+issueObject.getId());
            SubTaskMoveHelper.ChangeParentIssue(issueObject, subTaskIssueHolder, getLoggedInUser());

            //indexingService.reIndex(issueManager.getIssueObject(subTaskIssueHolder.getId()));

        }

        issueManager.updateIssue(getLoggedInUser(), issueObject, EventDispatchOption.ISSUE_DELETED, true);

        for (Issue subTaskIssueObject:issueObject.getSubTaskObjects()) {
            indexingService.reIndex(issueManager.getIssueObject(subTaskIssueObject.getId()));
        }

        indexingService.reIndex(issueManager.getIssueObject(issueObject.getId()));

        return returnComplete(CommonUtils.getBaseUrl());

    }

    public String getBaseUrl(){
        return CommonUtils.getBaseUrl();
    }

    public Long getIssueId(){

        String stringId=getHttpRequest().getParameter("id");
        if (stringId==null){
            return null;
        }
        return Long.parseLong(stringId);
    }

    public static Issue getDeletedHolderIssue(Long projectId, ApplicationUser user,SearchService service) throws SearchException {
        List<Issue> deletedHolders =  getIssues(projectId,user,service);
        return deletedHolders.size()>0 ? deletedHolders.get(0) : null;
    }

    private static List<Issue> getIssues(Long projectId, ApplicationUser user,SearchService service) throws SearchException {

        Query query = JqlQueryBuilder.newBuilder()
                .where()
                .defaultAnd()
                .summary("DELETED-HOLDER")
                .project().eq(projectId)
                .endWhere()
                .buildQuery();

        //Jira 8.0
        return service.searchOverrideSecurity(user, query, PagerFilter.getUnlimitedFilter()).getResults();
        //Jira 7.0
        //return service.searchOverrideSecurity(user, query, PagerFilter.getUnlimitedFilter()).getIssues();
    }

    private Issue createIssue(Long projectId, IssueType issueType, Long issueSecurityLevelId) throws CreateException {
        IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
        ProjectManager projectManager = ComponentAccessor.getProjectManager();

        MutableIssue newIssue = issueFactory.getIssue();
        newIssue.setProjectId(projectId);
        newIssue.setIssueTypeObject(issueType);
        newIssue.setReporterId(getLoggedInUser().getName());
        newIssue.setSecurityLevelId(issueSecurityLevelId);

        // copy over some default fields
        newIssue.setSummary("DELETED-HOLDER");
        newIssue.setDescription("DELETED-HOLDER");

        Issue answer = null;
        try {
            answer = issueManager.createIssueObject(getLoggedInUser(), newIssue);
        } catch (CreateException e) {
            Project project = projectManager.getProjectObj(projectId);
            throw new CreateException("Could not create a new issue in project " + project.getName(), e);
        }
        return answer;
    }
}
