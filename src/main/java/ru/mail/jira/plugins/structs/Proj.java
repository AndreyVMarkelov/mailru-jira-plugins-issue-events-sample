package ru.mail.jira.plugins.structs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Structure keeps data for HTML representation of report.
 * 
 * @author Andrey Markelov
 */
public class Proj
{
    /**
     * Business  model.
     */
    private int bModel;

    /**
     * Company department.
     */
    private int compDep;

    /**
     * Dates.
     */
    public Map<Long, List<EventRepr>> dates;

    /**
     * Colors.
     */
    public Map<Long, String> colors;

    /**
     * Game project ID.
     */
    private int id;

    /**
     * Local name of game project. 
     */
    private String localName;

    /**
     * Type of game project.
     */
    private int projectType;

    /**
     * Territory of game project.
     */
    private int territory;

    /**
     * Constructor.
     */
    public Proj(
        int id,
        String localName,
        int compDep,
        int bModel,
        int projectType,
        int territory,
        Collection<Long> dates)
    {
        this.bModel = bModel;
        this.compDep = compDep;
        this.id = id;
        this.localName = localName;
        this.projectType = projectType;
        this.territory = territory;
        this.dates = new TreeMap<Long, List<EventRepr>>();
        this.colors = new HashMap<Long, String>();

        if (dates != null)
        {
            for (Long date : dates)
            {
                this.dates.put(date, new ArrayList<EventRepr>());
            }
        }
    }

    public int getbModel()
    {
        return bModel;
    }

    public int getCompDep()
    {
        return compDep;
    }

    /**
     * Count of game project dates.
     */
    public int getDateCount()
    {
        return dates.size();
    }

    public Map<Long, List<EventRepr>> getDates()
    {
        return dates;
    }

    public int getId()
    {
        return id;
    }

    public String getLocalName()
    {
        return localName;
    }

    public int getProjectType()
    {
        return projectType;
    }

    public int getTerritory()
    {
        return territory;
    }

    public void setbModel(int bModel)
    {
        this.bModel = bModel;
    }

    public void setCompDep(int compDep)
    {
        this.compDep = compDep;
    }

    public Map<Long, String> getColors()
    {
        return colors;
    }

    public void setDates(Map<Long, List<EventRepr>> dates)
    {
        this.dates = dates;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    public void setProjectType(int projectType)
    {
        this.projectType = projectType;
    }

    public void setTerritory(int territory)
    {
        this.territory = territory;
    }

    @Override
    public String toString()
    {
        return "Proj[bModel=" + bModel + ", compDep=" + compDep + ", dates="
            + dates + ", id=" + id + ", localName=" + localName
            + ", projectType=" + projectType + ", territory=" + territory + "]";
    }

    public void computeColor()
    {
        for (Map.Entry<Long, List<EventRepr>> entry : dates.entrySet())
        {
            long time = entry.getKey();

            List<EventRepr> evts = entry.getValue();
            for (EventRepr er : evts)
            {
                String color;
                if ((color = colors.get(time)) == null)
                {
                    color = "";
                }

                if (er.getColor().equals("red"))
                {
                    colors.put(time, "#f05b54");
                }

                if (er.getColor().equals("green") && !color.equals("#f05b54"))
                {
                    colors.put(time, "#9dff8d");
                }

                if (er.getColor().equals("orange") && !color.equals("#f05b54") && !color.equals("#9dff8d"))
                {
                    colors.put(time, "#f8da91");
                }

                if (er.getColor().equals("yellow") && !(color.equals("#f05b54") || color.equals("#9dff8d") || color.equals("#f8da91")))
                {
                    colors.put(time, "#f9fd92");
                }

                if (er.getColor().equals("gray") && !(color.equals("#f05b54") || color.equals("#9dff8d") || color.equals("#f8da91") || color.equals("#f9fd92")))
                {
                    colors.put(time, "#b6bab6");
                }
            }
        }
    }
}
