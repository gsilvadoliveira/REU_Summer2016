package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import analysis.BreakageMap;
import analysis.LogEvent;
import analysis.LogHistory;
import analysis.LogParser;
import app.CDTApp.CDTType;
import app.CDTApp.ExperimentType;

import com.kivancmuslu.www.debug.Debug;
import com.kivancmuslu.www.timer.Duration;

public class LogAnalyzer
{
    public static final long MILLION = 1000 * 1000;

    public static boolean DEBUG_EXECUTION = false;

    public static void main(String[] args) throws FileNotFoundException, ParseException
    {
        String size = "1gb";
        String experiment = "30m_10m";

//        CDTType cdtType = CDTType.NAIVE;
//        ExperimentType experimentType = null;

        CDTType cdtType = CDTType.NORMAL;
//        ExperimentType experimentType = ExperimentType.DOUBLE_STATIC;
        ExperimentType experimentType = ExperimentType.STATIC_NAIVE;
//        ExperimentType experimentType = ExperimentType.DOUBLE_NAIVE;

        String logPostfix =
            "cdt(" + cdtType + ")_experiment(" + experimentType + ")" + experiment + ".txt";
        String cdtLog = "cdt_" + logPostfix;
        String queryRunnerLog = "query_runner_" + logPostfix;

        File root =
            new File(new File(System.getProperty("user.home"), "Desktop"), "cdt_perf_experiments");
        File experimentRoot = new File(new File(root, size), experiment);
        File cdtLogFile = new File(experimentRoot, cdtLog);
        File queryRunnerLogFile = new File(experimentRoot, queryRunnerLog);

        File testPool = new File(root, "cdt_tpch.tests");
        File updatePool = new File(root, "cdt_tpch_simulation_inserts_and_updates.txt");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String updatePoolPath = updatePool.getAbsolutePath();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String testPoolPath = testPool.getAbsolutePath();

        run(queryRunnerLogFile, cdtLogFile, updatePoolPath, testPoolPath);
    }

    private static void run(File queryRunnerLogFile, File cdtLogFile, String updatePath,
                            String testPath) throws FileNotFoundException, ParseException
    {
        BreakageMap breakageMap = new BreakageMap(updatePath, testPath);

        LogHistory queryRunnerHistory = LogParser.parseLogHistory(queryRunnerLogFile);
        LogHistory cdtHistory = LogParser.parseLogHistory(cdtLogFile);

        LogHistory experimentHistory = queryRunnerHistory.merge(cdtHistory);

        ArrayList<LogEvent> missedUpdates = new ArrayList<>();
        int testFailures = 0;
        int testPasses = 0;
        ArrayList<Long> epsilons = new ArrayList<>();
        LogEvent lastUpdate = null;
        for (LogEvent event: experimentHistory)
        {
            String information = event.getInformation();
            if (DEBUG_EXECUTION && !event.getInformation().toLowerCase().startsWith("select"))
                System.out.println(event);
            if (isUpdateOrInsertQuery(information))
            {
//                System.out.println(event);
                if (lastUpdate != null)
                    missedUpdates.add(lastUpdate);
                lastUpdate = event;
            }
            else
            {
                String action = event.getAction();
                if (action.equals("FAIL") || action.equals("SUCCESS"))
                {
                    if (DEBUG_EXECUTION)
                        System.out.println(event);

                    if (action.equals("FAIL"))
                        testFailures++;
                    else
                        testPasses++;

                    if (lastUpdate != null)
                        lastUpdate =
                            doesTestCaptureUpdate(lastUpdate, event, epsilons, breakageMap);
                }
            }
        }

        System.out.println("Analysis completed, results:");
        System.out.println("Total updates = " + (epsilons.size() + missedUpdates.size()));
        System.out.println("Caught updates = " + epsilons.size());
        if (epsilons.size() != 0)
        {
            System.out.println("Epsilons: " + Debug.join(epsilons, ", "));
            long epsilonTotal = 0;
            for (Long epsilon: epsilons)
                epsilonTotal += epsilon.longValue();
            long averageEpsilon = epsilonTotal / epsilons.size();
            System.out.println("Average epsilon = "
                               + Duration.toHumanReadableString(averageEpsilon * MILLION));
        }
        System.out.println();

        System.out.println("Total test runs = " + (testFailures + testPasses));
        System.out.println("Test failures = " + testFailures);
        System.out.println("Test passes = " + testPasses);
        System.out.println();

        System.out.println("Missed updates = " + missedUpdates.size());
        if (missedUpdates.size() != 0)
        {
            System.out.println("Missed update queries: ");
            for (LogEvent event: missedUpdates)
                System.out.println("\t" + event.getInformation());
        }
    }

    private static @Nullable
    LogEvent doesTestCaptureUpdate(LogEvent lastUpdate, LogEvent event, ArrayList<Long> epsilons,
                                   BreakageMap breakageMap)
    {
        String test = event.getInformation();
        String update = lastUpdate.getInformation();
        if (breakageMap.doesUpdateBreaksTest(update, test))
        {
            if (DEBUG_EXECUTION)
                System.out.println("Caught " + update + " with test: " + test);
            long epsilon = event.getTimestamp().getTime() - lastUpdate.getTimestamp().getTime();
            epsilons.add(Long.valueOf(epsilon));
            return null;
        }
        return lastUpdate;
    }

    private static boolean isUpdateOrInsertQuery(String information)
    {
        String temp = information.toLowerCase();
        return temp.startsWith("insert") || temp.startsWith("update");
    }
}
