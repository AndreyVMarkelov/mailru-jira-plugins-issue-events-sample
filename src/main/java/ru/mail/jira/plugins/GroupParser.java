package ru.mail.jira.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GroupParser
{
    private static final String inFile = "/tmp/right.xlsx";

    private static final String outFile = "/tmp/rightout.sql";

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

        for (int i = 1; i < sheet.getLastRowNum(); i++)
        {
            XSSFRow datarow = sheet.getRow(i);
            if (datarow.getCell(2).getStringCellValue().equals(""))
            {
                break;
            }

            XSSFCell cell = datarow.getCell(0);
            String proj = cell.getStringCellValue();

            cell = datarow.getCell(5);
            String ac1 = cell.getStringCellValue();

            cell = datarow.getCell(6);
            if (cell != null)
            {
                String ac2 = cell.getStringCellValue();
                print.println(contructSql(proj, ac2));
            }

            print.println(contructSql(proj, ac1));
        }

        print.flush();
        print.close();
    }

    private static String contructSql(String proj, String user)
    {
        return String.format("INSERT INTO SYSTEM_ROLES(USERNAME, GP_ID) SELECT '%s', GP_ID FROM GAME_PROJECT WHERE LOCAL_NAME = '%s';", user, proj);
    }
}
