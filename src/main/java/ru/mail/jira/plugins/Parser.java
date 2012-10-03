package ru.mail.jira.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Parser
{
    private static final String inFile = "/tmp/rm.xlsx";

    private static final String outFile = "/tmp/out.sql";

    static Map<String, Integer> prTypes;
    static Map<String, Integer> compDeps;
    static Map<String, Integer> bModels;
    static Map<String, Integer> statuses;
    static Map<String, Integer> terrs;
    static Map<String, Integer> evTypes;
    static
    {
        prTypes = new HashMap<String, Integer>();
        prTypes.put("Client", 1);
        prTypes.put("Mini", 2);
        prTypes.put("Mobile", 3);
        prTypes.put("Social", 4);
        prTypes.put("Web", 5);

        compDeps = new HashMap<String, Integer>();
        compDeps.put("Allods studio", 1);
        compDeps.put("Allods Studio", 1);
        compDeps.put("DJ", 2);
        compDeps.put("Europe", 3);
        compDeps.put("ITT", 4);
        compDeps.put("Mini", 5);
        compDeps.put("Mobile", 6);
        compDeps.put("Nord", 7);
        compDeps.put("Operator", 8);
        compDeps.put("Publishing", 9);
        compDeps.put("PushKin", 10);
        compDeps.put("Social", 11);
        compDeps.put("TZ", 12);

        bModels = new HashMap<String, Integer>();
        bModels.put("Dev", 1);
        bModels.put("In", 2);
        bModels.put("Invest", 3);
        bModels.put("invest", 3);
        bModels.put("Out", 4);
        bModels.put("out", 4);
        bModels.put("Par", 5);

        statuses = new HashMap<String, Integer>();
        statuses.put("Заморожен", 1);
        statuses.put("Запущен", 2);
        statuses.put("Подготовка", 3);
        statuses.put("Подписание/Заявка", 4);
        statuses.put("Разработка", 5);

        terrs = new HashMap<String, Integer>();
        terrs.put("MENA", 1);
        terrs.put("Англия", 2);
        terrs.put("Бразилия", 3);
        terrs.put("Весь мир", 4);
        terrs.put("Германия", 5);
        terrs.put("Индонезия", 6);
        terrs.put("Испания", 7);
        terrs.put("Италия", 8);
        terrs.put("Китай", 9);
        terrs.put("Корея", 10);
        terrs.put("Польша", 11);
        terrs.put("Poland", 11);
        terrs.put("Россия", 12);
        terrs.put("США", 13);
        terrs.put("Тайвань", 14);
        terrs.put("Турция", 15);
        terrs.put("Филиппины", 16);
        terrs.put("Франция", 17);
        terrs.put("Япония", 18);

        evTypes = new HashMap<String, Integer>();
        evTypes.put("obt", 1);
        evTypes.put("obt*", 1);
        evTypes.put("PT", 1);

        evTypes.put("cl", 2);
        evTypes.put("CL", 2);
        evTypes.put("Launch", 2);
        evTypes.put("cs", 2);
        evTypes.put("release", 2);

        evTypes.put("close", 3);
        evTypes.put("Close", 3);
        evTypes.put("CLOSE", 3);
        evTypes.put("shut down", 3);

        evTypes.put("MM", 4);
        evTypes.put("ММ", 4);

        evTypes.put("OK", 5);
        evTypes.put("ОК", 5);

        evTypes.put("VK", 6);
        evTypes.put("ВК", 6);
        
        evTypes.put("FB", 7);

        evTypes.put("sign", 8);

        evTypes.put("pp", 9);

        evTypes.put("vs", 10);
        evTypes.put("proto", 10);
        evTypes.put("test 1", 10);
        evTypes.put("test 2", 10);

        evTypes.put("abt", 11);
        evTypes.put("AN", 11);
        evTypes.put("techtest", 11);
        evTypes.put("alfa", 11);
        evTypes.put("ibt", 11);

        evTypes.put("cbt", 12);

        evTypes.put("cbt1", 13);

        evTypes.put("cbt2", 14);
        evTypes.put("сbt2", 14);
        evTypes.put("cbt3", 14);

        evTypes.put("анонс", 15);

        evTypes.put("upd-year", 16);
        evTypes.put("done", 16);
        evTypes.put("bd", 16);

        evTypes.put("ДР", 17);
        evTypes.put("др", 17);

        evTypes.put("event", 18);
        evTypes.put("икона", 18);
        evTypes.put("IP", 18);

        evTypes.put("promo", 19);
        evTypes.put("название", 19);
        evTypes.put("free", 19);
        evTypes.put("tv", 19);
        evTypes.put("send", 19);

        evTypes.put("upd", 20);
        evTypes.put("Upd", 20);
        evTypes.put("update", 20);
        evTypes.put("build", 20);
        evTypes.put("site", 20);
        evTypes.put("sup", 20);
        evTypes.put("vip", 20);
        evTypes.put("merge", 20);

        evTypes.put("sale", 21);
        evTypes.put("budget", 21);
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    static PrintWriter print;
    static
    {
        try
        {
            print = new PrintWriter(outFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    throws IOException
    {
        XSSFWorkbook wb = new XSSFWorkbook(inFile);
        XSSFSheet sheet = wb.getSheetAt(0);

        Map<Integer, Date> colIndexes = new HashMap<Integer, Date>();

        XSSFRow row = sheet.getRow(0);
        for (int i = 0; i < row.getLastCellNum(); i++)
        {
            XSSFCell cell = row.getCell(i);
            if (i < 11)
            {
                //--> nothing
            }
            else
            {
                Date date = cell.getDateCellValue();
                colIndexes.put(i, date);
            }
        }

        List<Proj> projs = new ArrayList<Proj>();
        List<Event> events = new ArrayList<Event>();

        for (int i = 1; i < sheet.getLastRowNum(); i++)
        {
            XSSFRow datarow = sheet.getRow(i);
            if (datarow.getCell(2).getStringCellValue().equals(""))
            {
                break;
            }

            Proj proj = new Proj();
            for (int j = 1; j < 11; j++)
            {
                XSSFCell cell = datarow.getCell(j);
                if (j == 1)
                {
                    proj.businessUnit = cell.getStringCellValue();
                }

                if (j == 2)
                {
                    proj.name = cell.getStringCellValue();
                }

                if (j == 3)
                {
                    proj.businessModel = cell.getStringCellValue();
                }

                if (j == 4)
                {
                    proj.type = cell.getStringCellValue();
                }

                if (j == 5)
                {
                    proj.status = cell.getStringCellValue();
                }

                if (j == 6)
                {
                    proj.territory = cell.getStringCellValue();
                }
            }
            projs.add(proj);

            for (int j = 11; j < datarow.getLastCellNum(); j++)
            {
                XSSFCell cell = datarow.getCell(j);
                if (cell != null && cell.getCellType() == 1)
                {
                    String type = cell.getStringCellValue();
                    String date = sdf.format(colIndexes.get(j));
                    String gp = proj.name;
                    String name = null;
                    if (cell.getCellComment() != null)
                    {
                        XSSFComment comment = cell.getCellComment();
                        name = comment.getString().toString().replaceAll("\n", " ");
                    }

                    type = type.replaceAll(";", ",");
                    StringTokenizer st = new StringTokenizer(type, ",");
                    while (st.hasMoreElements())
                    {
                        String otype = st.nextToken();
                        Event event = new Event();
                        event.date = date;
                        event.type = otype.trim();
                        event.gp = gp;
                        event.name = name;
                        events.add(event);
                    }
                }
            }
        }

        for (int i = 0; i < projs.size(); i++)
        {
            Proj p = projs.get(i);
            String sql = p.toSql();
            s(sql);
        }
        s("\n");
        for (int i = 0; i < events.size(); i++)
        {
            Event e = events.get(i);
            String sql = e.toSql();
            s(sql);
        }

        print.flush();
        print.close();
    }

    public static void s(Object str)
    {
        print.write(str.toString());
        print.write("\n");
    }

    static class Proj
    {
        String name;

        String businessUnit;

        String businessModel;

        String type;

        String status;

        String territory;

        @Override
        public String toString()
        {
            return "Proj [name=" + name + ", businessUnit=" + businessUnit + ", businessModel=" + businessModel + ", type=" + type + ", status=" + status + ", territory=" + territory + "]";
        }

        public String toSql()
        {
            if (bModels.get(businessModel) == null)
            {
                return "\n\n---- Business model error in: " + this.toString() + "\n\n";
            }

            if (compDeps.get(businessUnit) == null)
            {
                return "\n\n---- Company department error in: " + this.toString() + "\n\n";
            }

            if (terrs.get(territory) == null)
            {
                return "\n\n---- Territory error in: " + this.toString() + "\n\n";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO GAME_PROJECT (GP_ID, LOCAL_NAME, DESCR, ORIG_NAME, PR_TYPE, LOGO, PAGE, DEVELOPER, COMPANY_DEPT, BMODEL, STATUS, TERRITORY, DELETED) VALUES (DEFAULT, ");
            sb.append("'").append(name).append("', "); // LOCAL_NAME (+)
            sb.append("'', "); // DESCR (-)
            sb.append("'', "); // ORIG_NAME (-)
            sb.append(prTypes.get(type)).append(", "); // PR_TYPE (+)
            sb.append("'', "); // LOGO (-)
            sb.append("'', "); // PAGE (-)
            sb.append("'', "); // DEVELOPER (-)
            sb.append(compDeps.get(businessUnit)).append(", "); // COMPANY_DEP (+)
            sb.append(bModels.get(businessModel)).append(", "); // BMODEL (+)
            sb.append(statuses.get(status)).append(", "); // STATUS (+)
            sb.append(terrs.get(territory)).append(", "); // TERRITORY (+)
            sb.append("0);"); // DELETED (+)

            return sb.toString();
        }
    }

    static class Event
    {
        String name;

        String date;

        String type;

        String gp;

        @Override
        public String toString()
        {
            return "Event[name=" + name + ", date=" + date + ", type=" + type + "]";
        }

        public String toSql()
        {
            if (evTypes.get(type) == null)
            {
                return "\n\n---- Event type error in: " + this.toString() + "\n\n";
            }

            String n = (name != null) ? (name + " (" + type + ")") : (date + "-" + type);

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO EVENT (TITLE, NAME, DESCR, GP_ID, EK_ID, STARTDATE, STARTTIME, ENDDATE, ENDTIME, DELETED) SELECT ");
            sb.append("'").append(n).append("', "); // TITLE (-)
            sb.append("'").append(n).append("', "); // NAME (+)
            sb.append("'', "); // DESCR (-)
            sb.append("GP_ID, "); // GP_ID (+)
            sb.append(evTypes.get(type)).append(", "); // EK_ID (+)
            sb.append("'").append(date).append("', "); // STARTDATE (+)
            sb.append("'', "); // STARTTIME (-)
            sb.append("NULL, "); // ENDDATE (-)
            sb.append("'', "); // ENDTIME (-)
            sb.append("0 "); // DELETED (+)
            sb.append("FROM GAME_PROJECT WHERE LOCAL_NAME = '").append(gp).append("';");

            return sb.toString();
        }
    }
}
