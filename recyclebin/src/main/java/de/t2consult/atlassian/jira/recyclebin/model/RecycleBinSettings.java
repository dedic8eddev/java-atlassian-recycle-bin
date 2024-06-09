package de.t2consult.atlassian.jira.recyclebin.model;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import de.t2consult.atlassian.jira.recyclebin.utils.StringUtils;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.ParseResult;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.component.ComponentAccessor;
// import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecycleBinSettings {
    private static final String PLUGIN_STORAGE_KEY = "de.t2consult.atlassian.jira.recyclebin";

    /*Begin DEFAULT-Values */
    private static final String DEFAULT_JQL_FILTER_VALUE = StringUtils.EmptyString;
    private static final String DEFAULT_JQL_FILTER_FRONT_VALUE = StringUtils.EmptyString;
    /*End DEFAULT-Values*/

    private static final String JQL_FILTER_KEY ="jqlFilter";
    private static final String JQL_FILTER_FRONT_KEY ="jqlFilterFront";

    private final PluginSettings pluginSettings;

    public RecycleBinSettings(PluginSettings pluginSettings){
        this.pluginSettings=pluginSettings;
    }

    public String getJqlFilter(){ return this.getSetting(JQL_FILTER_KEY, DEFAULT_JQL_FILTER_VALUE); }
    public void setJqlFilter(String jqlFilter) { 
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
        ParseResult result = searchService.parseQuery(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), jqlFilter);

        if (result.isValid()) {
            this.setSetting(JQL_FILTER_KEY, jqlFilter); 
        } else {
            this.setSetting(JQL_FILTER_KEY, DEFAULT_JQL_FILTER_VALUE); 
        }
    }

    public Boolean getIsJqlFilterValid(){
        String jqlFilter = this.getJqlFilterFront();
        SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
        ParseResult result = searchService.parseQuery(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), jqlFilter);

        return result.isValid();
     }

    public String getJqlFilterFront(){ return this.getSetting(JQL_FILTER_FRONT_KEY, DEFAULT_JQL_FILTER_FRONT_VALUE); }
    public void setJqlFilterFront(String jqlFilterFront) { 
        this.setSetting(JQL_FILTER_FRONT_KEY, jqlFilterFront);
        this.setJqlFilter(jqlFilterFront);
    }

    @SuppressWarnings("unchecked")
    private <T> T getSetting(String key, Object defaultValue){
        Object value = pluginSettings.get(PLUGIN_STORAGE_KEY + "." + key);
        return value != null ? (T) value : (T) defaultValue;
    }

    private void setSetting(String key, Object value){
        String settingKey = PLUGIN_STORAGE_KEY + "." + key;
        pluginSettings.put(settingKey, value );
    }
}
