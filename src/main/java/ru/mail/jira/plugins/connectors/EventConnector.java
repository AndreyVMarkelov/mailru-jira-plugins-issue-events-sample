package ru.mail.jira.plugins.connectors;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.mail.jira.plugins.GodEventsMgr;
import ru.mail.jira.plugins.SqlQueries;
import ru.mail.jira.plugins.Utils;
import ru.mail.jira.plugins.structs.CurrentEvent;
import ru.mail.jira.plugins.structs.EventKind;
import ru.mail.jira.plugins.structs.EventStruct;
import ru.mail.jira.plugins.structs.FileStruct;
import ru.mail.jira.plugins.structs.GameProject;
import ru.mail.jira.plugins.structs.GameProjectStruct;
import ru.mail.jira.plugins.structs.GodFileItem;

public class EventConnector
    extends Connector
{
    /**
     * Constructor.
     */
    public EventConnector(GodEventsMgr geMgr)
    {
        super(geMgr);
    }

    /**
     * Add new event.
     */
    public int addCurrEvent(CurrentEvent ce, String[] files)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            pStmt = conn.prepareStatement(SqlQueries.ADD_EVENT, PreparedStatement.RETURN_GENERATED_KEYS);
            pStmt.setString(1, ce.getName());
            pStmt.setString(2, ce.getDescr());
            pStmt.setInt(3, ce.getGp());
            pStmt.setInt(4, ce.getEk());
            pStmt.setDate(5, new Date(ce.getStartDate()));
            pStmt.setString(6, ce.getStartTime());
            if (ce.getEndDate() != -1)
            {
                pStmt.setDate(7, new Date(ce.getEndDate()));
            }
            else
            {
                pStmt.setNull(7, Types.DATE);
            }
            pStmt.setString(8, ce.getEndTime());
            pStmt.executeUpdate();

            rs = pStmt.getGeneratedKeys();
            rs.next();
            int evId = rs.getInt(1);

            closePreparedStatement(pStmt);

            if (files != null)
            {
                pStmt = conn.prepareStatement(SqlQueries.UPDATE_FILE);
                for (String file : files)
                {
                    pStmt.clearParameters();
                    pStmt.setInt(1, evId);
                    pStmt.setInt(2, Integer.parseInt(file));
                    pStmt.executeUpdate();
                }
            }

            conn.commit();

            return evId;
        }
        catch (SQLException sqlex)
        {
            conn.rollback();
            throw sqlex;
        }
        finally
        {
            if (conn != null)
            {
                conn.setAutoCommit(true);
            }

            close(rs, pStmt, conn);
        }
    }

    /**
     * Add kind of event to database.
     */
    public void addEventKind(
        String name,
        String comment,
        int type)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_EVENT_KINDS);
            pStmt.setString(1, name);
            pStmt.setString(2, comment);
            pStmt.setInt(3, type);
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

    public int addGameProject(GameProject gp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_GAME_PROJECT, PreparedStatement.RETURN_GENERATED_KEYS);
            pStmt.setString(1, gp.getLocalName());
            pStmt.setString(2, gp.getDescr());
            pStmt.setString(3, gp.getOrigName());
            pStmt.setInt(4, gp.getProjType());
            pStmt.setString(5, gp.getLogo());
            pStmt.setString(6, gp.getPage());
            pStmt.setString(7, gp.getDeveloper());
            pStmt.setInt(8, gp.getCompDep());
            pStmt.setInt(9, gp.getBModel());
            pStmt.setInt(10, gp.getTerritory());
            pStmt.setInt(11, gp.getStatus());
            pStmt.executeUpdate();

            rs = pStmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
        finally
        {
            close(rs, pStmt, conn);
        }
    }

    public void deleteEvent(int evId)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_EVENT);
            pStmt.setInt(1, evId);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    /**
     * Disable kind of event.
     */
    public void deleteEventKind(int evId)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_EVENT_KINDS);
            pStmt.setInt(1, evId);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public void deleteGameProject(
        int gpId)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.DELETE_GAME_PROJECT);
            pStmt.setInt(1, gpId);
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }

    public CurrentEvent getCurrentEvent(int evId)
    throws SQLException
    {
        CurrentEvent ce = null;

    	Connection conn = null;
    	PreparedStatement pStmt = null;
    	ResultSet rs = null;
    	try
    	{
    	    conn = dataSource.getConnection();
    	    pStmt = conn.prepareStatement(SqlQueries.GET_CURRENT_EVENT);
    	    pStmt.setInt(1, evId);
    	    rs = pStmt.executeQuery();
    	    while (rs.next())
    	    {
    	        ce = new CurrentEvent();
    	        ce.setId(rs.getInt(1));
    	        ce.setName(rs.getString(2));
    	        ce.setDescr(rs.getString(3));
    	        ce.setGp(rs.getInt(4));
    	        ce.setEk(rs.getInt(5));
    	        ce.setStartDate(Utils.getSqlDateAsLong(rs.getDate(6)));
    	        ce.setStartTime(rs.getString(7));
    	        ce.setEndDate(Utils.getSqlDateAsLong(rs.getDate(8)));
    	        ce.setEndTime(rs.getString(9));
    	    }
    	}
    	finally
    	{
    	    close(rs, pStmt, conn);
    	}

    	return ce;
    }

    public List<FileStruct> getEventFiles(int evId)
    throws SQLException
    {
        List<FileStruct> res = new ArrayList<FileStruct>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
    	    pStmt = conn.prepareStatement(SqlQueries.GET_EVENT_FILES);
    	    pStmt.setInt(1, evId);
    	    rs = pStmt.executeQuery();
    	    while (rs.next())
    	    {
    	        FileStruct fs = new FileStruct();
    	        fs.setId(rs.getInt(1));
    	        fs.setName(rs.getString(2));
    	        fs.setComment(rs.getString(3));
    	        fs.setEventId(evId);
    	        res.add(fs);
    	    }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * 
     */
    public List<EventKind> getEventKinds()
    throws SQLException
    {
        List<EventKind> res = new ArrayList<EventKind>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.EVENT_KINDS_LIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                EventKind pt = new EventKind();
                pt.setId(rs.getInt(1));
                pt.setName(rs.getString(2));
                pt.setComment(rs.getString(3));
                pt.setEtype(rs.getInt(4));
                res.add(pt);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Return list of events.
     */
    public List<CurrentEvent> getEvents()
    throws SQLException
    {
    	List<CurrentEvent> res = new ArrayList<CurrentEvent>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.EVENT_LIST_EXIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                CurrentEvent ce = new CurrentEvent();
                ce.setId(rs.getInt(1));
                ce.setName(rs.getString(2));
                ce.setDescr(rs.getString(3));
                ce.setGp(rs.getInt(4));
                ce.setEk(rs.getInt(5));
                ce.setStartDate(Utils.getSqlDateAsLong(rs.getDate(6)));
                ce.setStartTime(rs.getString(7));
                ce.setEndDate(Utils.getSqlDateAsLong(rs.getDate(8)));
                ce.setEndTime(rs.getString(9));
                res.add(ce);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Return list of events.
     */
    public Map<Integer, CurrentEvent> getEvDict()
    throws SQLException
    {
        Map<Integer, CurrentEvent> res = new HashMap<Integer, CurrentEvent>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.EVENT_LIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                CurrentEvent ce = new CurrentEvent();
                ce.setId(rs.getInt(1));
                ce.setName(rs.getString(2));
                ce.setDescr(rs.getString(3));
                ce.setGp(rs.getInt(4));
                ce.setEk(rs.getInt(5));
                ce.setStartDate(rs.getDate(6).getTime());
                ce.setStartTime(rs.getString(7));
                ce.setEndDate(Utils.getSqlDateAsLong(rs.getDate(8)));
                ce.setEndTime(rs.getString(9));
                res.put(ce.getId(), ce);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    /**
     * Get saved file.
     */
    public GodFileItem getFile(int fileId)
    throws SQLException
    {
        GodFileItem fi = null;

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.GET_SAVED_FILE);
            pStmt.setInt(1, fileId);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                byte[] data = rs.getBytes(1);
                String name = rs.getString(2);
                fi = new  GodFileItem(data, name);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return fi;
    }

    public GameProject getGameProject(int gpId)
    throws SQLException
    {
        GameProject gp = null;

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.GET_GAME_PROJECT);
            pStmt.setInt(1, gpId);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                gp = new GameProject();
                gp.setId(rs.getInt(1));
                gp.setLocalName(rs.getString(2));
                gp.setDescr(rs.getString(3));
                gp.setOrigName(rs.getString(4));
                gp.setProjType(rs.getInt(5));
                gp.setLogo(rs.getString(6));
                gp.setPage(rs.getString(7));
                gp.setDeveloper(rs.getString(8));
                gp.setCompDep(rs.getInt(9));
                gp.setbModel(rs.getInt(10));
                gp.setTerritory(rs.getInt(11));
                gp.setStatus(rs.getInt(12));
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return gp;
    }

    public List<GameProject> getGameProjects()
    throws SQLException
    {
        List<GameProject> res = new ArrayList<GameProject>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.GAME_PROJECT_LIST_EXIST);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                GameProject gp = new GameProject();
                gp.setId(rs.getInt(1));
                gp.setLocalName(rs.getString(2));
                gp.setOrigName(rs.getString(3));
                gp.setProjType(rs.getInt(4));
                gp.setLogo(rs.getString(5));
                gp.setPage(rs.getString(6));
                gp.setDeveloper(rs.getString(7));
                gp.setCompDep(rs.getInt(8));
                gp.setbModel(rs.getInt(9));
                gp.setTerritory(rs.getInt(10));
                gp.setStatus(rs.getInt(11));
                res.add(gp);
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return res;
    }

    public Map<Integer, GameProjectStruct> getReport(
        String[] icp,
        String[] ipt,
        String[] ibm,
        String[] iorigname,
        String[] iterritory,
        String[] iek,
        String istartDate,
        String iendDate)
    throws SQLException
    {
        Map<Integer, GameProjectStruct> prs = new HashMap<Integer, GameProjectStruct>();

        List<String> tokens = new ArrayList<String>();

        tokens.add("((A.STARTDATE >= '" + istartDate + "' AND A.STARTDATE <= '" + iendDate + "') OR (A.ENDDATE >= '" + istartDate + "' AND A.ENDDATE <= '" + iendDate + "') OR (A.STARTDATE <= '" + istartDate + "' AND A.ENDDATE >= '" + iendDate + "'))");

        if (icp != null && icp.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < icp.length; i++)
            {
                if (i == 0)
                {
                    sb.append(icp[i]);
                }
                else
                {
                    sb.append(",").append(icp[i]);
                }
            }
            sb.append(")");
            tokens.add("B.COMPANY_DEPT IN " + sb.toString());
        }

        if (ibm != null && ibm.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < ibm.length; i++)
            {
                if (i == 0)
                {
                    sb.append(ibm[i]);
                }
                else
                {
                    sb.append(",").append(ibm[i]);
                }
            }
            sb.append(")");
            tokens.add("B.BMODEL IN " + sb.toString());
        }

        if (ipt != null && ipt.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < ipt.length; i++)
            {
                if (i == 0)
                {
                    sb.append(ipt[i]);
                }
                else
                {
                    sb.append(",").append(ipt[i]);
                }
            }
            sb.append(")");
            tokens.add("B.PR_TYPE IN " + sb.toString());
        }

        if (iek != null && iek.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < iek.length; i++)
            {
                if (i == 0)
                {
                    sb.append(iek[i]);
                }
                else
                {
                    sb.append(",'").append(iek[i]).append("'");
                }
            }
            sb.append(")");
            tokens.add("A.EK_ID IN " + sb.toString());
        }

        if (iorigname != null && iorigname.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < iorigname.length; i++)
            {
                if (i == 0)
                {
                    sb.append("'").append(iorigname[i]).append("'");
                }
                else
                {
                    sb.append(",'").append(iorigname[i]).append("'");
                }
            }
            sb.append(")");
            tokens.add("B.LOCAL_NAME IN " + sb.toString());
        }

        if (iterritory != null && iterritory.length > 0)
        {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < iterritory.length; i++)
            {
                if (i == 0)
                {
                    sb.append("'").append(iterritory[i]).append("'");
                }
                else
                {
                    sb.append(",'").append(iterritory[i]).append("'");
                }
            }
            sb.append(")");
            tokens.add("B.TERRITORY IN " + sb.toString());
        }

        StringBuilder sb = new StringBuilder(SqlQueries.REPORT_QUERY);
        for (int i = 0; i < tokens.size(); i++)
        {
            if (i == 0)
            {
                sb.append(" WHERE ").append(tokens.get(i));
            }
            else
            {
                sb.append(" AND ").append(tokens.get(i));
            }
        }
        sb.append(" ORDER BY B.PR_TYPE, B.STATUS, B.TERRITORY, B.COMPANY_DEPT, B.BMODEL, B.LOCAL_NAME");

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(sb.toString());
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                int eId = rs.getInt(1);
                String eName = rs.getString(2);
                String eDescr = rs.getString(3);
                int gpId = rs.getInt(4);
                int eKind = rs.getInt(5);
                long startDate = (rs.getDate(6) != null) ? rs.getDate(6).getTime() : -1;
                String startTime = rs.getString(7);
                long endDate = (rs.getDate(8) != null) ? rs.getDate(8).getTime() : -1;
                String endTime = rs.getString(9);
                String gpLocalName = rs.getString(10);
                String gpDescr = rs.getString(11);
                String gpOrigName = rs.getString(12);
                int gpType = rs.getInt(13);
                String gpLogo = rs.getString(14);
                String gpPage = rs.getString(15);
                String gpDeveloper = rs.getString(16);
                int gpCp = rs.getInt(17);
                int gpBm = rs.getInt(18);
                int territory = rs.getInt(19);

                GameProjectStruct gps;
                if (prs.containsKey(gpId))
                {
                    gps = prs.get(gpId);
                    EventStruct es = new EventStruct();
                    es.setEventId(eId);
                    es.setKind(eKind);
                    es.setName(eName);
                    es.setDescr(eDescr);
                    es.setStartDate(startDate);
                    es.setEndDate(endDate);
                    es.setEndTime(endTime);
                    es.setStartTime(startTime);
                    gps.addEvent(es);
                }
                else
                {
                    gps = new GameProjectStruct();
                    gps.setProjectId(gpId);
                    gps.setbModel(gpBm);
                    gps.setProjectType(gpType);
                    gps.setCompDep(gpCp);
                    gps.setLocalName(gpLocalName);
                    gps.setDeveloper(gpDeveloper);
                    gps.setTerritory(territory);
                    gps.setLogo(gpLogo);
                    gps.setPage(gpPage);
                    gps.setDescr(gpDescr);
                    gps.setOrigName(gpOrigName);

                    EventStruct es = new EventStruct();
                    es.setEventId(eId);
                    es.setKind(eKind);
                    es.setName(eName);
                    es.setDescr(eDescr);
                    es.setStartDate(startDate);
                    es.setEndDate(endDate);
                    es.setEndTime(endTime);
                    es.setStartTime(startTime);
                    gps.addEvent(es);
                    prs.put(gpId, gps);
                }
            }
        }
        finally
        {
            close(rs, pStmt, conn);
        }

        return prs;
    }

    public int saveFile(
        String fileName,
        String comment,
        byte[] data)
    throws SQLException
    {
        int g = -1;

        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.ADD_FILE, Statement.RETURN_GENERATED_KEYS);
            pStmt.setString(1, fileName);
            pStmt.setString(2, comment);
            pStmt.setBytes(3, data);
            pStmt.executeUpdate();
            ResultSet gRs = pStmt.getGeneratedKeys();
            gRs.next();
            g = gRs.getInt(1);
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }

        return g;
    }

    public void updateCurrEvent(CurrentEvent ce, String[] files)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            pStmt = conn.prepareStatement(SqlQueries.UPDATE_CURRENT_EVENT);
            pStmt.setString(1, ce.getName());
            pStmt.setInt(2, ce.getGp());
            pStmt.setInt(3, ce.getEk());
            pStmt.setString(4, ce.getDescr());
            pStmt.setDate(5, new Date(ce.getStartDate()));
            pStmt.setString(6, ce.getStartTime());
            if (ce.getEndDate() != -1)
            {
                pStmt.setDate(7, new Date(ce.getEndDate()));
            }
            else
            {
                pStmt.setNull(7, Types.DATE);
            }
            pStmt.setString(8, ce.getEndTime());
            pStmt.setInt(9, ce.getId());
            pStmt.executeUpdate();

            closePreparedStatement(pStmt);

            if (files != null)
            {
                pStmt = conn.prepareStatement(SqlQueries.UPDATE_FILE);
                for (String file : files)
                {
                    pStmt.clearParameters();
                    pStmt.setInt(1, ce.getId());
                    pStmt.setInt(2, Integer.parseInt(file));
                    pStmt.executeUpdate();
                }
            }

            conn.commit();
        }
        catch (SQLException sqlex)
        {
            if (conn != null)
            {
                conn.rollback();
            }

            throw sqlex;
        }
        finally
        {
            if (conn != null)
            {
                conn.setAutoCommit(true);
            }

            close(rs, pStmt, conn);
        }
    }

    public void updateGameProject(GameProject gp)
    throws SQLException
    {
        Connection conn = null;
        PreparedStatement pStmt = null;
        try
        {
            conn = dataSource.getConnection();
            pStmt = conn.prepareStatement(SqlQueries.UPDATE_GAME_PROJECT);
            pStmt.setString(1, gp.getLocalName());
            pStmt.setString(2, gp.getDescr());
            pStmt.setString(3, gp.getOrigName());
            pStmt.setInt(4, gp.getProjType());
            pStmt.setString(5, gp.getLogo());
            pStmt.setString(6, gp.getPage());
            pStmt.setString(7, gp.getDeveloper());
            pStmt.setInt(8, gp.getCompDep());
            pStmt.setInt(9, gp.getBModel());
            pStmt.setInt(10, gp.getTerritory());
            pStmt.setInt(11, gp.getStatus());
            pStmt.setInt(12, gp.getId());
            pStmt.executeUpdate();
        }
        finally
        {
            closePreparedStatement(pStmt);
            closeConnection(conn);
        }
    }
}
