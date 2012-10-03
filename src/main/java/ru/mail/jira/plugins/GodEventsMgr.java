package ru.mail.jira.plugins;

import java.util.Set;

/**
 * Configuration settings.
 * 
 * @author Andrey Markelov
 */
public interface GodEventsMgr
{
    Set<String> getAdminUsers();

    String getAdminUserStr();

    String getDbHost();

    String getDbName();

    String getDbUser();

    String getDbUserPass();

    String getPort();

    String[] getWorkGroups();

    void setAdminUsers(Set<String> users);

    void setAdminUserStr(String users);

    void setDbHost(String dbHost);

    void setDbName(String dbName);

    void setDbUser(String dbUser);

    void setDbUserPass(String dbUserPass);

    void setPort(String port);

    void setWorkGroups(String[] workGroups);
}
