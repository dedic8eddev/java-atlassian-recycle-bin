package it.de.t2consult.atlassian.jira.recyclebin;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import de.t2consult.atlassian.jira.recyclebin.api.RecycleBinComponent;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{
    private final ApplicationProperties applicationProperties;
    private final RecycleBinComponent recycleBinComponent;

    public MyComponentWiredTest(ApplicationProperties applicationProperties, RecycleBinComponent recycleBinComponent)
    {
        this.applicationProperties = applicationProperties;
        this.recycleBinComponent = recycleBinComponent;
    }

    @Test
    public void testMyName()
    {
        assertEquals("names do not match!", "myComponent:" + applicationProperties.getDisplayName(), recycleBinComponent.getName());
    }
}