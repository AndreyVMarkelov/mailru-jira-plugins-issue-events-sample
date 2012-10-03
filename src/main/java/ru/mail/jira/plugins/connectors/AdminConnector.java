package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.structs.UserCard;

/**
 * 
 * 
 * @author Andrey Markelov
 */
public class AdminConnector
    extends Connector
{
    /**
     * Constructor.
     */
    public AdminConnector(GodEventsMgr geMgr)
    {
        super(geMgr);
    }

    /**
     * 
     */
    public void addGameMgr(String user, int gpId)
    throws SQLException
    {
        Map<String, UserCard> users = getUsers();
        for (Map.Entry<String, UserCard> entry : users.entrySet())
        {
            String euser = entry.getKey();
            if (user.equals(euser) && entry.getValue().getDivisions().contains(gpId))
            {
                return;
            }
        }

        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_GAME_MGR);
            pStmt.setString(1, user);
            pStmt.setInt(2, gpId);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void addProjMgr(String user, int parseInt)
    throws SQLException
    {
        Map<String, UserCard> users = getUsers();
        for (Map.Entry<String, UserCard> entry : users.entrySet())
        {
            String euser = entry.getKey();
            if (user.equals(euser) && entry.getValue().getDivisions().contains(parseInt))
            {
                return;
            }
        }

        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_DEP_MGR);
            pStmt.setString(1, user);
            pStmt.setInt(2, parseInt);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    /**
     * Add vice-presidents.
     */
    public void addVPs(String[] vps)
    throws SQLException
    {
        List<String> sUsers = Arrays.asList(vps);
        Map<String, UserCard> users = getUsers();
        List<String> updUsers = new ArrayList<String>();

        for (String s : sUsers)
        {
            if (users.containsKey(s))
            {
                if (!users.get(s).isVP())
                {
                    updUsers.add(s);
                }
            }
            else
            {
                updUsers.add(s);
            }
        }

        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_VP);
            for (String user : updUsers)
            {
                pStmt.clearParameters();
                pStmt.setString(1, user);
                pStmt.executeUpdate();
            }
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteDepMgr(String user, int cp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_DEP_MGR);
            pStmt.setString(1, user);
            pStmt.setInt(2, cp);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteDepMgrAll(int cp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_DEP_MGR_ALL);
            pStmt.setInt(1, cp);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteGameMgr(String user, int gp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_GAME_MGR);
            pStmt.setString(1, user);
            pStmt.setInt(2, gp);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteGameMgrAll(int gp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_GAME_MGR_ALL);
            pStmt.setInt(1, gp);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    /**
     * Delete a vice-president.
     */
    public void deleteVP(String user)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_VP);
            pStmt.setString(1, user);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }
}
