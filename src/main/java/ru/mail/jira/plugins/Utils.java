package ru.mail.jira.plugins;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.mail.jira.plugins.structs.UserCard;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;

/**
 * Utils.
 * 
 * @author Andrey Markelov
 */
public class Utils
{
    /**
     * Is VP action allowed?
     */
    public static boolean allowCpAction(
        User user,
        GodEventsMgr geMgr,
        UserCard card,
        int cp)
    {
        if (geMgr.getAdminUsers().contains(user.getName()) || card.getDivisions().contains(cp))
        {
            return true;
        }

        return false;
    }

    /**
     * Is VP action allowed?
     */
    public static boolean allowGPAction(
        User user,
        GodEventsMgr geMgr,
        UserCard card,
        int gp)
    {
        if (geMgr.getAdminUsers().contains(user.getName()) || card.getGameProjects().contains(gp))
        {
            return true;
        }

        return false;
    }

    /**
     * Is VP action allowed?
     */
    public static boolean allowVpAction(
        User user,
        GodEventsMgr geMgr,
        UserCard card)
    {
        if (geMgr.getAdminUsers().contains(user.getName()) || card.isVP())
        {
            return true;
        }

        return false;
    }

    /**
     * Is VP action allowed?
     */
    public static boolean allowVpDivAction(
        User user,
        GodEventsMgr geMgr,
        UserCard card)
    {
        if (geMgr.getAdminUsers().contains(user.getName()) || card.isVP() || !card.getDivisions().isEmpty())
        {
            return true;
        }

        return false;
    }

    public static long getSqlDateAsLong(Date date)
    {
        if (date == null)
        {
            return -1;
        }
        else
        {
            return date.getTime();
        }
    }

    public static Map<Integer, String> getStatusMap()
    {
        Map<Integer, String> res = new HashMap<Integer, String>();
        res.put(0, "Not assigned");
        res.put(1, "Frozen");
        res.put(2, "Processed");
        res.put(3, "In progress");
        res.put(4, "Requested");
        res.put(5, "In development");

        return res;
    }

    /**
     * This method checks has the user access for Plugin.
     */
    public static boolean hasUserPliginAccess(
        User user,
        GodEventsMgr geMgr,
        GroupManager groupManager)
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
            Collection<String> jGroups = groupManager.getGroupNamesForUser(user.getName());
            for (String group : groups)
            {
                if (jGroups.contains(group))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static List<Integer> strArrayToIntList(String[] arr)
    {
        List<Integer> res = new ArrayList<Integer>();

        if (arr != null)
        {
            for (String i : arr)
            {
                res.add(Integer.parseInt(i));
            }
        }

        return res;
    }

    public static String weakStr(String str)
    {
        return (str == null) ? "" : str;
    }

    /**
     * Private constructor.
     */
    private Utils() {}
}
