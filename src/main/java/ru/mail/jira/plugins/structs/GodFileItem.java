package ru.mail.jira.plugins.structs;

import java.util.Arrays;

/**
 * Stored file item.
 * 
 * @author Andrey Markelov
 */
public class GodFileItem
{
    /**
     * Binary data.
     */
    private byte[] data;

    /**
     * File name.
     */
    private String name;

    /**
     * Constructor.
     */
    public GodFileItem(byte[] data, String name)
    {
        this.data = data;
        this.name = name;
    }

    public byte[] getData()
    {
        return data;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "FileItem[data=" + Arrays.toString(data) + ", name=" + name + "]";
    }
}
