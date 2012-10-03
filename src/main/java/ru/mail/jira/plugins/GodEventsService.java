package ru.mail.jira.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.velocity.exception.VelocityException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.connectors.AdminConnector;
import ru.mail.jira.plugins.connectors.BModelConnector;
import ru.mail.jira.plugins.connectors.CompDepsConnector;
import ru.mail.jira.plugins.connectors.Connector;
import ru.mail.jira.plugins.connectors.EventConnector;
import ru.mail.jira.plugins.connectors.HistoryManager;
import ru.mail.jira.plugins.connectors.ProjectTypesConnector;
import ru.mail.jira.plugins.structs.CurrentEvent;
import ru.mail.jira.plugins.structs.EventKind;
import ru.mail.jira.plugins.structs.EventStruct;
import ru.mail.jira.plugins.structs.GameProject;
import ru.mail.jira.plugins.structs.GameProjectStruct;
import ru.mail.jira.plugins.structs.GodFileItem;
import ru.mail.jira.plugins.structs.HistoryDetail;
import ru.mail.jira.plugins.structs.HtmlEntity;
import ru.mail.jira.plugins.structs.UserCard;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.json.JSONException;

@Path("/geservice")
public class GodEventsService
{
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GodEventsService.class);

    /**
     * Configuration manager.
     */
    private final GodEventsMgr geMgr;

    /**
     * Group manager.
     */
    private final GroupManager groupManager;

    /**
     * Constructor.
     */
    public GodEventsService(
        GodEventsMgr geMgr,
        GroupManager groupManager)
    {
        this.geMgr = geMgr;
        this.groupManager = groupManager;
    }

    @POST
    @Path("/addbmodel")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addBModel(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addBModel - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String name = req.getParameter("name");
        String comment = req.getParameter("comment");

        if (name ==  null || name.isEmpty())
        {
            log.error("GodEventsService::addBModel - Incorrect input paramenets");
            return Response.serverError().status(500).build();
        }

        try
        {
            BModelConnector bcon = new BModelConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, bcon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::addBModel - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            bcon.addBModel(name, comment);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addBModel - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/addcompdep")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addCompDep(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addCompDep - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String name = req.getParameter("namefld");
        String comment = req.getParameter("commentfld");
        String[] mgrs = req.getParameterValues("vps");

        if (name ==  null || name.isEmpty())
        {
            log.error("GodEventsService::addCompDep - Incorrect input paramenets");
            return Response.serverError().status(500).build();
        }

        try
        {
            CompDepsConnector ccon = new CompDepsConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, ccon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::addCompDep - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            int cpId = ccon.addCompDep(name, comment);

            if (mgrs != null)
            {
                AdminConnector admCon = new AdminConnector(geMgr);
                for (String mgr : mgrs)
                {
                    admCon.addProjMgr(mgr, cpId);
                }
            }

            String baseUrl = getBaseUrl(req);
            return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=compdeps")).build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addCompDep - An SQL error occured", e);
            return Response.serverError().status(500).build();
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

    @POST
    @Path("/setprefs")
    @Produces({MediaType.APPLICATION_JSON})
    public Response setPrefs(@Context HttpServletRequest req)
    throws JSONException, Exception
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addCurrEvent - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String val = req.getParameter("val");
        int ival = Integer.parseInt(val);

        try
        {
            Connector c = new Connector(geMgr);
            c.updateUserPref(user.getName(), ival);
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addCurrEvent - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        String baseUrl = getBaseUrl(req);
        return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=prefs")).build();
    }

    @POST
    @Path("/addcurrevent")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addCurrEvent(@Context HttpServletRequest req)
    throws JSONException, Exception
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addCurrEvent - User has no privileges");
            return Response.serverError().status(403).build();
        }

        //--> input params
        String name = req.getParameter("name");
        String descr = req.getParameter("name");
        String gp = req.getParameter("gp");
        String ek = req.getParameter("ek");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");
        String[] files = req.getParameterValues("files");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long startDateLong = sdf.parse(startDate).getTime();
        long endDateLong = -1;
        if (endDate != null && !endDate.isEmpty())
        {
            endDateLong = sdf.parse(endDate).getTime();
        }

        CurrentEvent ce = new CurrentEvent();
        ce.setName(name);
        ce.setDescr(Utils.weakStr(descr));
        ce.setGp(Integer.parseInt(gp));
        ce.setEk(Integer.parseInt(ek));
        ce.setStartTime(Utils.weakStr(startTime));
        ce.setEndTime(Utils.weakStr(endTime));
        ce.setStartDate(startDateLong);
        ce.setEndDate(endDateLong);

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(gp)))
            {
                log.error("GodEventsService::addCurrEvent - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            int evId = evCon.addCurrEvent(ce, files);

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "created"));
            details.add(new HistoryDetail("name", "", ce.getName()));
            details.add(new HistoryDetail("description", "", ce.getDescr()));
            details.add(new HistoryDetail("project", "", Integer.toString(ce.getGp())));
            details.add(new HistoryDetail("eventtype", "", Integer.toString(ce.getEk())));
            details.add(new HistoryDetail("startdate", "", Long.toString(ce.getStartDate())));
            details.add(new HistoryDetail("enddate", "", Long.toString(ce.getEndDate())));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(1, evId, details, user.getName());
            details.clear();
            details.add(new HistoryDetail("action", "", "event created"));
            details.add(new HistoryDetail("action", "", "" + evId));
            hsMgr.addHistory(3, ce.getGp(), details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addCurrEvent - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        String baseUrl = getBaseUrl(req);
        return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=events")).build();
    }

    @POST
    @Path("/addeventkind")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addEventKind(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addEventKind - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String name = req.getParameter("name");
        String comment = req.getParameter("comment");
        String type = req.getParameter("type");

        if (name ==  null || name.isEmpty())
        {
            log.error("GodEventsService::addEventKind - Incorrect input paramenets");
            return Response.serverError().status(500).build();
        }

        EventConnector evCon = new EventConnector(geMgr);
        try
        {
            if (!Utils.allowVpAction(user, geMgr, evCon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::addEventKind - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            evCon.addEventKind(name, comment, Integer.parseInt(type));
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addEventKind - An SQL exception occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/addgamemgr")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addGameMgr(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addGameMgr - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String suser = req.getParameter("usr");
        int gpId;
        try
        {
            gpId = Integer.parseInt(req.getParameter("gp"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        if (suser == null || suser.isEmpty())
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::addGameMgr - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.addGameMgr(suser, gpId);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addGameMgr - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/addgproject")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addGameProject(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addGameProject - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String name = req.getParameter("name");
        String descr = req.getParameter("descr");
        String origname = req.getParameter("origname");
        String logo = req.getParameter("logotype");
        String page = req.getParameter("page");
        String developer = req.getParameter("developer");
        String territory = req.getParameter("territory");
        String bm = req.getParameter("bm");
        String pt = req.getParameter("pt");
        String cd = req.getParameter("cd");
        String sts = req.getParameter("sts");
        String[] mgrs = req.getParameterValues("vps");

        GameProject gp = new GameProject();
        gp.setLocalName(name);
        gp.setDescr(Utils.weakStr(descr));
        gp.setOrigName(Utils.weakStr(origname));
        gp.setLogo(Utils.weakStr(logo));
        gp.setPage(Utils.weakStr(page));
        gp.setDeveloper(Utils.weakStr(developer));
        gp.setCompDep(Integer.parseInt(cd));
        gp.setBModel(Integer.parseInt(bm));
        gp.setProjType(Integer.parseInt(pt));
        gp.setTerritory(Integer.parseInt(territory));
        gp.setStatus(Integer.parseInt(sts));

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowCpAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(cd)))
            {
                log.error("GodEventsService::addGameProject - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            int prId = evCon.addGameProject(gp);

            if (mgrs != null)
            {
                AdminConnector admCon = new AdminConnector(geMgr);
                for (String mgr : mgrs)
                {
                    admCon.addGameMgr(mgr, prId);
                }
            }

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "created"));
            details.add(new HistoryDetail("name", "", gp.getLocalName()));
            details.add(new HistoryDetail("description", "", gp.getDescr()));
            details.add(new HistoryDetail("developer", "", gp.getDeveloper()));
            details.add(new HistoryDetail("origName", "", gp.getOrigName()));
            details.add(new HistoryDetail("page", "", gp.getPage()));
            details.add(new HistoryDetail("logo", "", gp.getLogo()));
            details.add(new HistoryDetail("type", "", Integer.toString(gp.getProjType())));
            details.add(new HistoryDetail("model", "", Integer.toString(gp.getbModel())));
            details.add(new HistoryDetail("compdep", "", Integer.toString(gp.getCompDep())));
            details.add(new HistoryDetail("territory", "", Integer.toString(gp.getTerritory())));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(2, prId, details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addGameProject - An SQL exception occured", e);
            return Response.serverError().status(500).build();
        }

        String baseUrl = getBaseUrl(req);
        return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=gameprojects")).build();
    }

    @POST
    @Path("/addprojmgr")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addProjMgr(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addProjMgr - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String suser = req.getParameter("usr");
        int cpId;
        try
        {
            cpId = Integer.parseInt(req.getParameter("cd"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::addProjMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        if (suser == null || suser.isEmpty())
        {
            log.error("GodEventsService::addProjMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::addProjMgr - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.addProjMgr(suser, cpId);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addProjMgr - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/addprojtype")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addProjType(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addProjType - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String name = req.getParameter("name");
        String comment = req.getParameter("comment");

        if (name ==  null || name.isEmpty())
        {
            log.error("GodEventsService::addProjType - Incorrect input paramenets");
            return Response.serverError().status(500).build();
        }

        try
        {
            ProjectTypesConnector pcon = new ProjectTypesConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, pcon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::addProjType - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            pcon.addProjType(name, comment);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::addProjType - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/addvp")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addVP(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::addEventKind - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String vps = req.getParameter("vps");
        String[] vpsList;
        if (vps != null)
        {
            vpsList = vps.split("&");
        }
        else
        {
            vpsList = new String[0];
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::deleteEventKind - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.addVPs(vpsList);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteBModel - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/admcddiag")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createAdmCdDialog(@Context HttpServletRequest req)
    throws JSONException, IOException
    {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();

        Map<Integer, String> cds;
        try
        {
            cds = new Connector(geMgr).getCpDict();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
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

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("baseUrl", getBaseUrl(req));
        params.put("allUsers", allUsers);
        params.put("cds", cds);

        try
        {
            return Response.ok(new HtmlEntity(ComponentAccessor.getVelocityManager().getBody("templates/", "admcddiag.vm", params))).build();
        }
        catch (VelocityException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/admgpdiag")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createAdmGpDialog(@Context HttpServletRequest req)
    throws JSONException, IOException
    {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();

        Map<Integer, String> gps;
        try
        {
            gps = new Connector(geMgr).getGpDict();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
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

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("baseUrl", getBaseUrl(req));
        params.put("allUsers", allUsers);
        params.put("gps", gps);

        try
        {
            return Response.ok(new HtmlEntity(ComponentAccessor.getVelocityManager().getBody("templates/", "admgpdiag.vm", params))).build();
        }
        catch (VelocityException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/creategadget")
    @Produces({MediaType.APPLICATION_JSON})
    public Response createGeGadget(@Context HttpServletRequest req)
    throws JSONException
    {
        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();

        Boolean hasPerm = Boolean.FALSE;
        Boolean hasAdmPerm = Boolean.FALSE;
        if (Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            hasPerm = Boolean.TRUE;

            if (geMgr.getAdminUsers().contains(user.getName()))
            {
                hasAdmPerm = Boolean.TRUE;
            }
            else
            {
                try
                {
                    UserCard uc = new Connector(geMgr).getUserStatus(user.getName());
                    if (uc.isAnyPermission())
                    {
                        hasAdmPerm = Boolean.TRUE;
                    }
                }
                catch (SQLException e)
                {
                    log.error("A SQL error occurred", e);
                    return Response.serverError().build();
                }
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("hasPerm", hasPerm);
        params.put("hasAdmPerm", hasAdmPerm);
        params.put("baseUrl", getBaseUrl(req));

        try
        {
            return Response.ok(new HtmlEntity(ComponentAccessor.getVelocityManager().getBody("templates/", "gegadget.vm", params))).build();
        }
        catch (VelocityException e)
        {
            log.error("GodEventsService::createGeGadger - velocity error", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/deletebmodel")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteBModel(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteBModel - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteBModel - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        try
        {
            BModelConnector bcon = new BModelConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, bcon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::deleteBModel - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            bcon.deleteBModel(id);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteBModel - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deletecompdep")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteCompDep(@Context HttpServletRequest req)
    throws JSONException, URISyntaxException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteCompDep - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteCompDep - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        try
        {
            CompDepsConnector ccon = new CompDepsConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, ccon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::deleteCompDep - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            ccon.deleteCompDep(id);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteCompDep - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deletedepmgr")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteDepMgr(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteDepMgr - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String suser = req.getParameter("user");
        int cpId;
        try
        {
            cpId = Integer.parseInt(req.getParameter("cp"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        if (suser == null || suser.isEmpty())
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::deleteDepMgr - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.deleteDepMgr(suser, cpId);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteDepMgr - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deleteevent")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteEvent(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteEvent - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteEvent - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            CurrentEvent oldCe = evCon.getCurrentEvent(id);

            if (!Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), oldCe.getGp()))
            {
                log.error("GodEventsService::deleteEvent - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            evCon.deleteEvent(id);

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "deleted"));
            details.add(new HistoryDetail("name", oldCe.getName(), ""));
            details.add(new HistoryDetail("description", oldCe.getDescr(), ""));
            details.add(new HistoryDetail("project", Integer.toString(oldCe.getGp()), ""));
            details.add(new HistoryDetail("eventtype", Integer.toString(oldCe.getEk()), ""));
            details.add(new HistoryDetail("startdate", Long.toString(oldCe.getStartDate()), ""));
            details.add(new HistoryDetail("enddate", Long.toString(oldCe.getEndDate()), ""));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(1, id, details, user.getName());
            details.clear();
            details.add(new HistoryDetail("action", "", "event deleted"));
            details.add(new HistoryDetail("action", "" + id, ""));
            hsMgr.addHistory(3, oldCe.getGp(), details, user.getName());

            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteEvent - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deleteeventkind")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteEventKind(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteEventKind - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteEventKind - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        EventConnector evCon = new EventConnector(geMgr);
        try
        {
            if (!Utils.allowVpAction(user, geMgr, evCon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::deleteEventKind - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            evCon.deleteEventKind(id);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteEventKind - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deletegamemgr")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteGameMgr(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteGameMgr - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String suser = req.getParameter("user");
        int gpId;
        try
        {
            gpId = Integer.parseInt(req.getParameter("gp"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        if (suser == null || suser.isEmpty())
        {
            log.error("GodEventsService::deleteDepMgr - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::deleteGameMgr - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.deleteGameMgr(suser, gpId);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteGameMgr - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deletegameproject")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteGameProject(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteGameProject - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteGameProject - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, evCon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::deleteGameProject - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            GameProject oldGp = evCon.getGameProject(id);
            evCon.deleteGameProject(id);

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "deleted"));
            details.add(new HistoryDetail("name", oldGp.getLocalName(), ""));
            details.add(new HistoryDetail("description", oldGp.getDescr(), ""));
            details.add(new HistoryDetail("developer", oldGp.getDeveloper(), ""));
            details.add(new HistoryDetail("origName", oldGp.getOrigName(), ""));
            details.add(new HistoryDetail("page", oldGp.getPage(), ""));
            details.add(new HistoryDetail("logo", oldGp.getLogo(), ""));
            details.add(new HistoryDetail("type", Integer.toString(oldGp.getProjType()), ""));
            details.add(new HistoryDetail("model", Integer.toString(oldGp.getbModel()), ""));
            details.add(new HistoryDetail("compdep", Integer.toString(oldGp.getCompDep()), ""));
            details.add(new HistoryDetail("territory", Integer.toString(oldGp.getTerritory()), ""));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(2, id, details, user.getName());

            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteGameProject - An SQL exception occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deleteprojtype")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteProjType(@Context HttpServletRequest req)
    throws JSONException, URISyntaxException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteProjType - User has no privileges");
            return Response.serverError().status(403).build();
        }

        int id;
        try
        {
            id = Integer.parseInt(req.getParameter("id"));
        }
        catch (NumberFormatException nex)
        {
            log.error("GodEventsService::deleteProjType - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        try
        {
            ProjectTypesConnector pcon = new ProjectTypesConnector(geMgr);
            if (!Utils.allowVpAction(user, geMgr, pcon.getUserStatus(user.getName())))
            {
                log.error("GodEventsService::deleteProjType - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            pcon.deleteProjType(id);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteProjType - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @POST
    @Path("/deletevp")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteVP(@Context HttpServletRequest req)
    throws JSONException
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::deleteVP - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String suser = req.getParameter("user");
        if (suser == null || suser.isEmpty())
        {
            log.error("GodEventsService::deleteVP - Incorrect input parameters");
            return Response.serverError().status(500).build();
        }

        AdminConnector adCon = new AdminConnector(geMgr);
        try
        {
            if (!geMgr.getAdminUsers().contains(user.getName()))
            {
                log.error("GodEventsService::deleteVP - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }
            adCon.deleteVP(suser);
            return Response.ok().build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::deleteVP - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }
    }

    @GET
    @Produces ({ MediaType.APPLICATION_JSON})
    @Path("/excelreport")
    public Response exportExcel(@Context HttpServletRequest req)
    throws IOException, ParseException
    {
        if (!Utils.hasUserPliginAccess(ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser(), geMgr, groupManager))
        {
            log.error("GodEventsService::exportExcel - User has no privileges.");
            return Response.serverError().status(403).build();
        }

        String[] icp = req.getParameterValues("cp");
        String[] ipt = req.getParameterValues("pt");
        String[] ibm = req.getParameterValues("bm");
        String[] iek = req.getParameterValues("ek");
        String[] iorigname = req.getParameterValues("origname");
        String[] iterritory = req.getParameterValues("territory");
        String istartDate= req.getParameter("startDate");
        String iendDate = req.getParameter("endDate");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

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

        Map<Integer, GameProjectStruct> prs;
        Map<Integer, String> cds;
        Map<Integer, String> pts;
        Map<Integer, String> bms;
        Map<Integer, String> eks;
        Map<Integer, EventKind> feks;
        Map<Integer, String> gpTerr;
        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            prs = evCon.getReport(icp, ipt, ibm, iorigname, iterritory, iek, istartDate, iendDate);
            cds = evCon.getCpDict();
            pts = evCon.getPtDict();
            bms = evCon.getBmDict();
            eks = evCon.getEkDict();
            feks = evCon.getEks();
            gpTerr = evCon.getTerritories();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
        }

        Set<Long> fdates = new TreeSet<Long>();
        while (startDate.compareTo(endDate) <= 0)
        {
            Calendar c = Calendar.getInstance();
            c.setTime(startDate.getTime());
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            c.set(Calendar.HOUR_OF_DAY, 0);  
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            fdates.add(c.getTime().getTime());
            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        //--> create workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet mainSheet = wb.createSheet();
        mainSheet.autoSizeColumn(0);

        CreationHelper factory = wb.getCreationHelper();
        Drawing drawing = mainSheet.createDrawingPatriarch();
        //<--

        int rowCount = 0;
        int columnCount = 0;
        Map<Long, Integer> colIndexes = new HashMap<Long, Integer>();

        //--> create excel header
        HSSFRow row = mainSheet.createRow(rowCount++);
        row.setHeightInPoints(100);

        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setRotation((short) 90);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        HSSFFont font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        HSSFCell cell = row.createCell(columnCount++);
        cell.setCellValue(new HSSFRichTextString("Company Department"));
        cell.setCellStyle(style);

        cell = row.createCell(columnCount++);
        cell.setCellValue(factory.createRichTextString("Project"));
        cell.setCellStyle(style);

        cell = row.createCell(columnCount++);
        cell.setCellValue(factory.createRichTextString("Business Model"));
        cell.setCellStyle(style);

        cell = row.createCell(columnCount++);
        cell.setCellValue(factory.createRichTextString("Type"));
        cell.setCellStyle(style);

        cell = row.createCell(columnCount++);
        cell.setCellValue(factory.createRichTextString("Status"));
        cell.setCellStyle(style);

        cell = row.createCell(columnCount++);
        cell.setCellValue(factory.createRichTextString("Territory"));
        cell.setCellStyle(style);

        int headerI = columnCount;

        style = wb.createCellStyle();
        style.setDataFormat(factory.createDataFormat().getFormat("MMMM DD\\, YYYY"));
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setRotation((short) 90);
        font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        for (Long date : fdates)
        {
            int colIndex = columnCount;
            colIndexes.put(date, colIndex);
            cell = row.createCell(colIndex);
            cell.setCellValue(new Date(date));
            cell.setCellStyle(style);
            columnCount++;
        }
        //<--

        for (Map.Entry<Integer, GameProjectStruct> entry : prs.entrySet())
        {
            GameProjectStruct gps = entry.getValue();

            row = mainSheet.createRow(rowCount++);
            cell = row.createCell(0);
            cell.setCellValue(factory.createRichTextString(cds.get(gps.getCompDep())));

            cell = row.createCell(1);
            cell.setCellValue(factory.createRichTextString(gps.getLocalName()));

            cell = row.createCell(2);
            cell.setCellValue(factory.createRichTextString(bms.get(gps.getbModel())));

            cell = row.createCell(3);
            cell.setCellValue(factory.createRichTextString(pts.get(gps.getProjectType())));

            cell = row.createCell(4);
            cell.setCellValue(factory.createRichTextString(""));

            cell = row.createCell(5);
            cell.setCellValue(factory.createRichTextString(gpTerr.get(gps.getTerritory())));

            Map<Long, Integer> priorityMap = new HashMap<Long, Integer>();

            for (EventStruct es : gps.getEvents())
            {
                //--> START DATE
                Calendar fc = Calendar.getInstance();
                fc.setTimeInMillis(es.getStartDate());
                fc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                fc.set(Calendar.HOUR_OF_DAY, 0);  
                fc.set(Calendar.MINUTE, 0);
                fc.set(Calendar.SECOND, 0);
                fc.set(Calendar.MILLISECOND, 0);
                int cInx = colIndexes.get(fc.getTimeInMillis());
                cell = row.getCell(cInx);
                if (cell == null)
                {
                    cell = row.createCell(cInx);
                    cell.setCellValue(factory.createRichTextString(eks.get(es.getKind())));

                    CellStyle cellStyle = wb.createCellStyle();
                    if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.RED.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    cell.setCellStyle(cellStyle);

                    ClientAnchor anchor = factory.createClientAnchor();
                    anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
                    anchor.setCol1(cell.getColumnIndex());
                    anchor.setCol2(cell.getColumnIndex() + 5);
                    anchor.setRow1(row.getRowNum());
                    anchor.setRow2(row.getRowNum() + 10);
                    Comment comment = drawing.createCellComment(anchor);

                    StringBuilder sb = new StringBuilder();
                    sb.append(sdf.format(es.getStartDate()));
                    if (es.getEndDate() > 0)
                    {
                        sb.append(" - ").append(sdf.format(es.getEndDate()));
                    }
                    sb.append(": ").append(es.getName());
                    comment.setString(factory.createRichTextString(sb.toString()));
                    cell.setCellComment(comment);
                }
                else
                {
                    String val = cell.getRichStringCellValue().getString();
                    cell.setCellValue(factory.createRichTextString(val + "," + eks.get(es.getKind())));

                    CellStyle cellStyle = wb.createCellStyle();
                    if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                    {
                        cellStyle.setFillForegroundColor(IndexedColors.RED.index);
                        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                        priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                    {
                        if (priorityMap.containsKey(es.getStartDate()))
                        {
                            int pr = priorityMap.get(es.getStartDate());
                            if (pr != PlugInConsts.RED)
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                            cell.setCellStyle(cellStyle);
                        }
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                    {
                        if (priorityMap.containsKey(es.getStartDate()))
                        {
                            int pr = priorityMap.get(es.getStartDate());
                            if (pr != PlugInConsts.GREEN || pr != PlugInConsts.RED)
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                            cell.setCellStyle(cellStyle);
                        }
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                    {
                        if (priorityMap.containsKey(es.getStartDate()))
                        {
                            int pr = priorityMap.get(es.getStartDate());
                            if (pr != PlugInConsts.GREEN || pr != PlugInConsts.RED || pr != PlugInConsts.ORANGE)
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                            cell.setCellStyle(cellStyle);
                        }
                    }
                    else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                    {
                        if (!priorityMap.containsKey(es.getStartDate()))
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                            cell.setCellStyle(cellStyle);
                        }
                    }

                    Comment comment = cell.getCellComment();
                    StringBuilder sb = new StringBuilder(comment.getString().getString());
                    sb.append("\n\n");
                    sb.append(sdf.format(es.getStartDate()));
                    if (es.getEndDate() > 0)
                    {
                        sb.append(" - ").append(sdf.format(es.getEndDate()));
                    }
                    sb.append(": ").append(es.getName());
                    comment.setString(factory.createRichTextString(sb.toString()));
                    cell.setCellComment(comment);
                }

                //--> END DATE
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(es.getEndDate());
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                c.set(Calendar.HOUR_OF_DAY, 0);  
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                if (colIndexes.containsKey(c.getTimeInMillis()))
                {
                    cInx = colIndexes.get(c.getTimeInMillis());
                    cell = row.getCell(cInx);
                    if (cell == null)
                    {
                        cell = row.createCell(cInx);
                        cell.setCellValue(factory.createRichTextString(eks.get(es.getKind())));

                        CellStyle cellStyle = wb.createCellStyle();
                        if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.RED.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                        }
                        cell.setCellStyle(cellStyle);

                        ClientAnchor anchor = factory.createClientAnchor();
                        anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
                        anchor.setCol1(cell.getColumnIndex());
                        anchor.setCol2(cell.getColumnIndex() + 5);
                        anchor.setRow1(row.getRowNum());
                        anchor.setRow2(row.getRowNum() + 10);

                        Comment comment = drawing.createCellComment(anchor);

                        StringBuilder sb = new StringBuilder();
                        sb.append(sdf.format(es.getStartDate())).append(" - ").append(sdf.format(es.getEndDate()));
                        sb.append(": ").append(es.getName());
                        comment.setString(factory.createRichTextString(sb.toString()));
                        cell.setCellComment(comment);
                    }
                    else
                    {
                        if (es.getStartDate() == es.getEndDate())
                        {
                            continue;
                        }

                        String val = cell.getRichStringCellValue().getString();
                        cell.setCellValue(factory.createRichTextString(val + "," + eks.get(es.getKind())));

                        CellStyle cellStyle = wb.createCellStyle();
                        if (feks.get(es.getKind()).getEtype() == PlugInConsts.RED)
                        {
                            cellStyle.setFillForegroundColor(IndexedColors.RED.index);
                            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                            priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                            cell.setCellStyle(cellStyle);
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GREEN)
                        {
                            if (priorityMap.containsKey(es.getStartDate()))
                            {
                                int pr = priorityMap.get(es.getStartDate());
                                if (pr != PlugInConsts.RED)
                                {
                                    cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                                    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                    priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                    cell.setCellStyle(cellStyle);
                                }
                            }
                            else
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.ORANGE)
                        {
                            if (priorityMap.containsKey(es.getStartDate()))
                            {
                                int pr = priorityMap.get(es.getStartDate());
                                if (pr != PlugInConsts.GREEN || pr != PlugInConsts.RED)
                                {
                                    cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                                    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                    priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                    cell.setCellStyle(cellStyle);
                                }
                            }
                            else
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.ORANGE.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.YELLOW)
                        {
                            if (priorityMap.containsKey(es.getStartDate()))
                            {
                                int pr = priorityMap.get(es.getStartDate());
                                if (pr != PlugInConsts.GREEN || pr != PlugInConsts.RED || pr != PlugInConsts.ORANGE)
                                {
                                    cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                                    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                    priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                    cell.setCellStyle(cellStyle);
                                }
                            }
                            else
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }
                        else if (feks.get(es.getKind()).getEtype() == PlugInConsts.GRAY)
                        {
                            if (!priorityMap.containsKey(es.getStartDate()))
                            {
                                cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                                cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                priorityMap.put(es.getStartDate(), feks.get(es.getKind()).getEtype());
                                cell.setCellStyle(cellStyle);
                            }
                        }

                        Comment comment = cell.getCellComment();
                        StringBuilder sb = new StringBuilder(comment.getString().getString());
                        sb.append("\n\n");
                        sb.append(sdf.format(es.getStartDate())).append(" - ").append(sdf.format(es.getEndDate()));
                        sb.append(": ").append(es.getName());
                        comment.setString(factory.createRichTextString(sb.toString()));
                        cell.setCellComment(comment);
                    }
                }
            }
        }

        mainSheet.createFreezePane(headerI, prs.size());
        //--> main settings
        String autoFilter = "A1:F".concat(Integer.toString(prs.size()));
        mainSheet.setAutoFilter(CellRangeAddress.valueOf(autoFilter));
        for (int i = 0; i < headerI; i++)
        {
            mainSheet.autoSizeColumn(i);
        }
        //<--
        mainSheet.getRow(0).getCell(headerI - 1).setAsActiveCell();
        wb.setActiveSheet(0);
        mainSheet.showInPane((short)0, (short)0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.close();

        return Response.ok(os.toByteArray())
            .header("Content-Disposition", "attachment; filename=events-report.xls")
            .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
            .build();
    }

    @GET
    @Produces ({ MediaType.APPLICATION_JSON})
    @Path("/exportfile")
    public Response exportFile(@Context HttpServletRequest req)
    throws JSONException, IOException
    {
        if (!Utils.hasUserPliginAccess(ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser(), geMgr, groupManager))
        {
            log.error("GodEventsService::exportFile - User has no privileges.");
            return Response.serverError().status(403).build();
        }

        String fileIdStr = req.getParameter("fileId");

        int fileId;
        try
        {
            fileId = Integer.parseInt(fileIdStr);
        }
        catch(NumberFormatException nex)
        {
            log.error("GodEventsService::exportFile - ", nex);
            return Response.status(500).build();
        }

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            GodFileItem fi = evCon.getFile(fileId);

            if (fi == null)
            {
                log.error("GodEventsService::exportFile - File is not found.");
                return Response.status(404).build();
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(fi.getData());
            os.close();

            return Response.ok(os.toByteArray())
                .header("Content-Disposition", String.format("attachment; filename=%s", fi.getName()))
                .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .build();
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/updatecurrevent")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateCurrEvent(@Context HttpServletRequest req)
    throws JSONException, Exception
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::updateCurrEvent - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String descr = req.getParameter("descr");
        String gp = req.getParameter("gp");
        String ek = req.getParameter("ek");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");
        String[] files = req.getParameterValues("files");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long startDateLong = sdf.parse(startDate).getTime();
        long endDateLong = -1;
        if (endDate != null && !endDate.isEmpty())
        {
            endDateLong = sdf.parse(endDate).getTime();
        }

        int iid = Integer.parseInt(id);

        CurrentEvent ce = new CurrentEvent();
        ce.setId(iid);
        ce.setName(name);
        ce.setDescr(Utils.weakStr(descr));
        ce.setGp(Integer.parseInt(gp));
        ce.setEk(Integer.parseInt(ek));
        ce.setStartTime(Utils.weakStr(startTime));
        ce.setEndTime(Utils.weakStr(endTime));
        ce.setStartDate(startDateLong);
        ce.setEndDate(endDateLong);

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(gp)))
            {
                log.error("GodEventsService::updateCurrEvent - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            CurrentEvent oldCe = evCon.getCurrentEvent(iid);

            evCon.updateCurrEvent(ce, files);

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();

            if (!ce.getName().equals(oldCe.getName()))
            {
                details.add(new HistoryDetail("name", oldCe.getName(), ce.getName()));
            }
            if (!ce.getDescr().equals(oldCe.getDescr()))
            {
                details.add(new HistoryDetail("description", oldCe.getDescr(), ce.getDescr()));
            }
            if (ce.getGp() != oldCe.getGp())
            {
                details.add(new HistoryDetail("project", Integer.toString(oldCe.getGp()), Integer.toString(ce.getGp())));
            }
            if (ce.getEk() != oldCe.getEk())
            {
                details.add(new HistoryDetail("eventtype", Integer.toString(oldCe.getEk()), Integer.toString(ce.getEk())));
            }
            if (ce.getStartDate() != oldCe.getStartDate())
            {
                details.add(new HistoryDetail("startdate", Long.toString(oldCe.getStartDate()), Long.toString(ce.getStartDate())));
            }
            if (ce.getEndDate() != oldCe.getEndDate())
            {
                details.add(new HistoryDetail("enddate", Long.toString(oldCe.getEndDate()), Long.toString(ce.getEndDate())));
            }

            details.add(new HistoryDetail("action", "", "updated"));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(1, iid, details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::updateCurrEvent - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        String baseUrl = getBaseUrl(req);
        return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=events")).build();
    }

    @POST
    @Path("/updatecurreventdlg")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateCurrEventDlg(@Context HttpServletRequest req)
    throws JSONException, Exception
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::updateCurrEvent - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String descr = req.getParameter("descr");
        String gp = req.getParameter("gp");
        String ek = req.getParameter("ek");
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");
        String[] files = req.getParameterValues("files");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long startDateLong = sdf.parse(startDate).getTime();
        long endDateLong = -1;
        if (endDate != null && !endDate.isEmpty())
        {
            endDateLong = sdf.parse(endDate).getTime();
        }

        int iid = Integer.parseInt(id);

        CurrentEvent ce = new CurrentEvent();
        ce.setId(iid);
        ce.setName(name);
        ce.setDescr(Utils.weakStr(descr));
        ce.setGp(Integer.parseInt(gp));
        ce.setEk(Integer.parseInt(ek));
        ce.setStartTime(Utils.weakStr(startTime));
        ce.setEndTime(Utils.weakStr(endTime));
        ce.setStartDate(startDateLong);
        ce.setEndDate(endDateLong);

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowGPAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(gp)))
            {
                log.error("GodEventsService::updateCurrEvent - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            CurrentEvent oldCe = evCon.getCurrentEvent(iid);

            evCon.updateCurrEvent(ce, files);

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();

            if (!ce.getName().equals(oldCe.getName()))
            {
                details.add(new HistoryDetail("name", oldCe.getName(), ce.getName()));
            }
            if (!ce.getDescr().equals(oldCe.getDescr()))
            {
                details.add(new HistoryDetail("description", oldCe.getDescr(), ce.getDescr()));
            }
            if (ce.getGp() != oldCe.getGp())
            {
                details.add(new HistoryDetail("project", Integer.toString(oldCe.getGp()), Integer.toString(ce.getGp())));
            }
            if (ce.getEk() != oldCe.getEk())
            {
                details.add(new HistoryDetail("eventtype", Integer.toString(oldCe.getEk()), Integer.toString(ce.getEk())));
            }
            if (ce.getStartDate() != oldCe.getStartDate())
            {
                details.add(new HistoryDetail("startdate", Long.toString(oldCe.getStartDate()), Long.toString(ce.getStartDate())));
            }
            if (ce.getEndDate() != oldCe.getEndDate())
            {
                details.add(new HistoryDetail("enddate", Long.toString(oldCe.getEndDate()), Long.toString(ce.getEndDate())));
            }

            details.add(new HistoryDetail("action", "", "updated"));
            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(1, iid, details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::updateCurrEvent - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/updategproject")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateGameProject(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::updateGameProject - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String descr = req.getParameter("descr");
        String origname = req.getParameter("origname");
        String logo = req.getParameter("logotype");
        String page = req.getParameter("page");
        String developer = req.getParameter("developer");
        String territory = req.getParameter("territory");
        String bm = req.getParameter("bm");
        String pt = req.getParameter("pt");
        String cd = req.getParameter("cd");
        String sts = req.getParameter("sts");
        String[] mgrs = req.getParameterValues("vps");

        int iid = Integer.parseInt(id);

        GameProject gp = new GameProject();
        gp.setId(iid);
        gp.setDescr(Utils.weakStr(descr));
        gp.setLocalName(Utils.weakStr(name));
        gp.setOrigName(Utils.weakStr(origname));
        gp.setLogo(Utils.weakStr(logo));
        gp.setPage(Utils.weakStr(page));
        gp.setDeveloper(Utils.weakStr(developer));
        gp.setCompDep(Integer.parseInt(cd));
        gp.setBModel(Integer.parseInt(bm));
        gp.setProjType(Integer.parseInt(pt));
        gp.setTerritory(Integer.parseInt(territory));
        gp.setStatus(Integer.parseInt(sts));

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowCpAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(cd)))
            {
                log.error("GodEventsService::updateGameProject - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            GameProject oldGp = evCon.getGameProject(iid);

            evCon.updateGameProject(gp);

            if (mgrs != null)
            {
                AdminConnector admCon = new AdminConnector(geMgr);
                admCon.deleteGameMgrAll(iid);
                for (String mgr : mgrs)
                {
                    admCon.addGameMgr(mgr, iid);
                }
            }

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "updated"));

            if (!oldGp.getLocalName().equals(gp.getLocalName()))
            {
                details.add(new HistoryDetail("name", oldGp.getLocalName(), gp.getLocalName()));
            }
            if (!oldGp.getDescr().equals(gp.getDescr()))
            {
                details.add(new HistoryDetail("description", oldGp.getDescr(), gp.getDescr()));
            }
            if (!oldGp.getDeveloper().equals(gp.getDeveloper()))
            {
                details.add(new HistoryDetail("developer", oldGp.getDeveloper(), gp.getDeveloper()));
            }
            if (!oldGp.getOrigName().equals(gp.getOrigName()))
            {
                details.add(new HistoryDetail("origName", oldGp.getOrigName(), gp.getOrigName()));
            }
            if (!oldGp.getPage().equals(gp.getPage()))
            {
                details.add(new HistoryDetail("page", oldGp.getPage(), gp.getPage()));
            }
            if (!oldGp.getLogo().equals(gp.getLogo()))
            {
                details.add(new HistoryDetail("logo", oldGp.getLogo(), gp.getLogo()));
            }
            if (oldGp.getProjType() != gp.getProjType())
            {
                details.add(new HistoryDetail("type", Integer.toString(oldGp.getProjType()), Integer.toString(gp.getProjType())));
            }
            if (oldGp.getbModel() != gp.getbModel())
            {
                details.add(new HistoryDetail("model", Integer.toString(oldGp.getbModel()), Integer.toString(gp.getbModel())));
            }
            if (oldGp.getCompDep() != gp.getCompDep())
            {
                details.add(new HistoryDetail("compdep", Integer.toString(oldGp.getCompDep()), Integer.toString(gp.getCompDep())));
            }
            if (oldGp.getTerritory() != gp.getTerritory())
            {
                details.add(new HistoryDetail("territory", Integer.toString(oldGp.getTerritory()), Integer.toString(gp.getTerritory())));
            }

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(2, iid, details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::updateGameProject - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        String baseUrl = getBaseUrl(req);
        return Response.seeOther(URI.create(baseUrl + "/plugins/servlet/godevents/viewer?page=gameprojects")).build();
    }

    @POST
    @Path("/updategprojectdlg")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateGameProjectDlg(@Context HttpServletRequest req)
    {
        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (!Utils.hasUserPliginAccess(user, geMgr, groupManager))
        {
            log.error("GodEventsService::updateGameProject - User has no privileges");
            return Response.serverError().status(403).build();
        }

        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String descr = req.getParameter("descr");
        String origname = req.getParameter("origname");
        String logo = req.getParameter("logotype");
        String page = req.getParameter("page");
        String developer = req.getParameter("developer");
        String territory = req.getParameter("territory");
        String bm = req.getParameter("bm");
        String pt = req.getParameter("pt");
        String cd = req.getParameter("cd");
        String sts = req.getParameter("sts");
        String[] mgrs = req.getParameterValues("vps");

        int iid = Integer.parseInt(id);

        GameProject gp = new GameProject();
        gp.setId(iid);
        gp.setDescr(Utils.weakStr(descr));
        gp.setLocalName(Utils.weakStr(name));
        gp.setOrigName(Utils.weakStr(origname));
        gp.setLogo(Utils.weakStr(logo));
        gp.setPage(Utils.weakStr(page));
        gp.setDeveloper(developer);
        gp.setCompDep(Integer.parseInt(cd));
        gp.setBModel(Integer.parseInt(bm));
        gp.setProjType(Integer.parseInt(pt));
        gp.setTerritory(Integer.parseInt(territory));
        gp.setStatus(Integer.parseInt(sts));

        try
        {
            EventConnector evCon = new EventConnector(geMgr);
            if (!Utils.allowCpAction(user, geMgr, evCon.getUserStatus(user.getName()), Integer.parseInt(cd)))
            {
                log.error("GodEventsService::updateGameProject - User has no privileges to perform an action");
                return Response.serverError().status(403).build();
            }

            GameProject oldGp = evCon.getGameProject(iid);

            evCon.updateGameProject(gp);

            if (mgrs != null)
            {
                AdminConnector admCon = new AdminConnector(geMgr);
                admCon.deleteGameMgrAll(iid);
                for (String mgr : mgrs)
                {
                    admCon.addGameMgr(mgr, iid);
                }
            }

            List<HistoryDetail> details = new ArrayList<HistoryDetail>();
            details.add(new HistoryDetail("action", "", "updated"));

            if (!oldGp.getLocalName().equals(gp.getLocalName()))
            {
                details.add(new HistoryDetail("name", oldGp.getLocalName(), gp.getLocalName()));
            }
            if (!oldGp.getDescr().equals(gp.getDescr()))
            {
                details.add(new HistoryDetail("description", oldGp.getDescr(), gp.getDescr()));
            }
            if (!oldGp.getDeveloper().equals(gp.getDeveloper()))
            {
                details.add(new HistoryDetail("developer", oldGp.getDeveloper(), gp.getDeveloper()));
            }
            if (!oldGp.getOrigName().equals(gp.getOrigName()))
            {
                details.add(new HistoryDetail("origName", oldGp.getOrigName(), gp.getOrigName()));
            }
            if (!oldGp.getPage().equals(gp.getPage()))
            {
                details.add(new HistoryDetail("page", oldGp.getPage(), gp.getPage()));
            }
            if (!oldGp.getLogo().equals(gp.getLogo()))
            {
                details.add(new HistoryDetail("logo", oldGp.getLogo(), gp.getLogo()));
            }
            if (oldGp.getProjType() != gp.getProjType())
            {
                details.add(new HistoryDetail("type", Integer.toString(oldGp.getProjType()), Integer.toString(gp.getProjType())));
            }
            if (oldGp.getbModel() != gp.getbModel())
            {
                details.add(new HistoryDetail("model", Integer.toString(oldGp.getbModel()), Integer.toString(gp.getbModel())));
            }
            if (oldGp.getCompDep() != gp.getCompDep())
            {
                details.add(new HistoryDetail("compdep", Integer.toString(oldGp.getCompDep()), Integer.toString(gp.getCompDep())));
            }
            if (oldGp.getTerritory() != gp.getTerritory())
            {
                details.add(new HistoryDetail("territory", Integer.toString(oldGp.getTerritory()), Integer.toString(gp.getTerritory())));
            }

            HistoryManager hsMgr = new HistoryManager(geMgr);
            hsMgr.addHistory(2, iid, details, user.getName());
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::updateGameProject - An SQL error occured", e);
            return Response.serverError().status(500).build();
        }

        return Response.ok().build();
    }

    @POST
    @Produces ({ MediaType.APPLICATION_JSON})
    @Path("/uploadfile")
    public Response uploadFile(@Context HttpServletRequest req)
    throws org.json.JSONException, FileUploadException
    {
        FileItemFactory factory = new DiskFileItemFactory();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // Parse the request
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);

        JSONObject result = new JSONObject();

        EventConnector evCon = new EventConnector(geMgr);
        try
        {
            int fId = -1;
            for (FileItem fi : items)
            {
                fId = evCon.saveFile(fi.getName(), "", fi.get());

                Map<String, String> fileProps = new HashMap<String, String>();
                fileProps.put("fId", Integer.toString(fId));
                fileProps.put("fName", fi.getName());
                result.put("fileProps", fileProps);
            }
        }
        catch (SQLException e)
        {
            log.error("GodEventsService::getAdmCdDialog - velocity error", e);
            return Response.serverError().build();
        }

        return Response.ok(result.toString()).type(MediaType.TEXT_PLAIN).build();
    }
}
