package ru.mail.jira.plugins.structs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistWrap
{
    private List<History> hist;

    public HistWrap(List<History> hist)
    {
        this.hist = hist;
    }

    public HistWrap(List<History> hist, int id)
    {
        this.hist = new ArrayList<History>();
        Iterator<History> iter = hist.iterator();
        while (iter.hasNext())
        {
            History h = iter.next();
            if (h.getRefId() == id)
            {
                this.hist.add(h);
            }
        }
    }

    public List<History> getHist()
    {
        return hist;
    }

    public boolean projectExist(int ptojId)
    {
        boolean res = false;

        for (History h : hist)
        {
            if ((h.getType() == 2 || h.getType() == 3) && h.getRefId() == ptojId)
            {
                res = true;
                break;
            }
        }

        return res;
    }

    public boolean eventExist(int evId)
    {
        boolean res = false;

        for (History h : hist)
        {
            if (h.getType() == 1 && h.getRefId() == evId)
            {
                res = true;
                break;
            }
        }

        return res;
    }
}
