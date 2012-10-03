package ru.mail.jira.plugins;

/**
 * Unique sequence.
 * 
 * @author Andrey Markelov
 */
public class UniqueNum
{
    private static long seq = System.currentTimeMillis();

    /**
     * Private constructor.
     */
    private UniqueNum() {}

    public synchronized static long getSeq()
    {
        return seq++;
    }
}
