package de.t2consult.atlassian.jira.recyclebin;

import com.atlassian.jira.component.ComponentAccessor;

public class CommonUtils {
    public static String getBaseUrl() {
        return ComponentAccessor.getApplicationProperties().getDefaultBackedString("jira.baseurl");
    }
}
