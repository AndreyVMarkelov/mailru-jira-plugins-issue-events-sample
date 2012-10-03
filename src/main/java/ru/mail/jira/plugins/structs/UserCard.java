package ru.mail.jira.plugins.structs;

import java.util.HashSet;
import java.util.Set;

/**
 * User permission information.
 * 
 * @author Andrey Markelov
 */
public class UserCard
{
    /**
     * Divisions.
     */
    private Set<Integer> divisions;

    /**
     * Game projects.
     */
    private Set<Integer> gameProjects;

    /**
     * Is vice-president?
     */
    private boolean isVP;

    /**
     * Constructor.
     */
    public UserCard()
    {
        this.isVP = false;
        this.gameProjects = new HashSet<Integer>();
        this.divisions = new HashSet<Integer>();
    }

    /**
     * Add division.
     */
    public void addDivision(Integer d)
    {
        divisions.add(d);
    }

    /**
     * Add game project.
     */
    public void addGameProject(Integer gp)
    {
        gameProjects.add(gp);
    }

    public Set<Integer> getDivisions()
    {
        return divisions;
    }

    public Set<Integer> getGameProjects()
    {
        return gameProjects;
    }

    public boolean isAnyPermission()
    {
        return (isVP || !gameProjects.isEmpty() || !divisions.isEmpty());
    }

    public boolean isVP()
    {
        return isVP;
    }

    public void setVP(boolean isVP)
    {
        this.isVP = isVP;
    }

    @Override
    public String toString()
    {
        return ("UserCard[isVP=" + isVP + ", divisions=" + divisions + ", gameProjects=" + gameProjects + "]");
    }
}
