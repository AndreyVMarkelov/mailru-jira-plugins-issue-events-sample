package ru.mail.jira.plugins;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * Implementation of configuration settings.
 * 
 * @author Andrey Markelov
 */
public class GodEventsMgrImpl
    implements GodEventsMgr
{
    /*
     * Jira keys.
     */
    private final static String DATABASE_KEY = "DATABASE_KEY";
    private final static String DB_HOST_KEY = "DB_HOST_KEY";
    private final static String DB_PORT_KEY = "DB_PORT_KEY";
    private final static String GROUPS_KEY = "GROUPS_KEY";
    private final static String PASSWORD_KEY = "PASSWORD_KEY";
    private final static String USER_ADMIN_KEY = "USER_ADMIN_KEY";
    private final static String USERNAME_KEY = "USERNAME_KEY";

    /**
     * Plug-In Jira db key.
     */
    private final String PLUGIN_KEY = "godEvents";

    /**
     * Plug-In settings factory.
     */
    private final PluginSettingsFactory pluginSettingsFactory;

    /**
     * Constructor.
     */
    public GodEventsMgrImpl(PluginSettingsFactory pluginSettingsFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public Set<String> getAdminUsers()
    {
        String adminStr = getStringProperty(USER_ADMIN_KEY);

        Set<String> users = new TreeSet<String>();
        StringTokenizer st = new StringTokenizer(adminStr, ",");
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            users.add(token.trim());
        }

        return users;
    }

    @Override
    public String getAdminUserStr()
    {
        return getStringProperty(USER_ADMIN_KEY);
    }

    @Override
    public String getDbHost()
    {
        return getStringProperty(DB_HOST_KEY);
    }

    @Override
    public String getDbName()
    {
        return getStringProperty(DATABASE_KEY);
    }

    @Override
    public String getDbUser()
    {
        return getStringProperty(USERNAME_KEY);
    }

    @Override
    public String getDbUserPass()
    {
        return getStringProperty(PASSWORD_KEY);
    }

    @Override
    public String getPort()
    {
        return getStringProperty(DB_PORT_KEY);
    }

    private String getStringProperty(String key)
    {
        return (String) pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY).get(key);
    }

    @Override
    public String[] getWorkGroups()
    {
        String groups = getStringProperty(GROUPS_KEY);
        if (groups != null)
        {
            return groups.split("&");
        }

        return null;
    }

    @Override
    public void setAdminUsers(Set<String> users)
    {
        StringBuilder sb = new StringBuilder();
        if (users != null)
        {
            for (String user : users)
            {
                sb.append(user).append(", ");
            }
        }

        setStringProperty(USER_ADMIN_KEY, sb.toString());
    }

    @Override
    public void setAdminUserStr(String users)
    {
        setStringProperty(USER_ADMIN_KEY, users);
    }

    @Override
    public void setDbHost(String dbHost)
    {
        setStringProperty(DB_HOST_KEY, dbHost);
    }

    @Override
    public void setDbName(String dbName)
    {
        setStringProperty(DATABASE_KEY, dbName);
    }

    @Override
    public void setDbUser(String dbUser)
    {
        setStringProperty(USERNAME_KEY, dbUser);
    }

    @Override
    public void setDbUserPass(String dbUserPass)
    {
        setStringProperty(PASSWORD_KEY, dbUserPass);
    }

    @Override
    public void setPort(String port)
    {
        setStringProperty(DB_PORT_KEY, port);
    }

    private void setStringProperty(String key, String value)
    {
        pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY).put(key, value);
    }

    @Override
    public void setWorkGroups(String[] workGroups)
    {
        if (workGroups != null)
        {
            StringBuilder sb = new StringBuilder(50);
            for (String workGroup : workGroups)
            {
                sb.append(workGroup).append("&");
            }
            setStringProperty(GROUPS_KEY, sb.toString());
        }
    }
}
