package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.console.ArgumentType;
import com.kivancmuslu.www.console.ConsoleParser;
import com.kivancmuslu.www.timer.Duration;
import com.kivancmuslu.www.timer.Timer;

import conTest.SharedOperations;
import database.DBConnection;
import database.DBInterface;

public class QueryRunnerApp
{
    private static boolean writeConsole_;

    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") @NonNull public static final String LS = System.lineSeparator();

    public static void main(String[] args) throws SQLException, FileNotFoundException,
        InterruptedException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Boolean printConsoleDefault = Boolean.TRUE;
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Integer noRunsDefault = Integer.valueOf(10);
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Long delayDefault = Long.valueOf(0L);
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Integer noWarmUpRunsDefault = Integer.valueOf(-1);

        ConsoleParser parser = new ConsoleParser("Query Runner");
        parser.addArgument(ArgumentType.FILE, "query_file");
        parser.addArgument(ArgumentType.NEW_OR_EXISTING_FILE_OR_FOLDER, "output_file");
        parser.addOptionalArgument(ArgumentType.BOOLEAN, "print_console", printConsoleDefault);
        parser.addOptionalArgument(ArgumentType.INT, "no_runs", noRunsDefault);
        parser.addOptionalArgument(ArgumentType.LONG, "delay", delayDefault);
        parser.addOptionalArgument(ArgumentType.INT, "no_warm_up_runs", noWarmUpRunsDefault);
        parser.assertUsage(args);

        File queryFile = parser.getValidatedFile(0);
        File outputFile = parser.getValidatedFile(1);

        int noRuns = parser.getValidatedOptionalInteger("no_runs");
        int noWarmUpRuns = parser.getValidatedOptionalInteger("no_warm_up_runs");
        if (noWarmUpRuns == -1)
            noWarmUpRuns = noRuns / 2;
        long delay = parser.getValidatedOptionalLong("delay");

        run(queryFile, outputFile, noRuns, noWarmUpRuns, delay,
            parser.getValidatedOptionalBoolean("print_console"));
    }

    public static void run(File selectPool, File updatePool, File outputFile, long timeLimit,
                           long updateTimeLimit) throws FileNotFoundException, SQLException
    {
        DBConnection connection = DBInterface.getInstance().createConnection();

        long remainingTime = timeLimit;
        // Start with an update.
        long remainingUpdateTime = -1;

        ArrayList<String> selects = SharedOperations.readQueries(selectPool);
        ArrayList<String> updates = SharedOperations.readQueries(updatePool);

        int selectIndex = 0;
        int updateIndex = 0;

        int selectCount = 0;
        int updateCount = 0;

//        int maxUpdates = (int) (timeLimit / updateTimeLimit) - 1;
        int maxUpdates = (int) (timeLimit / updateTimeLimit);

        try (Formatter writer = new Formatter(outputFile))
        {
            while (remainingTime > 0)
            {
                if (remainingUpdateTime > 0 || updateCount == maxUpdates)
                {
                    // Run a select.
                    // Suppressed due to missing array element annotations.
                    @SuppressWarnings("null") @NonNull String selectQuery = selects.get(selectIndex);
                    Date now = new Date();
                    executeQuery(selectQuery, connection, writer);
                    long elapsed = new Date().getTime() - now.getTime();

                    selectCount++;
                    remainingUpdateTime -= elapsed;
                    remainingTime -= elapsed;

                    selectIndex++;
                    selectIndex %= selects.size();
                }
                else
                {
                    // Run an update.
                    remainingUpdateTime = updateTimeLimit;
                    // Suppressed due to missing array element annotations.
                    @SuppressWarnings("null") @NonNull String updateQuery = updates.get(updateIndex);
                    Date now = new Date();
                    executeQuery(updateQuery, connection, writer);
                    long elapsed = new Date().getTime() - now.getTime();

                    updateCount++;
                    remainingTime -= elapsed;

                    updateIndex++;
                    updateIndex %= updates.size();
                }
            }

            write(writer,
                  LS + "Execution for " + Duration.toHumanReadableString(timeLimit * 1000000)
                      + " completed." + LS);
            write(writer, "# of select executions: " + selectCount + LS);
            write(writer, "# of update executions: " + updateCount + LS);
        }
        connection.close();
    }

    public static void run(File queryFile, File outputFile, int noRuns, int noWarmUpRuns,
                           long delay, boolean writeConsole) throws SQLException,
        InterruptedException, FileNotFoundException
    {
        writeConsole_ = writeConsole;

        DBConnection connection = DBInterface.getInstance().createConnection();

        Timer.start();
        try (Formatter writer = new Formatter(outputFile); Scanner reader = new Scanner(queryFile))
        {
            reader.useDelimiter(";");
            ArrayList<String> queries = new ArrayList<>();
            while (reader.hasNext())
            {
                String query = reader.next().trim();
                if (!query.equals(""))
                    queries.add(query);
            }

            for (int a = 0; a < noRuns + noWarmUpRuns; a++)
            {
                Timer.start();
                write(writer, "Executing run: " + (a + 1) + ":" + LS);
                for (String query: queries)
                {
                    // Suppressed due to missing library annotations.
                    @SuppressWarnings("null") @NonNull String safeQuery = query;
                    executeQuery(safeQuery, connection, writer);
                    if (delay != 0)
                        Thread.sleep(delay);
                }
                long duration = Timer.stop();
                long sleepTime = delay * queries.size() * 1000000;
                duration -= sleepTime;
                write(writer,
                      "Run took: " + duration + " ns. ("
                          + new Duration(duration).toHumanReadableString() + ")." + LS + LS);
            }
            long duration = Timer.stop();
            long sleepTime = delay * queries.size() * noRuns;
            duration -= sleepTime;
            write(writer,
                  "Whole execution took: " + duration + " ns. ("
                      + new Duration(duration).toHumanReadableString() + ")." + LS + LS);

            duration /= noRuns;
            write(writer,
                  "An average run took: " + duration + " ns. ("
                      + new Duration(duration).toHumanReadableString() + ")." + LS + LS);
        }

        connection.close();
    }

    private static void executeQuery(String query, DBConnection connection, Formatter writer)
        throws SQLException
    {
        write(writer,
              "!!! " + SharedOperations.timestamp() + " @ " + SharedOperations.toOneLine(query)
                  + LS, true);

        if (query.toLowerCase().startsWith("select"))
        {
            try (ResultSet result = connection.executeQuery(query))
            {
                // Just execute the query, nothing to do with the results.
            }
            connection.commit();
        }
        else
            connection.executeUpdate(query);
    }

    private static void write(Formatter writer, String message)
    {
        write(writer, message, false);
    }

    private static void write(Formatter writer, String message, boolean skipConsole)
    {
        writer.format("%s", message);
        writer.flush();
        if (writeConsole_ && !skipConsole)
            System.out.print(message);
    }
}
