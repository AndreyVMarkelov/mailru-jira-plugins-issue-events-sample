package ru.mail.jira.plugins;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.connectors.BModelConnector;
import ru.mail.jira.plugins.connectors.CompDepsConnector;
import ru.mail.jira.plugins.connectors.Connector;
import ru.mail.jira.plugins.connectors.EventConnector;
import ru.mail.jira.plugins.connectors.HistoryManager;
import ru.mail.jira.plugins.connectors.ProjectTypesConnector;
import ru.mail.jira.plugins.structs.BusynessModel;
import ru.mail.jira.plugins.structs.CompDep;
import ru.mail.jira.plugins.structs.CurrentEvent;
import ru.mail.jira.plugins.structs.DictWrap;
import ru.mail.jira.plugins.structs.EventKind;
import ru.mail.jira.plugins.structs.EventRepr;
import ru.mail.jira.plugins.structs.EventStruct;
import ru.mail.jira.plugins.structs.EventTypes;
import ru.mail.jira.plugins.structs.FileStruct;
import ru.mail.jira.plugins.structs.GameProject;
import ru.mail.jira.plugins.structs.GameProjectStruct;
import ru.mail.jira.plugins.structs.HistWrap;
import ru.mail.jira.plugins.structs.History;
import ru.mail.jira.plugins.structs.Proj;
import ru.mail.jira.plugins.structs.ProjectType;
import ru.mail.jira.plugins.structs.UserCard;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

/**
 * Managing servlet.
 * 
 * @author Andrey Markelov
 */
public class ViewerServlet
    extends HttpServlet
{
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ViewerServlet.class);

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = -4670316903271752949L;

    /**
     * Configuration manager.
     */
    private final GodEventsMgr geMgr;

    /**
     * Group manager.
     */
    private final GroupManager groupManager;

    /**
     * Template renderer.
     */
    private final TemplateRenderer renderer;

    /**
     * Utility for work with JIRA users.
     */
    private final UserUtil userUtil;

    /**
     * Login URI provider.
     */
    private final LoginUriProvider loginUriProvider;

    /**
     * Constructor.
     */
    public ViewerServlet(
        GodEventsMgr geMgr,
        GroupManager groupManager,
        TemplateRenderer renderer,
        UserUtil userUtil,
        LoginUriProvider loginUriProvider)
    {
        this.geMgr = geMgr;
        this.groupManager = groupManager;
        this.renderer = renderer;
        this.userUtil = userUtil;
        this.loginUriProvider = loginUriProvider;
    }

    /**
     * Return display name of user if user exists in JIRA.
     */
    private String getDisplayUser(UserUtil userUtil, String user)
    {
        User userObj = userUtil.getUserObject(user);
        if (userObj != null)
        {
            return userObj.getDisplayName();
        }
        else
        {
            return user;
        }
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
    throws IOException
    {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    @Override
    protected void doGet(
        HttpServletRequest req,
        HttpServletResponse resp)
    throws ServletException, IOException
    {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();

        if (user == null)
        {
            redirectToLogin(req, resp);
            return;
        }

        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            resp.sendError(403);
            return;
        }

        String page = req.getParameter("page");

        if (page == null)
        {
            resp.sendError(404);
        }
        else if (page.equals("addcompdep"))
        {
            pageAddCompDep(req, resp, user);
        }
        else if (page.equals("history"))
        {
            pageHistory(req, resp, user);
        }
        else if (page.equals("prefs"))
        {
            pagePrefs(req, resp, user);
        }
        else if (page.equals("project"))
        {
            pageProject(req, resp, user);
        }
        else if (page.equals("event"))
        {
            pageEvent(req, resp, user);
        }
        else if (page.equals("reports"))
        {
            try
            {
                pageReports(req, resp, user);
            }
            catch (RenderingException e)
            {
                e.printStackTrace();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        else if (page.equals("settings"))
        {
            if (geMgr.getAdminUsers().contains(user.getName()))
            {
                pageAdmin(req, resp, user);
                return;
            }

            Map<String, UserCard> uc;
            try
            {
                EventConnector c = new EventConnector(geMgr);
                uc = c.getUsers();

                if (uc.containsKey(user.getName()))
                {
                    UserCard uCard = uc.get(user.getName());
                    if (uCard.isVP())
                    {
                        pageCompDeps(req, resp, user);
                    }
                    else if (!uCard.getDivisions().isEmpty())
                    {
                        pageGameProjects(req, resp, user);
                    }
                    else if (!uCard.getGameProjects().isEmpty())
                    {
                        pageEvents(req, resp, user);
                    }
                    else
                    {
                        resp.sendError(403);
                        return;
                    }
                }
                else
                {
                    pagePrefs(req, resp, user);
                }
            }
            catch (SQLException e)
            {
                resp.sendError(500, e.getMessage());
                return;
            }
        }
        else if (page.equals("admin"))
        {
            pageAdmin(req, resp, user);
        }
        else if (page.equals("bmodels"))
        {
            pageBModels(req, resp, user);
        }
        else if (page.equals("compdeps"))
        {
            pageCompDeps(req, resp, user);
        }
        else if (page.equals("events"))
        {
            pageEvents(req, resp, user);
        }
        else if (page.equals("gameprojects"))
        {
            pageGameProjects(req, resp, user);
        }
        else if (page.equals("projtypes"))
        {
            pageProjTypes(req, resp, user);
        }
        else if (page.equals("eventtypes"))
        {
            pageEventKinds(req, resp, user);
        }
        else if (page.equals("addproject"))
        {
            pageAddProject(req, resp, user);
        }
        else if (page.equals("addcurrevent"))
        {
            pageCurrEvent(req, resp, user);
        }
        else if (page.equals("editproject"))
        {
            pageEditProject(req, resp, user);
        }
        else if (page.equals("editcurrevent"))
        {
            pageEditCurrEvent(req, resp, user);
        }
        else if (page.equals("links"))
        {
            pageLinks(req, resp, user);
        }
        else
        {
            resp.sendError(404);
        }
    }

    private void pageHistory(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws ServletException, IOException
    {
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_PREFS);

        SimpleDateFormat sdt = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> eks;
        Map<Integer, String> gpss;
        Map<Integer, String> gpTerr;
        List<History> hist;
        Map<Integer, CurrentEvent> evs;
        try
        {
            EventConnector c = new EventConnector(geMgr);
            cds = c.getCpDict();
            pts = c.getPtDict();
            bms = c.getBmDict();
            eks = c.getEkDict();
            gpss = c.getGpDict();
            gpTerr = c.getTerritories();
            evs = c.getEvDict();
            int ival = c.getUserPref(user.getName());

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hist = hsMgr.getHistories(3, ival);
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        for (History h : hist)
        {
            h.setUser(getDisplayUser(userUtil, h.getUser()));
        }

        DictWrap dwrap = new DictWrap(cds, pts, bms, eks, gpTerr, gpss);

        parms.put("hist", new HistWrap(hist));
        parms.put("gpss", gpss);
        parms.put("evs", evs);
        parms.put("dwrap", dwrap);
        parms.put("sdt", sdt);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/showHistory.vm", parms, resp.getWriter());
    }

    private void pageAddCompDep(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws ServletException, IOException
    {
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_PREFS);

        UserCard card;
        try
        {
            Connector c = new Connector(geMgr);
            card = c.getUserStatus(user.getName());
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, String> allUsers = new HashMap<String, String>();
        for (String group : geMgr.getWorkGroups())
        {
            Collection<User> users = groupManager.getUsersInGroup(group);
            for (User usr : users)
            {
                allUsers.put(usr.getName(), usr.getDisplayName());
            }
        }

        parms.put("allUsers", allUsers);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("card", card);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/addcompdep.vm", parms, resp.getWriter());
    }

    private void pagePrefs(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws ServletException, IOException
    {
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_PREFS);
        parms.put("sts", Utils.getStatusMap());

        Integer ival;
        UserCard card;
        try
        {
            Connector c = new Connector(geMgr);
            card = c.getUserStatus(user.getName());
            ival = c.getUserPref(user.getName());
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        parms.put("ival", ival);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("card", card);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/userprefs.vm", parms, resp.getWriter());
    }

    @Override
    protected void doPost(
        HttpServletRequest req,
        HttpServletResponse resp)
    throws ServletException, IOException
    {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();

        if (user == null)
        {
            redirectToLogin(req, resp);
            return;
        }

        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            resp.sendError(403);
            return;
        }

        String page = req.getParameter("page");

        if (page == null)
        {
            resp.sendError(404);
        }
        else if (page.equals("reports"))
        {
            try
            {
                pageReports(req, resp, user);
            }
            catch (RenderingException e)
            {
                e.printStackTrace();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            resp.sendError(404);
        }
    }

    /**
     * Return context path of JIRA.
     */
    private String getBaseUrl(HttpServletRequest req)
    {
        return (req.getScheme() + "://" + req.getServerName() + ":" +
            req.getServerPort() + req.getContextPath());
    }

    private void pageAddProject(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
            IOException
    {
        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENT);
        parms.put("sts", Utils.getStatusMap());

        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> gpTerr;
        UserCard card;
        try
        {
            Connector c = new Connector(geMgr);
            card = c.getUserStatus(user.getName());
            cds = c.getCpDict();
            pts = c.getPtDict();
            bms = c.getBmDict();
            gpTerr = c.getTerritories();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, String> allUsers = new HashMap<String, String>();
        for (String group : geMgr.getWorkGroups())
        {
            Collection<User> users = groupManager.getUsersInGroup(group);
            for (User usr : users)
            {
                allUsers.put(usr.getName(), usr.getDisplayName());
            }
        }

        if (!isAdmin)
        {
            Iterator<Integer> iter = cds.keySet().iterator();
            while (iter.hasNext())
            {
                Integer cp = iter.next();
                if (!card.getDivisions().contains(cp))
                {
                    iter.remove();
                }
            }
        }

        parms.put("isAdmin", isAdmin);
        parms.put("cds", cds);
        parms.put("allUsers", allUsers);
        parms.put("pts", pts);
        parms.put("bms", bms);
        parms.put("gpTerr", gpTerr);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/addproject.vm", parms, resp.getWriter());
    }

    /**
     * Go to admin page.
     */
    private void pageAdmin(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        if (!geMgr.getAdminUsers().contains(user.getName()))
        {
            log.error("ViewerServlet::pageAdmin - User has no privileges to perform an action");
            resp.sendError(403);
            return;
        }

        UserCard card;
        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> gps;
        Map<String, UserCard> uc;
        try
        {
            Connector c = new Connector(geMgr);
            card = c.getUserStatus(user.getName());
            cds = c.getCpDict();
            pts = c.getPtDict();
            bms = c.getBmDict();
            gps = c.getGpDict();
            uc = c.getUsers();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, String> allUsers = new HashMap<String, String>();
        for (String group : geMgr.getWorkGroups())
        {
            Collection<User> users = groupManager.getUsersInGroup(group);
            for (User usr : users)
            {
                allUsers.put(usr.getName(), usr.getDisplayName());
            }
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_ADMIN);
        parms.put("i18n", ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper());
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("allUsers", allUsers);
        parms.put("card", card);
        parms.put("cds", cds);
        parms.put("pts", pts);
        parms.put("bms", bms);
        parms.put("gps", gps);
        parms.put("uc", uc);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/admins.vm", parms, resp.getWriter());
    }

    /**
     * Go to business models.
     */
    private void pageBModels(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        UserCard card;
        List<BusynessModel> bms;
        try 
        {
            BModelConnector bConn = new BModelConnector(geMgr);
            card = bConn.getUserStatus(user.getName());
            if (!Utils.allowVpDivAction(user, geMgr, card))
            {
                log.error("ViewerServlet::pageBModels - User has no privileges to perform an action");
                resp.sendError(403);
                return;
            }
            bms = bConn.getBusynessModels();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_BMODEL);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("card", card);
        parms.put("bms", bms);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/busyness-models.vm", parms, resp.getWriter());
    }

    /**
     * Go to company departments.
     */
    private void pageCompDeps(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        UserCard card;
        List<CompDep> cds;
        try
        {
            CompDepsConnector cpConn = new CompDepsConnector(geMgr);
            card = cpConn.getUserStatus(user.getName());
            if (!Utils.allowVpDivAction(user, geMgr, card))
            {
                log.error("ViewerServlet::pageCompDeps - User has no privileges to perform an action");
                resp.sendError(403);
                return;
            }
            cds = cpConn.getCompDeps();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_COMP_DEPS);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("card", card);
        parms.put("cds", cds);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/comp-deps.vm", parms, resp.getWriter());
    }

    /**
     * 
     */
    private void pageCurrEvent(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
           IOException
    {
        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        List<EventKind> eks;
        List<GameProject> gps;
        UserCard card;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            card = evCon.getUserStatus(user.getName());
            eks = evCon.getEventKinds();
            gps = evCon.getGameProjects();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        if (!isAdmin)
        {
            Iterator<GameProject> iter = gps.iterator();
            while (iter.hasNext())
            {
                GameProject gp = iter.next();
                if (!card.getGameProjects().contains(gp.getId()))
                {
                    iter.remove();
                }
            }
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENT);
        parms.put("eks", eks);
        parms.put("gps", gps);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/addcurrentevent.vm", parms, resp.getWriter());
    }

    private void pageEditCurrEvent(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
            IOException
    {
        String evId = req.getParameter("ev");

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENT);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        List<EventKind> eks;
        List<GameProject> gps;
        CurrentEvent ce;
        List<FileStruct> evFiles;
        UserCard card;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            card = evCon.getUserStatus(user.getName());
            eks = evCon.getEventKinds();
            gps = evCon.getGameProjects();
            ce = evCon.getCurrentEvent(Integer.parseInt(evId));
            evFiles = evCon.getEventFiles(Integer.parseInt(evId));
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        if (!isAdmin)
        {
            Iterator<GameProject> iter = gps.iterator();
            while (iter.hasNext())
            {
                GameProject gp = iter.next();
                if (!card.getGameProjects().contains(gp.getId()))
                {
                    iter.remove();
                }
            }
        }

        parms.put("isAdmin", true);
        parms.put("eks", eks);
        parms.put("gps", gps);
        parms.put("evId", evId);
        parms.put("ce", ce);
        parms.put("sdf", sdf);
        parms.put("evFiles", evFiles);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/editcurrevent.vm", parms, resp.getWriter());
    }

    private void pageEditProject(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
            IOException
    {
        String gpId = req.getParameter("gp");

        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENT);
        parms.put("sts", Utils.getStatusMap());

        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        GameProject gp;
        Map<Integer, String> gpTerr;
        UserCard card;
        Map<String, UserCard> cUsers;
        try
        {
            EventConnector c = new EventConnector(geMgr);
            card = c.getUserStatus(user.getName());
            cUsers = c.getUsers();
            cds = c.getCpDict();
            pts = c.getPtDict();
            bms = c.getBmDict();
            gp = c.getGameProject(Integer.parseInt(gpId));
            gpTerr = c.getTerritories();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, String> allUsers = new HashMap<String, String>();
        for (String group : geMgr.getWorkGroups())
        {
            Collection<User> users = groupManager.getUsersInGroup(group);
            for (User usr : users)
            {
                allUsers.put(usr.getName(), usr.getDisplayName());
            }
        }

        if (!isAdmin)
        {
            Iterator<Integer> iter = cds.keySet().iterator();
            while (iter.hasNext())
            {
                Integer cp = iter.next();
                if (!card.getDivisions().contains(cp))
                {
                    iter.remove();
                }
            }
        }

        parms.put("cds", cds);
        parms.put("pts", pts);
        parms.put("bms", bms);
        parms.put("allUsers", allUsers);
        parms.put("isAdmin", isAdmin);
        parms.put("gpId", Integer.parseInt(gpId));
        parms.put("gp", gp);
        parms.put("card", card);
        parms.put("gpTerr", gpTerr);
        parms.put("isAdmin", isAdmin);
        parms.put("cUsers", cUsers);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/editproject.vm", parms, resp.getWriter());
    }

    /**
     * Display information about event.
     */
    private void pageEvent(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
           IOException
    {
        String ev = req.getParameter("ev");

        int evId;
        try
        {
            evId = Integer.parseInt(ev);
        }
        catch(NumberFormatException nex)
        {
            resp.sendError(500, "An internal error");
            return;
        }

        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            resp.sendError(403);
            return;
        }

        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        List<EventKind> eks;
        List<GameProject> gps;
        CurrentEvent ce;
        List<FileStruct> evFiles;
        List<History> hist;
        Map<Integer, String> gpTerr;
        Map<Integer, String> gpss;
        Map<Integer, String> ekd;
        Map<Integer, CurrentEvent> evs;
        boolean hasPriv;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            eks = evCon.getEventKinds();
            gps = evCon.getGameProjects();
            ce = evCon.getCurrentEvent(evId);
            evFiles = evCon.getEventFiles(evId);
            cds = evCon.getCpDict();
            pts = evCon.getPtDict();
            bms = evCon.getBmDict();
            ekd = evCon.getEkDict();
            gpTerr = evCon.getTerritories();
            gpss = evCon.getGpDict();
            evs = evCon.getEvDict();
            int ival = evCon.getUserPref(user.getName());
            hasPriv = Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), ce.getGp());

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hist = hsMgr.getHistories(2, ival);
        }
        catch (SQLException e)
        {
            log.error("ViewerServlet::pageEvent - SQL error occurred", e);
            resp.sendError(500, "An internal error");
            return;
        }

        for (History h : hist)
        {
            h.setUser(getDisplayUser(userUtil, h.getUser()));
        }

        SimpleDateFormat sdt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DictWrap dwrap = new DictWrap(cds, pts, bms, ekd, gpTerr, gpss);

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("sdf", new SimpleDateFormat("yyyy-MM-dd"));
        parms.put("eks", eks);
        parms.put("gps", gps);
        parms.put("evId", evId);
        parms.put("ce", ce);
        parms.put("evFiles", evFiles);
        parms.put("hist", new HistWrap(hist, evId));
        parms.put("dwrap", dwrap);
        parms.put("gpss", gpss);
        parms.put("evs", evs);
        parms.put("sdt", sdt);
        parms.put("hasPriv", hasPriv);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/showEvent.vm", parms, resp.getWriter());
    }

    /**
     * Display information about event.
     */
    private void pageProject(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
           IOException
    {
        String ev = req.getParameter("pr");

        int prId;
        try
        {
            prId = Integer.parseInt(ev);
        }
        catch(NumberFormatException nex)
        {
            resp.sendError(500, "An internal error");
            return;
        }

        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            resp.sendError(403);
            return;
        }

        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        GameProject gp;
        Map<Integer, String> gpTerr;
        List<History> hist;
        Map<Integer, String> eks;
        Map<Integer, String> gpss;
        Map<Integer, CurrentEvent> evs;
        boolean hasPriv;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            cds = evCon.getCpDict();
            pts = evCon.getPtDict();
            bms = evCon.getBmDict();
            gp = evCon.getGameProject(prId);
            gpTerr = evCon.getTerritories();
            eks = evCon.getEkDict();
            gpss = evCon.getGpDict();
            evs = evCon.getEvDict();
            int ival = evCon.getUserPref(user.getName());
            hasPriv = Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), gp.getId());

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hist = hsMgr.getHistories(1, ival);
        }
        catch (SQLException e)
        {
            log.error("ViewerServlet::pageEvent - SQL error occurred", e);
            resp.sendError(500, "An internal error");
            return;
        }

        for (History h : hist)
        {
            h.setUser(getDisplayUser(userUtil, h.getUser()));
        }

        SimpleDateFormat sdt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DictWrap dwrap = new DictWrap(cds, pts, bms, eks, gpTerr, gpss);

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("sdf", new SimpleDateFormat("yyyy-MM-dd"));
        parms.put("cds", cds);
        parms.put("pts", pts);
        parms.put("prId", prId);
        parms.put("bms", bms);
        parms.put("gp", gp);
        parms.put("gpTerr", gpTerr);
        parms.put("sts", Utils.getStatusMap());
        parms.put("hist", new HistWrap(hist, prId));
        parms.put("gpss", gpss);
        parms.put("evs", evs);
        parms.put("dwrap", dwrap);
        parms.put("sdt", sdt);
        parms.put("hasPriv", hasPriv);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/showProject.vm", parms, resp.getWriter());
    }

    /**
     * Go to kinds of events page.
     */
    private void pageEventKinds(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException,
            IOException
    {
        List<EventKind> ek;
        UserCard card;
        try
        {
            EventConnector evConn = new EventConnector(geMgr);
            card = evConn.getUserStatus(user.getName());
            if (!Utils.allowVpDivAction(user, geMgr, card))
            {
                log.error("ViewerServlet::pageEventKinds - User has no privileges to perform an action");
                resp.sendError(403);
                return;
            }
            ek = evConn.getEventKinds();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENT);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));
        parms.put("card", card);
        parms.put("ek", ek);
        parms.put("types", EventTypes.getEventTypesDict());

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/eventkinds.vm", parms, resp.getWriter());
    }

    /**
     * Go to events.
     */
    private void pageEvents(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        UserCard card;
        Map<Integer, String> eks;
        Map<Integer, String> gps;
        List<CurrentEvent> currevs;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            card = evCon.getUserStatus(user.getName());
            eks = evCon.getEkDict();
            currevs = evCon.getEvents();
            gps = evCon.getGpDict();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        if (!isAdmin)
        {
            Iterator<CurrentEvent> iter = currevs.iterator();
            while (iter.hasNext())
            {
                CurrentEvent ce = iter.next();
                if (!card.getGameProjects().contains(ce.getGp()))
                {
                    iter.remove();
                }
            }
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_EVENTS);
        parms.put("isAdmin", isAdmin);
        parms.put("card", card);
        parms.put("eks", eks);
        parms.put("currevs", currevs);
        parms.put("gps", gps);
        parms.put("sdf", new SimpleDateFormat("yyyy-MM-dd"));
        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/events.vm", parms, resp.getWriter());
    }

    /**
     * Go to game projects.
     */
    private void pageGameProjects(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        boolean isAdmin = geMgr.getAdminUsers().contains(user.getName());

        UserCard card;
        List<GameProject> gps;
        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> gpTerr;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            card = evCon.getUserStatus(user.getName());
            gps = evCon.getGameProjects();
            cds = evCon.getCpDict();
            pts = evCon.getPtDict();
            bms = evCon.getBmDict();
            gpTerr = evCon.getTerritories();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        if (!isAdmin)
        {
            Iterator<GameProject> iter = gps.iterator();
            while (iter.hasNext())
            {
                GameProject gp = iter.next();
                if (!card.getGameProjects().contains(gp.getId()) && !card.getDivisions().contains(gp.getCompDep()))
                {
                    iter.remove();
                }
            }
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_GAME_PROJ);
        parms.put("card", card);
        parms.put("gps", gps);
        parms.put("cds", cds);
        parms.put("pts", pts);
        parms.put("bms", bms);
        parms.put("isAdmin", isAdmin);
        parms.put("sts", Utils.getStatusMap());
        parms.put("gpTerr", gpTerr);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/game-projects.vm", parms, resp.getWriter());
    }

    private void pageLinks(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        Boolean hasPerm = Boolean.TRUE;

/*        Map<String, UserCard> uc;
        try
        {
            Connector c = new Connector(geMgr);
            uc = c.getUsers();
            if (geMgr.getAdminUsers().contains(user.getName()) || uc.containsKey(user.getName()))
            {
                hasPerm = Boolean.TRUE;
            }
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }*/

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_ADMIN);
        parms.put("i18n", ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper());
        parms.put("hasPerm", hasPerm);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/linkspage.vm", parms, resp.getWriter());
    }

    /**
     * Go to project types.
     */
    private void pageProjTypes(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException
    {
        UserCard card;
        List<ProjectType> pts;
        try
        {
            ProjectTypesConnector ptCon =  new ProjectTypesConnector(geMgr);
            card = ptCon.getUserStatus(user.getName());
            if (!Utils.allowVpDivAction(user, geMgr, card))
            {
                log.error("ViewerServlet::pageProjTypes - User has no privileges to perform an action");
                resp.sendError(403);
                return;
            }
            pts = ptCon.getProjTypes();
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("listType", PlugInConsts.PAGE_PROJ_TYPES);
        parms.put("pts", pts);
        parms.put("card", card);
        parms.put("isAdmin", geMgr.getAdminUsers().contains(user.getName()));

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/project-types.vm", parms, resp.getWriter());
    }

    private void pageReports(
        HttpServletRequest req,
        HttpServletResponse resp,
        User user)
    throws RenderingException, IOException, ParseException
    {
        String[] icp = req.getParameterValues("cp");
        String[] ipt = req.getParameterValues("pt");
        String[] ibm = req.getParameterValues("bm");
        String[] iek = req.getParameterValues("ek");
        String[] iorigname = req.getParameterValues("origname");
        String[] iterritory = req.getParameterValues("territory");
        String istartDate= req.getParameter("startDate");
        String iendDate = req.getParameter("endDate");
        String allCols = req.getParameter("allCols");
        if (allCols == null || allCols.equals(""))
        {
            allCols = "";
        }

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("lang", req.getLocale().getLanguage());
        parms.put("baseUrl", getBaseUrl(req));
        parms.put("icp", Utils.strArrayToIntList(icp));
        parms.put("ipt", Utils.strArrayToIntList(ipt));
        parms.put("ibm", Utils.strArrayToIntList(ibm));
        parms.put("iek", Utils.strArrayToIntList(iek));
        parms.put("iorigname", (iorigname != null) ? Arrays.asList(iorigname) : new ArrayList<String>());
        parms.put("iterritory", (iterritory != null) ? Arrays.asList(iterritory) : new ArrayList<String>());
        parms.put("sts", Utils.getStatusMap());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdt = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        if (istartDate == null || istartDate.equals(""))
        {
            Calendar sc = Calendar.getInstance();
            sc.add(Calendar.MONTH, -3);
            startDate.setTime(sc.getTime());
            istartDate = sdf.format(sc.getTime());
        }
        else
        {
            startDate.setTime(sdf.parse(istartDate));
        }

        if (iendDate == null || iendDate.equals(""))
        {
            Calendar sc = Calendar.getInstance();
            sc.add(Calendar.MONTH, 3);
            endDate.setTime(sc.getTime());
            iendDate = sdf.format(sc.getTime());
        }
        else
        {
            endDate.setTime(sdf.parse(iendDate));
        }

        parms.put("istartDate", istartDate);
        parms.put("iendDate", iendDate);

        Map<String, UserCard> uc;
        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> eks;
        Map<Integer, GameProjectStruct> prs;
        Map<Integer, String> gpss;
        List<String> gpNames;
        Map<Integer, String> gpTerr;
        List<History> hist;
        Map<Integer, CurrentEvent> evs;
        Map<Integer, EventKind> feks;
        try
        {
            EventConnector c = new EventConnector(geMgr);
            uc = c.getUsers();
            cds = c.getCpDict();
            pts = c.getPtDict();
            bms = c.getBmDict();
            eks = c.getEkDict();
            gpss = c.getGpDict();
            gpNames = c.getUniqueGpNames();
            gpTerr = c.getTerritories();
            prs = c.getReport(icp, ipt, ibm, iorigname, iterritory, iek, istartDate, iendDate);
            evs = c.getEvDict();
            feks = c.getEks();
            int ival = c.getUserPref(user.getName());

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hist = hsMgr.getHistories(3, ival);
        }
        catch (SQLException e)
        {
            resp.sendError(500, e.getMessage());
            return;
        }

        for (History h : hist)
        {
            h.setUser(getDisplayUser(userUtil, h.getUser()));
        }

        DictWrap dwrap = new DictWrap(cds, pts, bms, eks, gpTerr, gpss);

        Set<Long> fdates = new TreeSet<Long>();
        while (startDate.compareTo(endDate) <= 0)
        {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate.getTime());
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            fdates.add(c.getTime().getTime());
            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        List<Proj> fprojs = new ArrayList<Proj>();
        for (Map.Entry<Integer, GameProjectStruct> entry : prs.entrySet())
        {
            GameProjectStruct gps = entry.getValue();

            Proj proj = new Proj(
                gps.getProjectId(),
                gps.getLocalName(),
                gps.getCompDep(),
                gps.getbModel(),
                gps.getProjectType(),
                gps.getTerritory(),
                fdates);

            for (EventStruct es : gps.getEvents())
            {
                for (Map.Entry<Long, List<EventRepr>> en : proj.getDates().entrySet())
                {
                    long s = en.getKey();
                    Calendar sc = Calendar.getInstance();
                    sc.setTimeInMillis(s);
                    sc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    sc.set(Calendar.HOUR_OF_DAY, 0);  
                    sc.set(Calendar.MINUTE, 0);
                    sc.set(Calendar.SECOND, 0);
                    sc.set(Calendar.MILLISECOND, 0);

                    List<EventRepr> events = en.getValue();

                    Calendar fc = Calendar.getInstance();
                    fc.setTimeInMillis(es.getStartDate());
                    fc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    fc.set(Calendar.HOUR_OF_DAY, 0);  
                    fc.set(Calendar.MINUTE, 0);
                    fc.set(Calendar.SECOND, 0);
                    fc.set(Calendar.MILLISECOND, 0);

                    if (es.getEndDate() == -1)
                    {
                        if (sc.compareTo(fc) == 0)
                        {
                            String color = "#ffffff";
                            if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                            {
                                color = "red";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                            {
                                color = "green";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                            {
                                color = "orange";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                            {
                                color = "yellow";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                            {
                                color = "gray";
                            }

                            EventRepr er = new EventRepr(
                                es.getEventId(),
                                es.getName(),
                                es.getKind(),
                                eks.get(es.getKind()),
                                sdf.format(es.getStartDate()),
                                "",
                                color);
                            events.add(er);
                        }
                    }
                    else
                    {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(es.getEndDate());
                        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        c.set(Calendar.HOUR_OF_DAY, 0);  
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);

                        if (fc.compareTo(sc) <= 0 && c.compareTo(sc) >= 0)
                        {
                            String color = "#ffffff";
                            if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                            {
                                color = "red";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                            {
                                color = "green";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                            {
                                color = "orange";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                            {
                                color = "yellow";
                            }
                            else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                            {
                                color = "gray";
                            }

                            EventRepr er = new EventRepr(
                                es.getEventId(),
                                es.getName(),
                                es.getKind(),
                                eks.get(es.getKind()),
                                sdf.format(es.getStartDate()),
                                sdf.format(es.getEndDate()),
                                color);
                            events.add(er);
                        }
                    }
                }
            }

            proj.computeColor();
            fprojs.add(proj);
        }

        Map<Long, Integer> colIndexes = new HashMap<Long, Integer>();
        int colIndex = 0;
        List<String> colHeaders = new ArrayList<String>();
        for (Long date : fdates)
        {
            colHeaders.add(sdf.format(new Date(date)));
            colIndexes.put(date, colIndex++);
        }

        boolean hasAdminPrm = false;
        if (geMgr.getAdminUsers().contains(user.getName()))
        {
            hasAdminPrm = true;
        }
        else
        {
            UserCard card = uc.get(user.getName());
            if (card != null && card.isAnyPermission())
            {
                hasAdminPrm = true;
            }
        }

        parms.put("hasAdminPrm", hasAdminPrm);
        parms.put("uc", uc);
        parms.put("cds", cds);
        parms.put("pts", pts);
        parms.put("bms", bms);
        parms.put("eks", eks);
        parms.put("prs", prs);
        parms.put("gpNames", gpNames);
        parms.put("gpTerr", gpTerr);
        parms.put("colHeaders", colHeaders);
        parms.put("projs", fprojs);
        parms.put("hist", new HistWrap(hist));
        parms.put("gpss", gpss);
        parms.put("evs", evs);
        parms.put("dwrap", dwrap);
        parms.put("sdt", sdt);
        parms.put("allCols", allCols);

        resp.setContentType("text/html;charset=utf-8");
        renderer.render("/templates/reports.vm", parms, resp.getWriter());
    }
}
