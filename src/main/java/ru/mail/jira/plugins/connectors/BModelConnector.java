package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.structs.BusynessModel;

public class BModelConnector
    extends Connector
{
    /**
     * Constructor.
     */
    public BModelConnector(GodEventsMgr geMgr)
    {
        super(geMgr);
    }

    public void addBModel(
        String name,
        String comment)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_BUSYNESS_MODEL);
            pStmt.setString(1, name);
            pStmt.setString(2, comment);
            pStmt.executeUpdate();
        }
        catch (SQLException sqlex)
        {
            if (!sqlex.getSQLState().equals("23000"))
            {
                throw sqlex;
            }
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteBModel(int id)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_BUSYNESS_MODEL);
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
    public List<BusynessModel> getBusynessModels()
    throws SQLException
    {
        List<BusynessModel> res = new ArrayList<BusynessModel>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.BUSYNESS_MODEL_LIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
            	BusynessModel pt = new BusynessModel();
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
