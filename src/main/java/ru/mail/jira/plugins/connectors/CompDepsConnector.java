package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.structs.CompDep;

public class CompDepsConnector
    extends Connector
{
    /**
     * Constructor.
     */
    public CompDepsConnector(GodEventsMgr geMgr)
    {
        super(geMgr);
    }

    public int addCompDep(
        String name,
        String comment)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_COMPANY_DEPARTMENT, PreparedStatement.RETURN_GENERATED_KEYS);
            pStmt.setString(1, name);
            pStmt.setString(2, comment);
            pStmt.executeUpdate();

            rs = pStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException sqlex)
        {
            throw sqlex;
        }
        finally
        {
            close(rs, pStmt, conn);
        }
    }

    public void deleteCompDep(int id)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_COMPANY_DEPARTMENT);
            pStmt.setInt(1, id);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    /**
     * This method returns user permission for this plugin.
     */
    public List<CompDep> getCompDeps()
    throws SQLException
    {
        List<CompDep> res = new ArrayList<CompDep>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.COMPANY_DEPARTMENT_LIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
            	CompDep pt = new CompDep();
                pt.setId(rs.getInt(1));
                pt.setName(rs.getString(2));
                pt.setComment(rs.getString(3));
                res.add(pt);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }
}
