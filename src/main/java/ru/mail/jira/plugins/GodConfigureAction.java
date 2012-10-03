package ru.mail.jira.plugins;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ru.mail.jira.plugins.connectors.Connector;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.ApplicationProperties;

/**
 * This class is used for configuring Plug-In.
 * 
 * @author Andrey Markelov
 */
public class GodConfigureAction
    extends JiraWebActionSupport
{
    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 1437688758307379730L;

    /**
     * Application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Database host.
     */
    private String dbHost;

    /**
     * Database name.
     */
    private String dbName;

    /**
     * Database user's password.
     */
    private String dbPass;

    /**
     * Database port.
     */
    private String dbPort;

    /**
     * Database user.
     */
    private String dbUser;

    /**
     * Plug-In manager.
     */
    private final GodEventsMgr geMgr;

    /**
     * Group manager.
     */
    private final GroupManager groupManager;

    /**
     * Is saved?
     */
    private boolean isSaved = false;

    /**
     * Plug-In admin.
     */
    private String userNames;

    /**
     * Saved groups.
     */
    private List<String> savedGroups;

    /**
     * Selected groups.
     */
    private String[] selectedGroups = new String[0];

    /**
     * Constructor.
     */
    public GodConfigureAction(
        ApplicationProperties applicationProperties,
        GodEventsMgr geMgr,
        GroupManager groupManager)
    {
        this.applicationProperties = applicationProperties;
        this.geMgr = geMgr;
        this.groupManager = groupManager;

        // initialize params from saved properties
        dbHost = geMgr.getDbHost();
        dbPort = geMgr.getPort();
        dbName = geMgr.getDbName();
        dbUser = geMgr.getDbUser();
        dbPass = geMgr.getDbUserPass();
        userNames = geMgr.getAdminUserStr();
        selectedGroups = geMgr.getWorkGroups();
        savedGroups = selectedGroups == null ? null : Arrays.asList(geMgr.getWorkGroups());
    }

    @Override
    protected String doExecute()
    throws Exception
    {
        geMgr.setDbHost(dbHost);
        geMgr.setPort(dbPort);
        geMgr.setDbName(dbName);
        geMgr.setDbUser(dbUser);
        geMgr.setDbUserPass(dbPass);
        geMgr.setAdminUserStr(userNames);
        geMgr.setWorkGroups(selectedGroups);
        if (selectedGroups != null)
        {
            savedGroups = Arrays.asList(geMgr.getWorkGroups());
        }
        setSaved(true);

        return getRedirect("ViewGodEventsCfg!default.jspa?saved=true");
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();

        if (dbHost.isEmpty())
        {
            addErrorMessage("god.events.admin.error.dbhost");
            return;
        }

        if (dbPort.isEmpty())
        {
            addErrorMessage("god.events.admin.error.dbport");
            return;
        }
        else
        {
            try
            {
                Integer.parseInt(dbPort);
            }
            catch (NumberFormatException nex)
            {
                addErrorMessage("god.events.admin.error.dbport");
                return;
            }
        }

        if (dbName.isEmpty())
        {
            addErrorMessage("god.events.admin.error.dbname");
            return;
        }

        if (dbUser.isEmpty())
        {
            addErrorMessage("god.events.admin.error.dbuser");
            return;
        }

        if (dbPass.isEmpty())
        {
            addErrorMessage("god.events.admin.error.dbpass");
            return;
        }

        if (userNames.isEmpty())
        {
            addErrorMessage("god.events.admin.error.pgadmin");
            return;
        }

        if (!Connector.isDriverInitialized())
        {
            addErrorMessage("god.events.admin.error.driver");
            return;
        }

        Connector.initDataSource(dbHost, dbPort, dbName, dbUser, dbPass);
    }

    /**
     * Get all Jira groups.
     */
    public Collection<Group> getAllGroups()
    {
        return groupManager.getAllGroups();
    }

    /**
     * Get context path.
     */
    public String getBaseUrl()
    {
        return applicationProperties.getBaseUrl();
    }

    public String getDbHost()
    {
        return dbHost;
    }

    public String getDbName()
    {
        return dbName;
    }

    public String getDbPass()
    {
        return dbPass;
    }

    public String getDbPort()
    {
        return dbPort;
    }

    public String getDbUser()
    {
        return dbUser;
    }

    public List<String> getSavedGroups()
    {
        return savedGroups;
    }

    public String[] getSelectedGroups()
    {
        return selectedGroups;
    }

    public String getUserNames()
    {
        return userNames;
    }

    public boolean hasAdminPermission()
    {
        User user = getLoggedInUser();
        if (user == null)
        {
            return false;
        }

        return getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser());
    }

    public boolean isSaved()
    {
        return isSaved;
    }

    public void setDbHost(String dbHost)
    {
        this.dbHost = dbHost;
    }

    public void setDbName(String dbName)
    {
        this.dbName = dbName;
    }

    public void setDbPass(String dbPass)
    {
        this.dbPass = dbPass;
    }

    public void setDbPort(String dbPort)
    {
        this.dbPort = dbPort;
    }

    public void setDbUser(String dbUser)
    {
        this.dbUser = dbUser;
    }

    public void setSaved(boolean isSaved)
    {
        this.isSaved = isSaved;
    }

    public void setSavedGroups(List<String> savedGroups)
    {
        this.savedGroups = savedGroups;
    }

    public void setSelectedGroups(String[] selectedGroups)
    {
        this.selectedGroups = selectedGroups;
    }

    public void setUserNames(String userNames)
    {
        this.userNames = userNames;
    }
}
