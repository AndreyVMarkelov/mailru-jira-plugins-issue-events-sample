package ru.mail.jira.plugins.structs;

import ru.mail.jira.plugins.UniqueNum;

/**
 * Event for HTML representation.
 * 
 * @author Andrey Markelov
 */
public class EventRepr
{
    /**
     * Event color.
     */
    private String color;

    /**
     * End date of event.
     */
    private String endDate;

    /**
     * Event ID.
     */
    private int id;

    /**
     * Event name.
     */
    private String name;

    /**
     * Start date of event.
     */
    private String startDate;

    /**
     * Event type.
     */
    private String type;

    /**
     * Event int type.
     */
    private int typeInt;

    /**
     * Unique number.
     */
    private long uniqueNum;

    /**
     * Constructor.
     */
    public EventRepr(
        int id,
        String name,
        int typeInt,
        String type,
        String startDate,
        String endDate,
        String color)
    {
        this.uniqueNum = UniqueNum.getSeq();
        this.id = id;
        this.name = name;
        this.typeInt = typeInt;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
    }

    public String getColor()
    {
        return color;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public int getId()
    {
        return id;
    }

    public String getInfo()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(startDate);
        if (!endDate.isEmpty())
        {
            sb.append("-").append(endDate);
        }
        sb.append(": ").append(name);

        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public String getType()
    {
        return type;
    }

    public long getUniqueNum()
    {
        return uniqueNum;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public int getTypeInt()
    {
        return typeInt;
    }

    public void setTypeInt(int typeInt)
    {
        this.typeInt = typeInt;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "EventRepr[id=" + id + ", name=" + name + ", type=" + type
            + ", startDate=" + startDate + ", endDate=" + endDate
            + ", color=" + color + "]";
    }
}
