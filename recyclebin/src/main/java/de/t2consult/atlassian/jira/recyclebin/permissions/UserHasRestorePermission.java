package de.t2consult.atlassian.jira.recyclebin.permissions;

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
import de.t2consult.atlassian.jira.recyclebin.web.RecycleBinBannerContextProvider;

import javax.inject.Inject;

import static de.t2consult.atlassian.jira.recyclebin.permissions.PermissionHelper.IsAllowedToRestoreIssues;

@Scanned
public class UserHasRestorePermission extends com.atlassian.jira.plugin.webfragment.conditions.AbstractProjectPermissionCondition {
    //private static final Logger log = LoggerFactory.getLogger(UserHasProjectPermission.class);
    //log.warn("" + globalPermissionKey.toString() + ": "+allowedGlobal);

    private PermissionManager permissionManager;

    @Inject
    public UserHasRestorePermission(@ComponentImport PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }


    @Override
    protected boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper, ProjectPermissionKey permissionKey) {
        return IsAllowedToRestoreIssues(user,jiraHelper.getProject(),permissionKey,permissionManager);
    }
}
