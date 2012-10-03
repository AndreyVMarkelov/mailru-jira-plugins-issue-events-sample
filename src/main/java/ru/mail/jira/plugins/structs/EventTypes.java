package ru.mail.jira.plugins.structs;

import java.util.HashMap;
import java.util.Map;

public enum EventTypes
{
    STARTINCOUNTRY(1, "god.events.evtypes.startincountry", "red"),
    STARTPROJECT(2, "god.events.evtypes.startwork", "green"),
    KEYEVENT(3, "god.events.evtypes.keyevent", "orange"),
    LARGEEVENT(4, "god.events.evtypes.largeevent", "yellow"),
    ORDINALEVENT(5, "god.events.evtypes.ordinal", "gray");

    private int id;

    private String name;

    private String color;

    private EventTypes(int id, String name, String color)
    {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getColor()
    {
        return color;
    }

    public static Map<Integer, String> getEventTypesDict()
    {
        Map<Integer, String> res = new HashMap<Integer, String>();
        for (EventTypes et : EventTypes.values())
        {
            res.put(et.id, et.name);
        }

        return res;
    }
}
