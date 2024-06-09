package de.t2consult.atlassian.jira.recyclebin.permissions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.*;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.plugin.webfragment.conditions.cache.ConditionCacheKeys;
import com.atlassian.jira.plugin.webfragment.conditions.cache.RequestCachingConditionHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import de.t2consult.atlassian.jira.recyclebin.web.RecycleBinBannerContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class PermissionHelper {

    private static final String ISSUE_SECURITY_LEVEL_NAME="Deleted";
    private static final String ISSUE_SECURITY_LEVEL_DESCRIPTION="This is the security entity for deleted issues. Please do not rename, delete or modify.";

    private static final Logger log = LoggerFactory.getLogger(PermissionHelper.class);
    public static Long GetDeletedIssuesPermissionSchemeId(){

        IssueSecurityLevelManager levelSecurityManager= ComponentAccessor.getIssueSecurityLevelManager();
        Collection<IssueSecurityLevel> issueSecurityLevels = levelSecurityManager.getIssueSecurityLevelsByName(ISSUE_SECURITY_LEVEL_NAME);
        if (issueSecurityLevels.size()==0){
            CreateDeletedIssueSecurityLevel();

        }else if(issueSecurityLevels.size()>1){
            log.warn("More than one security Level found with name Deleted, taking first.");
        }
        issueSecurityLevels = levelSecurityManager.getIssueSecurityLevelsByName(ISSUE_SECURITY_LEVEL_NAME);
        IssueSecurityLevel issueSecurityLevel = issueSecurityLevels.stream().findFirst().orElse(null);
        return issueSecurityLevel.getId();
    }

    private static void CreateDeletedIssueSecurityLevel() {

        IssueSecuritySchemeManager issueSecuritySchemeManager= ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);

        Collection<IssueSecurityLevelScheme> issueSecurityLevelSchemes =issueSecuritySchemeManager.getIssueSecurityLevelSchemes();
        IssueSecurityLevelScheme deletedScheme = issueSecurityLevelSchemes.stream().filter(ils-> ils.getName().equals(ISSUE_SECURITY_LEVEL_NAME)).findFirst().orElse(null);

        if (deletedScheme == null){
            Scheme created = issueSecuritySchemeManager.createSchemeObject(ISSUE_SECURITY_LEVEL_NAME,ISSUE_SECURITY_LEVEL_DESCRIPTION);

            IssueSecurityLevelManager issueSecurityLevelManager= ComponentAccessor.getComponent(IssueSecurityLevelManager.class);
            IssueSecurityLevel deleted = issueSecurityLevelManager.createIssueSecurityLevel(created.getId(),ISSUE_SECURITY_LEVEL_NAME, ISSUE_SECURITY_LEVEL_DESCRIPTION);
        }

    }

    public static boolean CheckPermission(String permissionString, ApplicationUser user, Project project, ProjectPermissionKey permissionKey, PermissionManager permissionManager) {
        if (RecycleBinBannerContextProvider.isBuildExpired())
            return false;

        GlobalPermissionManager globalPermissionManager = ComponentAccessor.getGlobalPermissionManager();

        GlobalPermissionKey globalPermissionKey = GlobalPermissionKey.of(permissionString);
        boolean allowedGlobal = RequestCachingConditionHelper.cacheConditionResultInRequest(ConditionCacheKeys.permission(globalPermissionKey, user),
                () -> globalPermissionManager.hasPermission(globalPermissionKey, user));

        if (allowedGlobal){
            return true;
        }

        if (project == null) {
            return false;
        }

        return RequestCachingConditionHelper.cacheConditionResultInRequest(
                ConditionCacheKeys.permission(permissionKey, user, project),
                () -> permissionManager.hasPermission(permissionKey, project, user));
    }

    public static boolean IsAllowedToRecycleIssues(ApplicationUser user, Project project, ProjectPermissionKey permissionKey, PermissionManager permissionManager) {
        ProjectPermissionKey recycleIssuePermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.recycleIssueProject");
        ProjectPermissionKey useRecycleBinPermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.useRecycleBinProject");
        
        return 
            CheckPermission("de.t2consult.atlassian.jira.recycleIssueGlobal", user, project, recycleIssuePermissionKey, permissionManager)
            || CheckPermission("de.t2consult.atlassian.jira.useRecycleBinGlobal", user, project, useRecycleBinPermissionKey, permissionManager);
    }

    public static boolean IsAllowedToRestoreIssues(ApplicationUser user, Project project, ProjectPermissionKey permissionKey, PermissionManager permissionManager) {
        ProjectPermissionKey restoreIssuePermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.restoreIssueProject");
        ProjectPermissionKey useRecycleBinPermissionKey = new ProjectPermissionKey("de.t2consult.atlassian.jira.useRecycleBinProject");

        return 
            CheckPermission("de.t2consult.atlassian.jira.restoreIssueGlobal", user, project, restoreIssuePermissionKey, permissionManager)
            || CheckPermission("de.t2consult.atlassian.jira.useRecycleBinGlobal", user, project, useRecycleBinPermissionKey, permissionManager);
    }

    public static boolean IsAllowedToDeleteIssues(ApplicationUser user, Project project, ProjectPermissionKey permissionKey, PermissionManager permissionManager) {
        return CheckPermission(permissionKey.permissionKey(), user, project, permissionKey, permissionManager);
    }


    public static boolean IsAllowedToUseRecycleBin(ApplicationUser user, Project project, ProjectPermissionKey permissionKey, PermissionManager permissionManager) {
        return CheckPermission("de.t2consult.atlassian.jira.useRecycleBinGlobal", user, project, permissionKey, permissionManager);
    }
}
