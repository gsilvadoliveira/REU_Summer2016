package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;

import analysis.LogHistory;
import analysis.LogHistoryDiff;
import analysis.LogParser;
import analysis.TimelessLogEvent;

public class CDTLogComparer
{
    public static void main(String[] args) throws FileNotFoundException, ParseException
    {
        File root = new File("C:\\Users\\Kivanc\\Desktop\\CDT Experiments\\1GB\\30m_5m");
        File file1 = new File(root, "cdt_cdt(NORMAL)_experiment(DOUBLE_STATIC)30m_5m.txt");
        File file2 = new File(root, "cdt_cdt(NORMAL)_experiment(STATIC_NAIVE)30m_5m.txt");

        run(file1, file2);
    }

    public static void run(File log1, File log2) throws FileNotFoundException, ParseException
    {
        LogHistory history1 = LogParser.parseLogHistory(log1);
//        System.out.println(history1);
        LogHistory history2 = LogParser.parseLogHistory(log2);
//        System.out.println(history2);

        LogHistoryDiff diff = history1.diff(history2);
        ArrayList<TimelessLogEvent> firstOnly = diff.getFirstOnly();
        ArrayList<TimelessLogEvent> secondOnly = diff.getSecondOnly();
        if (firstOnly.size() != 0)
        {
            System.out.println("Events contained in the first history only.");
            for (TimelessLogEvent event: firstOnly)
                System.out.println(event);
            System.out.println("");
        }
        if (secondOnly.size() != 0)
        {
            System.out.println("Events contained in the second history only.");
            for (TimelessLogEvent event: secondOnly)
                System.out.println(event);
        }
    }
}
