package de.t2consult.atlassian.jira.recyclebin.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import de.t2consult.atlassian.jira.recyclebin.CommonUtils;
import de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper;
import de.t2consult.atlassian.jira.recyclebin.permissions.UserHasRestorePermission;
import de.t2consult.atlassian.jira.recyclebin.web.RestoreIssueWebworkModuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToRecycleIssues;
import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToRestoreIssues;
import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToDeleteIssues;
import static de.t2consult.atlassian.jira.recyclebin.web.RecycleIssueWebworkModuleAction.getDeletedHolderIssue;

@Scanned
public class RecycledIssuesReport extends AbstractReport {

    private static final Logger log = LoggerFactory.getLogger(UserHasRestorePermission.class);

    private final SearchService searchService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private int page;
    private int pagesize;

    @Inject
    public RecycledIssuesReport(@ComponentImport ProjectManager projectManager, @ComponentImport SearchService searchService, @ComponentImport JiraAuthenticationContext jiraAuthenticationContext, @ComponentImport UserProjectHistoryManager userProjectHistoryManager) {
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    @Override
    public boolean showReport(){
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        ProjectPermissionKey projectPermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.restoreIssueProject");

        return IsAllowedToRestoreIssues(user,currentProject,projectPermissionKey,permissionManager);
    }


    public String generateReportHtml(ProjectActionSupport projectActionSupport, Map map) throws Exception {

        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        Long deletedIssueSecurityLevelId= PermissionHelper.GetDeletedIssuesPermissionSchemeId();
        List<Issue> issues = getIssues(currentProject.getId(), deletedIssueSecurityLevelId, this.jiraAuthenticationContext, this.searchService);

        log.debug("page: "+page+ " pagesize: "+pagesize+ " - issueCount: "+issues.size());
        List<Issue> issueSubList = issues.stream().skip(page*pagesize).limit(pagesize).collect(Collectors.toList());
        return generateReportHtml(issueSubList,issues.size());
    }

    private String generateReportHtml(List<Issue> issues,int totalCount) {
        if (this.jiraAuthenticationContext.getLoggedInUser() == null) {
            return "Please, login first";
        }

        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PermissionManager permissionManager = ComponentAccessor.getPermissionManager();
        Project currentProject = userProjectHistoryManager.getCurrentProject(10, this.jiraAuthenticationContext.getLoggedInUser());
        ProjectPermissionKey restorePermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.restoreIssueProject");

        Map<String, Object> velocityParams = new HashMap<>();

        velocityParams.put("issues", issues);
        velocityParams.put("baseUrl", CommonUtils.getBaseUrl());
        velocityParams.put("totalCount", totalCount);
        velocityParams.put("currentPage", page);
        velocityParams.put("totalPages",totalCount/pagesize);
        velocityParams.put("hasDeletePermission", IsAllowedToDeleteIssues(user,currentProject,ProjectPermissions.DELETE_ISSUES,permissionManager));
        return this.descriptor.getHtml("view", velocityParams);
    }

    @Override
    public void validate(ProjectActionSupport action, Map params) {
        page=ParameterUtils.getIntParam(params, "page",0);
        log.debug("Requesting page: "+page);

        //TODO default-value - duplicates in atlassian-plugin.xml
        pagesize=ParameterUtils.getIntParam(params, "pagesize",10);
        super.validate(action, params);
    }


    public static List<Issue> getIssues(Long projectId, Long level, JiraAuthenticationContext jiraAuthenticationContext, SearchService searchService) throws SearchException {

        Issue deletedHolder=getDeletedHolderIssue(projectId, jiraAuthenticationContext.getLoggedInUser(), searchService);
        String key = deletedHolder==null ? "" : deletedHolder.getKey();

        Query query = JqlQueryBuilder.newBuilder()
                .where()
                    .defaultAnd()
                        .project().eq(projectId)
                        .sub()
                            .issueParent(key)
                        .or()
                            .level().eq(level)
                            .and()
                .sub()
                            .not()
                            .issue(key)
                .endsub()
                        .endsub()
                .endWhere()
                .buildQuery();


        //TODO use pagerfilter above for these queries!!

        //Jira 8.0
        return searchService.searchOverrideSecurity(jiraAuthenticationContext.getLoggedInUser(), query, PagerFilter.getUnlimitedFilter()).getResults();
        //Jira 7.0
        //return searchService.searchOverrideSecurity(jiraAuthenticationContext.getLoggedInUser(), query, PagerFilter.getUnlimitedFilter()).getIssues();
    }
}
