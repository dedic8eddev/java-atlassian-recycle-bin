package de.t2consult.atlassian.jira.recyclebin.permissions;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.ParseResult;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugin.webfragment.conditions.cache.ConditionCacheKeys;
import com.atlassian.jira.plugin.webfragment.conditions.cache.RequestCachingConditionHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.jira.security.JiraAuthenticationContext;
import de.t2consult.atlassian.jira.recyclebin.model.RecycleBinSettings;

import java.util.List;
import javax.inject.Inject;

@Scanned
public class IssueIsAllowedForRecycle extends com.atlassian.jira.plugin.webfragment.conditions.AbstractProjectPermissionCondition {
    private SearchService searchProvider;
    private PermissionManager permissionManager;
    private RecycleBinSettings recycleBinSettings;
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Inject
    public IssueIsAllowedForRecycle(
        @ComponentImport SearchService searchProvider,
        @ComponentImport PermissionManager permissionManager,
        @ComponentImport PluginSettingsFactory pluginSettingsFactory,
        @ComponentImport JiraAuthenticationContext jiraAuthenticationContext
    ) {
        this.searchProvider = searchProvider;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.recycleBinSettings = new RecycleBinSettings(pluginSettingsFactory.createGlobalSettings());
    }


    @Override
    protected boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper, ProjectPermissionKey permissionKey) {
        Issue issue = (Issue) jiraHelper.getContextParams().get("issue");
        String jqlFilter = recycleBinSettings.getJqlFilter();
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
        ParseResult result = searchService.parseQuery(this.jiraAuthenticationContext.getLoggedInUser(), jqlFilter);
        Query query = result.getQuery();

        try {
            List<Issue> issues = this.searchProvider.search(this.jiraAuthenticationContext.getLoggedInUser(), query, PagerFilter.getUnlimitedFilter()).getResults();
            return issues.contains(issue);
        } catch (SearchException e) {
            return true;
        }
    }
}
