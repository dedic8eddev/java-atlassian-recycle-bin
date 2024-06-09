package de.t2consult.atlassian.jira.recyclebin.jira.webwork;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import de.t2consult.atlassian.jira.recyclebin.model.RecycleBinSettings;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import de.t2consult.atlassian.jira.recyclebin.utils.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Scanned
public class RecycleBinConfigurationModuleAction extends JiraWebActionSupport
{
    private final RecycleBinSettings recycleBinSettings;

    @Inject
    RecycleBinConfigurationModuleAction(
        @ComponentImport final PluginSettingsFactory pluginSettingsFactory
    ){
        this.recycleBinSettings = new RecycleBinSettings(pluginSettingsFactory.createGlobalSettings());
    }

    @Override
    public String execute() throws Exception {
        return super.doDefault();
    }

    public String getJqlFilter(){ return recycleBinSettings.getJqlFilter(); }
    public void setJqlFilter(String value){ recycleBinSettings.setJqlFilter(value); }

    public String getJqlFilterFront(){ return recycleBinSettings.getJqlFilterFront(); }
    public void setJqlFilterFront(String value){ recycleBinSettings.setJqlFilterFront(value); }

    public Boolean getIsJqlFilterValid(){ return recycleBinSettings.getIsJqlFilterValid(); }
}
