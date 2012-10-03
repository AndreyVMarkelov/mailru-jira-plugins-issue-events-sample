package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.structs.EventKind;
import ru.mail.jira.plugins.structs.UserCard;

/**
 * JDBC connector class.
 * 
 * @author Andrey Markelov
 */
public class Connector
{
    /**
     * Datasource.
     */
    protected static DataSource dataSource;

    /**
     * JDBC driver class.
     */
    private final static String DRIVER_CLASS = "com.mysql.jdbc.Driver";

    private static boolean isDriverInitialized = false;

    /**
     * Lock object for initialization datasource object.
     */
    private static final Object lock = new Object();

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(Connector.class);

    private static GenericObjectPool pool = null;

    static
    {
        try
        {
            Class.forName(DRIVER_CLASS).newInstance();
            isDriverInitialized = true;
        }
        catch (Exception e)
        {
            isDriverInitialized = false;
        }
    }

    public static synchronized void initDataSource(
        String host,
        String port,
        String dbName,
        String user,
        String password)
    {
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8", host, port, dbName);
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        connectionPool.setMinIdle(1);
        connectionPool.setMaxActive(4);
        connectionPool.setMaxWait(120000);
        connectionPool.setTestOnBorrow(true);
        pool = connectionPool; 

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
            jdbcUrl,
            user,
            password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
            connectionFactory,
            connectionPool,
            null,
            null,
            false,
            true);
        dataSource = new PoolingDataSource(poolableConnectionFactory.getPool()); 
    }

    public static boolean isDriverInitialized()
    {
        return isDriverInitialized;
    }

    public static void setDriverInitialized(boolean isDriverInitialized)
    {
        Connector.isDriverInitialized = isDriverInitialized;
    }

    public Connector(GodEventsMgr geMgr)
    {
        synchronized (lock)
        {
            if (dataSource == null)
            {
                initDataSource(
                    geMgr.getDbHost(),
                    geMgr.getPort(),
                    geMgr.getDbName(),
                    geMgr.getDbUser(),
                    geMgr.getDbUserPass());
            }
        }
    }

    protected void close(ResultSet rs, PreparedStatement pStmt, Connection conn)
    {
        closeResultSet(rs);
        closePreparedStatement(pStmt);
        closeConnection(conn);
    }

    protected void closeConnection(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                log.error("Cannot close connection.", e);
            }
        }
    }

    protected void closePreparedStatement(PreparedStatement pStmt)
    {
        if (pStmt != null)
        {
            try
            {
                pStmt.close();
            }
            catch (SQLException e)
            {
                log.error("Cannot close prepared statement", e);
            }
        }
    }

    protected void closeResultSet(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                log.error("Cannot close resultset", e);
            }
        }
    }

    /**
     * Get business models map.
     */
    public Map<Integer, String> getBmDict()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.BUSYNESS_MODEL_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get company departments map.
     */
    public Map<Integer, String> getCpDict()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.COMPANY_DEPARTMENT_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    public Map<Integer, String> getTerritories()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.TERRITORY_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get company departments map.
     */
    public Map<Integer, EventKind> getEks()
    throws SQLException
    {
        Map<Integer, EventKind> res = new HashMap<Integer, EventKind>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.EVENT_KINDS_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                EventKind ek = new EventKind(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                res.put(ek.getId(), ek);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get company departments map.
     */
    public Map<Integer, String> getEkDict()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.EVENT_KINDS_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get game projects map.
     */
    public Map<Integer, String> getGpDict()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.GAME_PROJECT_MAP);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get project types map.
     */
    public Map<Integer, String> getPtDict()
    throws SQLException
    {
        Map<Integer, String> res = new HashMap<Integer, String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.PROJECT_TYPE_LIST_ALL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.put(rs.getInt(1), rs.getString(2));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get unique game projects names.
     */
    public List<String> getUniqueGpNames()
    throws SQLException
    {
        List<String> res = new ArrayList<String>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.UNIQUE_PROJ_NAMES);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                res.add(rs.getString(1));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get users.
     */
    public Map<String, UserCard> getUsers()
    throws SQLException
    {
        Map<String, UserCard> res = new TreeMap<String, UserCard>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ALL_USERS);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                String user = rs.getString(1);
                if (!res.containsKey(user))
                {
                    res.put(user, new UserCard());
                }

                UserCard uc = res.get(user);
                int d = rs.getInt(2);
                int gp = rs.getInt(3);
                int vp = rs.getInt(4);

                if (gp != 0)
                {
                    uc.addGameProject(gp);
                }

                if (d != 0)
                {
                    uc.addDivision(d);
                }

                if (vp != 0)
                {
                    uc.setVP(true);
                }
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * This method returns user permission for this plugin.
     */
    public UserCard getUserStatus(String user)
    throws SQLException
    {
        UserCard uc = new UserCard();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.USER_ROLES_INFO);
            pStmt.setString(1, user);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                int gp = rs.getInt(1);
                int d = rs.getInt(2);
                int vp = rs.getInt(3);

                if (gp != 0)
                {
                    uc.addGameProject(gp);
                }

                if (d != 0)
                {
                    uc.addDivision(d);
                }

                if (vp != 0)
                {
                    uc.setVP(true);
                }
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return uc;
    }

    public int getUserPref(String user)
    throws SQLException
    {
        int val = 2;

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.USER_PREF_FIND);
            pStmt.setString(1, user);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                val = rs.getInt(3);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return val;
    }

    public void updateUserPref(String user, int ival)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.USER_PREF_FIND);
            pStmt.setString(1, user);
            rs = pStmt.executeQuery();
            boolean is = rs.next();
            closePreparedStatement(pStmt);

            if (is)
            {
                pStmt = conn.prepareStatement(SqlQueries.USER_PREF_UPDATE);
                pStmt.setInt(1, ival);
                pStmt.setString(2, user);
            }
            else
            {
                pStmt = conn.prepareStatement(SqlQueries.USER_PREF_ADD);
                pStmt.setString(1, user);
                pStmt.setInt(2, ival);
            }
            pStmt.executeUpdate();
        }
        finally
        {
            close(rs, pStmt, conn);
        }
    }
}
