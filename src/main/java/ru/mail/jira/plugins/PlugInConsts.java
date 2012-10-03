package ru.mail.jira.plugins;

/**
 * PlugIn constants.
 * 
 * @author Andrey Markelov
 */
public interface PlugInConsts
{
    /*
     * User permissions.
     */
    int NO_RIGHTS = 0;
    int GAME_PROJECT_MGR = 1;
    int DIVISION_MGR = 2;
    int VP = 3;
    int ADMIN = 4;

    /*
     * Page types.
     */
    int PAGE_EVENTS = 1;
    int PAGE_GAME_PROJ = 2;
    int PAGE_COMP_DEPS = 3;
    int PAGE_PROJ_TYPES = 4;
    int PAGE_BMODEL = 5;
    int PAGE_EVENT = 6;
    int PAGE_ADMIN = 7;
    int PAGE_PREFS = 8;

    /*
     * Types of events.
     */
    int RED = 1;
    int GREEN = 2;
    int ORANGE = 3;
    int YELLOW = 4;
    int GRAY = 5;
}
