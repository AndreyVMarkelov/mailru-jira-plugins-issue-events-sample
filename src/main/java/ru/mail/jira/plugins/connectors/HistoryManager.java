package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.structs.History;
import ru.mail.jira.plugins.structs.HistoryDetail;

public class HistoryManager
    extends Connector
{
    public HistoryManager(GodEventsMgr geMgr)
    {
        super(geMgr);
    }

    public void addHistory(int type, int evId, List<HistoryDetail> details, String user)
   	throws SQLException
   	{
   	    Connection conn = null;
   	    PreparedStatement pStmt = null;
   	    ResultSet rs = null;
   	    try
   	    {
   	        conn = dataSource.getConnection();
   	        pStmt = conn.prepareStatement(SqlQueries.ADD_EVENT_HISTORY, PreparedStatement.RETURN_GENERATED_KEYS);
   	        pStmt.setInt(1, type);
   	        pStmt.setInt(2, evId);
   	        pStmt.setString(3, user);
   	        pStmt.executeUpdate();

   	        rs = pStmt.getGeneratedKeys();
   	        rs.next();
   	        int hid = rs.getInt(1);

            closePreparedStatement(pStmt);

            pStmt = conn.prepareStatement(SqlQueries.ADD_HISTORY_DETAIL, PreparedStatement.RETURN_GENERATED_KEYS);
            for (HistoryDetail hd : details)
            {
                pStmt.clearParameters();
                pStmt.setInt(1, hid);
                pStmt.setString(2, hd.getField());
                pStmt.setString(3, hd.getOldval());
                pStmt.setString(4, hd.getNewval());
                pStmt.executeUpdate();
            }
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
            close(rs, pStmt, conn);
        }
    }

    public List<History> getHistories(int type, int ival)
    throws SQLException
    {
        List<History> res = new ArrayList<History>();

        String query;
        if (type == 1)
        {
            query = SqlQueries.HISTORY_PROJECT_LIST;
        }
        else if (type == 2)
        {
            query = SqlQueries.HISTORY_EVENT_LIST;
        }
        else
        {
            query = SqlQueries.HISTORY_ALL_LIST;
        }

        Connection conn = null;
    	PreparedStatement pStmt = null;
    	ResultSet rs = null;
    	PreparedStatement pStmt2 = null;
        ResultSet rs2 = null;
        try
    	{
    	    conn = dataSource.getConnection();
    	    pStmt = conn.prepareStatement(query);
    	    pStmt.setInt(1, ival);
    	    pStmt2 = conn.prepareStatement(SqlQueries.HISTORY_DETAIL_LIST);
    	    rs = pStmt.executeQuery();
    	    while (rs.next())
    	    {
    	        History h = new History();
                h.setId(rs.getInt(1));
                h.setType(rs.getInt(2));
                h.setRefId(rs.getInt(3));
                h.setUtime(rs.getTimestamp(4));
                h.setUser(rs.getString(5));

                pStmt2.clearParameters();
                pStmt2.setLong(1, h.getId());
                rs2 = pStmt2.executeQuery();
                while (rs2.next())
                {
                    HistoryDetail hd = new HistoryDetail();
                    hd.setField(rs2.getString(3));
                    hd.setOldval(rs2.getString(4));
                    hd.setNewval(rs2.getString(5));
                    h.addHistoryDetail(hd);
                }
                closeResultSet(rs2);
                res.add(h);
    	    }
    	}
    	finally
    	{
    	    close(rs, pStmt, conn);
        }

        return res;
    }
}
