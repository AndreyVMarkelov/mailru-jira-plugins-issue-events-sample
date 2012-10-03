package ru.mail.jira.plugins;

/**
 * SQL queries.
 * 
 * @author Andrey Markelov
 */
public interface SqlQueries
{
    /**
     * 
     */
    String ADD_BUSYNESS_MODEL = "INSERT INTO BUSYNESS_MODEL (BM_ID, NAME, COMMENT) VALUES (DEFAULT, ?, ?)";

    /**
     * 
     */
    String ADD_COMPANY_DEPARTMENT = "INSERT INTO COMPANY_DEPARTMENT (CD_ID, NAME, COMMENT) VALUES (DEFAULT, ?, ?)";

    /**
     * 
     */
    String ADD_DEP_MGR = "INSERT INTO SYSTEM_ROLES (ID, USERNAME, COMP_DEP_ID, GP_ID, VP) VALUES (DEFAULT, ?, ?, NULL, NULL)";

    /**
     * SQL statements adds new event.
     */
    String ADD_EVENT = "INSERT INTO EVENT (EVENT_ID, TITLE, NAME, DESCR, GP_ID, EK_ID, STARTDATE, STARTTIME, ENDDATE, ENDTIME) VALUES (DEFAULT, '', ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * 
     */
    String ADD_EVENT_HISTORY = "INSERT INTO HISTORY (ID, TYPE, PROJECT_ID, UTIME, USER) VALUES (DEFAULT, ?, ?, DEFAULT, ?);";

    /**
     * Insert kind of event.
     */
    String ADD_EVENT_KINDS = "INSERT INTO EVENT_KINDS (EV_ID, NAME, COMMENT, EVENT_TYPE) VALUES (DEFAULT, ?, ?, ?)";

    /**
     * 
     */
    String ADD_FILE = "INSERT INTO FILE (FILE_ID, NAME, COMMENT, CONTENT, EVENT_ID) VALUES (DEFAULT, ?, ?, ?, NULL)";

    /**
     * 
     */
    String ADD_GAME_MGR = "INSERT INTO SYSTEM_ROLES (ID, USERNAME, COMP_DEP_ID, GP_ID, VP) VALUES (DEFAULT, ?, NULL, ?, NULL)";

    /**
     * 
     */
    String ADD_GAME_PROJECT = "INSERT INTO GAME_PROJECT(GP_ID, LOCAL_NAME, DESCR, ORIG_NAME, PR_TYPE, LOGO, PAGE, DEVELOPER, COMPANY_DEPT, BMODEL, TERRITORY, STATUS) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * 
     */
    String ADD_HISTORY_DETAIL = "INSERT INTO HISTORY_DETAIL (ID, HID, FIELD, OLD, NEW) VALUES (DEFAULT, ?, ?, ?, ?)";

    /**
     * 
     */
    String ADD_PROJECT_TYPE = "INSERT INTO PROJECT_TYPE (PT_ID, NAME, COMMENT) VALUES (DEFAULT, ?, ?)";

    /**
     * 
     */
    String ADD_VP = "INSERT INTO SYSTEM_ROLES (ID, USERNAME, COMP_DEP_ID, GP_ID, VP) VALUES (DEFAULT, ?, NULL, NULL, 1)";

    /**
     * Get all users.
     */
    String ALL_USERS = "SELECT USERNAME, COMP_DEP_ID, GP_ID, VP FROM SYSTEM_ROLES ORDER BY USERNAME";

    /**
     * Get company department list.
     */
    String BUSYNESS_MODEL_LIST = "SELECT BM_ID, NAME, COMMENT FROM BUSYNESS_MODEL WHERE DELETED = 0 ORDER BY NAME";

    /**
     * Get company department all list.
     */
    String BUSYNESS_MODEL_LIST_ALL = "SELECT BM_ID, NAME, COMMENT FROM BUSYNESS_MODEL ORDER BY NAME";

    /**
     * Get company department list.
     */
    String COMPANY_DEPARTMENT_LIST = "SELECT CD_ID, NAME, COMMENT FROM COMPANY_DEPARTMENT WHERE DELETED = 0 ORDER BY NAME";

    /**
     * Get company department all list.
     */
    String COMPANY_DEPARTMENT_LIST_ALL = "SELECT CD_ID, NAME, COMMENT FROM COMPANY_DEPARTMENT ORDER BY NAME";

    /**
     * Disable the business model.
     */
    String DELETE_BUSYNESS_MODEL = "UPDATE BUSYNESS_MODEL SET DELETED = 1 WHERE BM_ID = ?";

    /**
     * Disable the company department.
     */
    String DELETE_COMPANY_DEPARTMENT = "UPDATE COMPANY_DEPARTMENT SET DELETED = DELETED + 1 WHERE CD_ID = ?";

    /**
     * 
     */
    String DELETE_DEP_MGR = "DELETE FROM SYSTEM_ROLES WHERE USERNAME = ? AND (COMP_DEP_ID IS NOT NULL AND COMP_DEP_ID = ?)";

    /**
     * 
     */
    String DELETE_DEP_MGR_ALL = "DELETE FROM SYSTEM_ROLES WHERE COMP_DEP_ID = ?";

    /**
     * 
     */
    String DELETE_EVENT = "UPDATE EVENT SET DELETED = DELETED + 1 WHERE EVENT_ID = ?";

    /**
     * Disable the kind of event.
     */
    String DELETE_EVENT_KINDS = "UPDATE EVENT_KINDS SET DELETED = DELETED + 1 WHERE EV_ID = ?";

    /**
     * 
     */
    String DELETE_FILE = "DELETE FROM FILE WHERE FILE_ID = ?";

    /**
     * 
     */
    String DELETE_GAME_MGR = "DELETE FROM SYSTEM_ROLES WHERE USERNAME = ? AND (GP_ID IS NOT NULL AND GP_ID = ?)";

    /**
     * 
     */
    String DELETE_GAME_MGR_ALL = "DELETE FROM SYSTEM_ROLES WHERE GP_ID = ?";

    /**
     * 
     */
    String DELETE_GAME_PROJECT = "UPDATE GAME_PROJECT SET DELETED = DELETED + 1 WHERE GP_ID = ?";

    /**
     * Disable project type.
     */
    String DELETE_PROJECT_TYPE = "UPDATE PROJECT_TYPE SET DELETED = DELETED + 1 WHERE PT_ID = ?";

    /**
     * 
     */
    String DELETE_VP = "DELETE FROM SYSTEM_ROLES WHERE USERNAME = ? AND (VP IS NOT NULL AND VP > 0)";

    /**
     * Get company department list.
     */
    String EVENT_KINDS_LIST = "SELECT EV_ID, NAME, COMMENT, EVENT_TYPE FROM EVENT_KINDS WHERE DELETED = 0 ORDER BY NAME";

    /**
     * Get company department all list.
     */
    String EVENT_KINDS_LIST_ALL = "SELECT EV_ID, NAME, COMMENT, EVENT_TYPE FROM EVENT_KINDS ORDER BY NAME";

    /**
     * SQL statement selects list of events.
     */
    String EVENT_LIST = "SELECT EVENT_ID, NAME, DESCR, GP_ID, EK_ID, STARTDATE, STARTTIME, ENDDATE, ENDTIME FROM EVENT ORDER BY NAME";

    /**
     * SQL statement selects list of events.
     */
    String EVENT_LIST_EXIST = "SELECT EVENT_ID, NAME, DESCR, GP_ID, EK_ID, STARTDATE, STARTTIME, ENDDATE, ENDTIME FROM EVENT WHERE DELETED = 0 ORDER BY NAME";

    /**
     * 
     */
    String GAME_PROJECT_LIST = "SELECT GP_ID, LOCAL_NAME, ORIG_NAME, PR_TYPE, LOGO, PAGE, DEVELOPER, COMPANY_DEPT, BMODEL, TERRITORY, STATUS FROM GAME_PROJECT";

    /**
     * 
     */
    String GAME_PROJECT_LIST_EXIST = "SELECT GP_ID, LOCAL_NAME, ORIG_NAME, PR_TYPE, LOGO, PAGE, DEVELOPER, COMPANY_DEPT, BMODEL, TERRITORY, STATUS FROM GAME_PROJECT WHERE DELETED = 0";

    /**
     * Get game projects map.
     */
    String GAME_PROJECT_MAP = "SELECT GP_ID, LOCAL_NAME FROM GAME_PROJECT ORDER BY LOCAL_NAME";

    /**
     * 
     */
    String GET_CURRENT_EVENT = "SELECT EVENT_ID, NAME, DESCR, GP_ID, EK_ID, STARTDATE, STARTTIME, ENDDATE, ENDTIME FROM EVENT WHERE EVENT_ID = ?";

    /**
     * 
     */
    String GET_EVENT_FILES = "SELECT FILE_ID, NAME, COMMENT FROM FILE WHERE EVENT_ID = ?";

    /**
     * 
     */
    String GET_GAME_PROJECT = "SELECT GP_ID, LOCAL_NAME, DESCR, ORIG_NAME, PR_TYPE, LOGO, PAGE, DEVELOPER, COMPANY_DEPT, BMODEL, TERRITORY, STATUS FROM GAME_PROJECT WHERE GP_ID = ?";

    /**
     * SQL statement selects the saved file.
     */
    String GET_SAVED_FILE = "SELECT CONTENT, NAME FROM FILE WHERE FILE_ID = ?";

    String HISTORY_ALL_LIST = "SELECT ID, TYPE, PROJECT_ID, UTIME, USER FROM HISTORY WHERE TYPE IN (1, 2) AND UTIME >= DATE_SUB(NOW(), INTERVAL ? WEEK)";

    String HISTORY_DETAIL_LIST = "SELECT ID, HID, FIELD, OLD, NEW FROM HISTORY_DETAIL WHERE HID = ?";

    String HISTORY_EVENT_LIST = "SELECT ID, TYPE, PROJECT_ID, UTIME, USER FROM HISTORY WHERE TYPE = 1 AND UTIME >= DATE_SUB(NOW(), INTERVAL ? WEEK)";

    String HISTORY_PROJECT_LIST = "SELECT ID, TYPE, PROJECT_ID, UTIME, USER FROM HISTORY WHERE TYPE IN (2, 3) AND UTIME >= DATE_SUB(NOW(), INTERVAL ? WEEK)";

    /**
     * Get project types list.
     */
    String PROJECT_TYPE_LIST = "SELECT PT_ID, NAME, COMMENT FROM PROJECT_TYPE WHERE DELETED = 0 ORDER BY NAME";

    /**
     * Get project types all list.
     */
    String PROJECT_TYPE_LIST_ALL = "SELECT PT_ID, NAME, COMMENT FROM PROJECT_TYPE ORDER BY NAME";

    /**
     * 
     */
    String REPORT_QUERY = "SELECT A.EVENT_ID, A.NAME, A.DESCR, A.GP_ID, A.EK_ID, A.STARTDATE, A.STARTTIME, A.ENDDATE, A.ENDTIME, B.LOCAL_NAME, B.DESCR, B.ORIG_NAME, B.PR_TYPE, B.LOGO, B.PAGE, B.DEVELOPER, B.COMPANY_DEPT, B.BMODEL, B.TERRITORY FROM EVENT A INNER JOIN GAME_PROJECT B ON (A.GP_ID = B.GP_ID)";

    /**
     * Get territory all list.
     */
    String TERRITORY_LIST_ALL = "SELECT ID, NAME FROM TERRITORY ORDER BY NAME";

    /**
     * Selects unique project names.
     */
    String UNIQUE_PROJ_NAMES = "SELECT DISTINCT LOCAL_NAME FROM GAME_PROJECT";

    /**
     * Selects unique project territories.
     */
    String UNIQUE_PROJ_TERRITORY = "SELECT DISTINCT TERRITORY FROM GAME_PROJECT";

    /**
     * 
     */
    String UPDATE_CURRENT_EVENT = "UPDATE EVENT SET NAME = ?, GP_ID = ?, EK_ID = ?, DESCR = ?, STARTDATE = ?, STARTTIME = ?, ENDDATE = ?, ENDTIME = ? WHERE EVENT_ID = ?";

    /**
     * 
     */
    String UPDATE_FILE = "UPDATE FILE SET EVENT_ID = ? WHERE FILE_ID = ?";

    /**
     * 
     */
    String UPDATE_GAME_PROJECT = "UPDATE GAME_PROJECT SET LOCAL_NAME = ?, DESCR = ?, ORIG_NAME = ?, PR_TYPE = ?, LOGO = ?, PAGE = ?, DEVELOPER = ?, COMPANY_DEPT = ?, BMODEL = ?, TERRITORY = ?, STATUS = ? WHERE GP_ID = ?";

    String USER_PREF_ADD = "INSERT INTO USER_SETTINGS (USER, PARAM, VALUE) VALUES (?, 'history', ?)";

    String USER_PREF_FIND = "SELECT USER, PARAM, VALUE FROM USER_SETTINGS WHERE USER = ? AND PARAM = 'history'";

    String USER_PREF_UPDATE = "UPDATE USER_SETTINGS SET VALUE = ? WHERE USER = ? AND PARAM = 'history'";

    /**
     * Get user permission information.
     */
    String USER_ROLES_INFO = "SELECT GP_ID, COMP_DEP_ID, VP FROM SYSTEM_ROLES WHERE USERNAME = ?";
}
