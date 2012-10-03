package ru.mail.jira.plugins.structs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DictWrap
{
    private Map<Integer, String> cds;
    private Map<Integer, String> pts;
    private Map<Integer, String> bms;
    private Map<Integer, String> eks;
    private Map<Integer, String> gpTerr;
    private Map<Integer, String> gpss;
    private SimpleDateFormat sdf;

    public DictWrap(
        Map<Integer, String> cds,
        Map<Integer, String> pts,
        Map<Integer, String> bms,
        Map<Integer, String> eks,
        Map<Integer, String> gpTerr,
        Map<Integer, String> gpss)
    {
        this.cds = cds;
        this.pts = pts;
        this.bms = bms;
        this.eks = eks;
        this.gpTerr = gpTerr;
        this.gpss = gpss;
        this.sdf = new SimpleDateFormat("yyyy-MM-dd");
    }

    public String getVal(String field, String val)
    {
        if (val.equals(""))
        {
            return "";
        }

        if (field.equals("type"))
        {
            return pts.get(Integer.parseInt(val));
        }
        else if (field.equals("model"))
        {
            return bms.get(Integer.parseInt(val));
        }
        else if (field.equals("compdep"))
        {
            return cds.get(Integer.parseInt(val));
        }
        else if (field.equals("eventtype"))
        {
            return eks.get(Integer.parseInt(val));
        }
        else if (field.equals("territory"))
        {
            return gpTerr.get(Integer.parseInt(val));
        }
        else if (field.equals("project"))
        {
            return gpss.get(Integer.parseInt(val));
        }
        else if (field.equals("startdate"))
        {
            Long lVal = Long.parseLong(val);
            if (lVal <= 0)
            {
                return "";
            }
            return sdf.format(new Date(lVal));
        }
        else if (field.equals("enddate"))
        {
            Long lVal = Long.parseLong(val);
            if (lVal <= 0)
            {
                return "";
            }
            return sdf.format(new Date(lVal));
        }
        else
        {
            return val;
        }
        
    }
}
