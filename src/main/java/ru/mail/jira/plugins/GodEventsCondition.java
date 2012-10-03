package ru.mail.jira.plugins;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.opensymphony.user.User;

/**
 * User conditions.
 * 
 * @author Andrey Markelov
 */
public class GodEventsCondition
    extends AbstractJiraCondition
{
    /**
     * Configuration manager.
     */
    private final GodEventsMgr geMgr;

    /**
     * Constructor.
     */
    public GodEventsCondition(GodEventsMgr geMgr)
    {
        this.geMgr = geMgr;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldDisplay(User user, JiraHelper jh)
    {
        if (user == null)
        {
            return false;
        }

        if (geMgr.getAdminUsers().isEmpty())
        {
            return false;
        }

        if (geMgr.getAdminUsers().contains(user.getName()))
        {
            return true;
        }

        String[] groups = geMgr.getWorkGroups();
        if (groups == null)
        {
            return false;
        }
        else
        {
            for (String group : groups)
            {
                if (user.inGroup(group))
                {
                    return true;
                }
            }

            return false;
        }
    }
}
