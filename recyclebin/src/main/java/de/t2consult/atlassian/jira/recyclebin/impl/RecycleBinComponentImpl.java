package de.t2consult.atlassian.jira.recyclebin.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import de.t2consult.atlassian.jira.recyclebin.api.RecycleBinComponent;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({RecycleBinComponent.class})
@Named ("myPluginComponent")
public class RecycleBinComponentImpl implements RecycleBinComponent
{
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public RecycleBinComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
}