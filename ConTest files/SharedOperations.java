package conTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNull;

public class SharedOperations implements SharedConstants
{
    public static final String CONFIGURATION_FILE_PATH = System.getProperty("user.home")
                                                         + File.separator + "cdt.config";

    private SharedOperations()
    {}

    public static ArrayList<String> readQueries(File testFile) throws FileNotFoundException
    {
        ArrayList<String> result = new ArrayList<>();
        try (Scanner reader = new Scanner(testFile))
        {
            reader.useDelimiter(";");
            while (reader.hasNext())
            {
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String testDescription = reader.next().trim();
                testDescription = removeComments(testDescription);
                if (testDescription.equals(""))
                    continue;

                result.add(testDescription);
            }
        }
        return result;
    }

    private static String removeComments(String testQuery)
    {
        String[] lines = testQuery.split(LS);
        StringBuilder result = new StringBuilder();
        boolean empty = true;
        for (String line: lines)
        {
            if (!line.startsWith("#"))
            {
                result.append(line);
                result.append(LS);
                empty = false;
            }
        }

        if (!empty)
            result.delete(result.length() - LS.length(), result.length());
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String res = result.toString().trim();
        return res;
    }

    public static Properties loadConfiguration()
    {
        try (InputStream is = new FileInputStream(CONFIGURATION_FILE_PATH))
        {
            Properties result = new Properties();
            result.load(is);
            return result;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String loadNonNullProperty(Properties properties, String propertyName)
    {
        String result = properties.getProperty(propertyName);
        if (result == null)
            throw new RuntimeException("Property: " + propertyName
                                       + " is not provided in dbconn.config file.");
        return result;
    }

    public static String timestamp()
    {
        Date now = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String result = format.format(now);
        return result;
    }

    public static String toOneLine(String information)
    {
        String result = information;
        result = result.replace(System.lineSeparator(), " ");
        result = result.replace("\t", " ");
        while (result.contains("  "))
            result = result.replace("  ", " ");
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String res = result.trim();
        return res;
    }
}
