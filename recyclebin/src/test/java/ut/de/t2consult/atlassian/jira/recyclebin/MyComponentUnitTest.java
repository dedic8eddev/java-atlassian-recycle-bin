package ut.de.t2consult.atlassian.jira.recyclebin;

import org.junit.Test;
import de.t2consult.atlassian.jira.recyclebin.api.RecycleBinComponent;
import de.t2consult.atlassian.jira.recyclebin.impl.RecycleBinComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        RecycleBinComponent component = new RecycleBinComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}