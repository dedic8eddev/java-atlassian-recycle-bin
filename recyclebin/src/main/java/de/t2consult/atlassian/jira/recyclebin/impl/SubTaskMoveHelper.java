package de.t2consult.atlassian.jira.recyclebin.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.user.ApplicationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SubTaskMoveHelper {

    private static final Logger log = LoggerFactory.getLogger(SubTaskMoveHelper.class);

    public static void ChangeParentIssue(MutableIssue issue, Issue newParentIssue, ApplicationUser user){

        try {
            IssueUpdateBean iub = ComponentAccessor.getSubTaskManager().changeParent(issue,newParentIssue,user);
            IssueUpdater issueUpdater = ComponentAccessor.getComponentOfType(IssueUpdater.class);
            issueUpdater.doUpdate(iub,true);

        } catch (RemoveException e) {
            log.error("RemoveException on changing parent.");
        } catch (CreateException e) {
            log.error("CreateException on changing parent.");
        }
    }

}
