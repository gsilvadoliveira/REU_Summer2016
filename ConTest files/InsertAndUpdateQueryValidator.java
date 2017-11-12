package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.debug.Debug;
import com.kivancmuslu.www.process.Command;
import com.kivancmuslu.www.process.Output;

import conTest.SharedOperations;

public class InsertAndUpdateQueryValidator
{
    public static final HashMap<Integer, ArrayList<Integer>> breakageMap_ = new HashMap<>();
    public static final HashSet<Integer> brokenInsertOrUpdates_ = new HashSet<>();

    public static void main(String[] args) throws IOException, InterruptedException
    {
        File root = new File(new File(System.getProperty("user.home")), "Desktop");
        File testsFile = new File(root, "cdt_tpch.tests");
        File insertAndUpdatesFiles = new File(root, "cdt_tpch_simulation_inserts_and_updates.txt");

        ArrayList<String> tests = SharedOperations.readQueries(testsFile);
        ArrayList<String> insertAndUpdates = SharedOperations.readQueries(insertAndUpdatesFiles);

//        run(tests, insertAndUpdates, 0, 0, root);
        for (int a = 0; a < insertAndUpdates.size(); a++)
        {
            if (skipInsertOrUpdate(a + 1))
                continue;
            for (int b = 0; b < tests.size(); b++)
                run(tests, insertAndUpdates, b, a, root);
        }

        System.out.println();
        System.out.println("Breakage map:");
        ArrayList<Integer> keys = new ArrayList<>(breakageMap_.keySet());
        Collections.sort(keys);
        for (Integer key: keys)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull ArrayList<Integer> value = breakageMap_.get(key);
            System.out.println(key + ": " + Debug.join(value, ", "));
        }
    }

    private static boolean skipInsertOrUpdate(int insertOrUpdateIndex)
    {
        ArrayList<Integer> runList = new ArrayList<>();
        runList.add(Integer.valueOf(2));
        runList.add(Integer.valueOf(4));
        runList.add(Integer.valueOf(12));
        return !runList.contains(Integer.valueOf(insertOrUpdateIndex));
    }

    private static void run(ArrayList<String> tests, ArrayList<String> insertAndUpdates,
                            int testIndex, int insertOrUpdateIndex, File root) throws IOException,
        InterruptedException
    {
        System.out.println("Executing insert/update #" + (insertOrUpdateIndex + 1) + " on test #"
                           + (testIndex + 1));

        String test = tests.get(testIndex);
        String insertOrUpdate = insertAndUpdates.get(insertOrUpdateIndex);
//        insertOrUpdate = "select * from nation";

        String tempFileName = "cdt_insert_and_update_validator_temp.txt";
        File tempFile = new File(root, tempFileName);
        try (Formatter writer = new Formatter(tempFile))
        {
            writer.format("%s%n%n", "begin;");
            writer.format("%s%n%s%n%s%n%s%n%s%n%n", "create table before", "as", "(", test, ");");
            writer.format("%s%s%n%n", insertOrUpdate, ";");
            writer.format("%s%n%s%n%s%n%s%n%s%n%n", "create table after", "as", "(", test, ");");

            writer.format("%s%n%n", "select * from before except (select * from after);");
            writer.format("%s%n%n", "select * from after except (select * from before);");

            writer.format("%s%n", "abort;");
        }

        Command command = new Command("C:\\Program Files\\PostgreSQL\\9.3\\bin\\psql.exe", "-U",
                                      "postgres", "-d", "cdt1gb", "-f", tempFile.getAbsolutePath());
        Output output = command.execute(false);
        String stdOut = output.getStdOut();

        int noDifferences = 0;
        int noDiffResults = 0;
        boolean insertOrUpdateExecuted = false;
        for (String line: stdOut.split(System.lineSeparator()))
        {
            System.out.println(line);
            if (line.startsWith("UPDATE ") || line.startsWith("INSERT "))
                insertOrUpdateExecuted = true;

            if (line.startsWith("(") && (line.endsWith(" rows)") || line.endsWith(" row)")))
            {
                line = line.substring("(".length());
                if (line.endsWith(" rows)"))
                    line = line.substring(0, line.length() - " rows)".length());
                else
                    line = line.substring(0, line.length() - " row)".length());

                line = line.trim();
                noDifferences += Integer.parseInt(line);
                noDiffResults++;
            }
        }

        if (insertOrUpdateExecuted && noDiffResults == 2 && noDifferences != 0)
        {
            int key = (insertOrUpdateIndex + 1);
            int value = (testIndex + 1);
            Integer keyObject = Integer.valueOf(key);
            Integer valueObject = Integer.valueOf(value);

            System.out.println(key + " conflicts with " + value);
            if (!breakageMap_.containsKey(keyObject))
                breakageMap_.put(keyObject, new ArrayList<Integer>());
            breakageMap_.get(keyObject).add(valueObject);
        }

        String stdErr = output.getStdErr();
        if (!stdErr.trim().equals(""))
        {
            System.out.println("Something bad happened...");
            System.out.println(stdErr);
            brokenInsertOrUpdates_.add(Integer.valueOf(insertOrUpdateIndex + 1));
        }

        tempFile.delete();
    }
}
