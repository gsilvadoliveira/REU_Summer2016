package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Scanner;

public class LogParser
{
    private LogParser()
    {}

    public static LogHistory parseLogHistory(String path) throws FileNotFoundException,
        ParseException
    {
        return parseLogHistory(new File(path));
    }

    public static LogHistory parseLogHistory(File file) throws ParseException,
        FileNotFoundException
    {
        LogHistory result = new LogHistory();
        try (Scanner reader = new Scanner(file))
        {
            while (reader.hasNext())
            {
                String line = reader.nextLine();
                if (line.contains("!!!"))
                    result.add(LogEvent.parse(line));
            }
        }
        return result;
    }
}
