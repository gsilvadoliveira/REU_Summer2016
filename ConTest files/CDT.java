package conTest;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import testing.CoalescedTest;
import testing.Test;
import testing.TestResult;

import com.kivancmuslu.www.concurrent.LinkedBlockingSet;
import com.kivancmuslu.www.debug.Debug;

import conTest.incremental.AggregateComputer;
import conTest.incremental.ClauseEvaluator;
import conTest.incremental.CountComputer;
import conTest.incremental.SumComputer;
import database.DBInterface;
import database.DBOperations;

public class CDT extends CDTBase
{
    static final String PL_PYTHON = getPLPythonVersion();

    private static String getPLPythonVersion()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return "plpython3u";
        return "plpython2u";
    }

    public static String TRIGGER_CONTROLLER_CLASS = "conTest.StaticTriggerController";
    public static String TEST_CONTROLLER_CLASS = "conTest.SmartTestController";

    private static final int NO_SERVER_THREADS = 10;
    private static final int NO_TEST_RUNNERS = 10;

    public static boolean DEBUG_TEST_QUEUE = false;
    public static boolean DEBUG_INCREMENTAL_COMPUTATION = false;
    public static boolean DEBUG_COALESCE = false;

    private final CDTServer server_;
    private final Thread serverThread_;

    private final LinkedBlockingSet<Test> testsToRun_;
    private final ConcurrentHashMap<String, Test> initializedTests_;

    private final ExecutorService testRunner_;

    private final TriggerController triggerController_;
    private final TestController testController_;

    public static boolean COALESCE_TESTS = true;
    public static boolean INCREMENTAL_TESTS = true;

    CDT()
    {
        // Do initial checks to make sure that CDT can run without any problems. If there is a
        // problem, show an error to the user and quit.
        assertPreconditions();

        // Create and run the server in a separate thread.
        server_ = new CDTServer(NO_SERVER_THREADS);
        serverThread_ = new Thread(server_);

        initializedTests_ = new ConcurrentHashMap<>();
        testsToRun_ = new LinkedBlockingSet<>();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull ExecutorService testRunner =
            Executors.newFixedThreadPool(NO_TEST_RUNNERS);
        testRunner_ = testRunner;
        triggerController_ =
            TriggerControllerFactory.createTriggerController(TRIGGER_CONTROLLER_CLASS);
        logInfo("Using trigger controller = " + triggerController_.getClass());
        testController_ = TestControllerFactory.createTestController(TEST_CONTROLLER_CLASS);
        logInfo("Using test controller = " + testController_.getClass());
    }

    private void assertPreconditions()
    {
        // Make sure that we can connect to the database without any problems.
        assertDatabaseConnection();
        // Make sure that plpython3u is installed (or can be installed).
        assertPLPythonInstallation();
    }

    private void assertDatabaseConnection()
    {
        try
        {
            DBInterface.getCurrentConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        // Must use logger_ as 'this' is not created yet.
        logInfo("Connection to database estabilished with success.");
    }

    private void assertPLPythonInstallation()
    {
        try
        {
            List<String> installedLanguages = DBOperations.getInstalledLanguages();
            boolean installed = installedLanguages.contains(PL_PYTHON);
            // If not installed, try installing it.
            if (!installed)
            {
                logInfo("Language: " + PL_PYTHON + " is not installed, installing...");
                DBOperations.installLanguage(PL_PYTHON);
                logInfo("Language: " + PL_PYTHON + " installed with success.");
            }
            else
                logInfo("Language: " + PL_PYTHON + " is already installed.");
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runInternal()
    {
        try
        {
            if (COALESCE_TESTS)
                coalesceTests();

            Test testToRun = testsToRun_.dequeue();

            if (testToRun != Test.NULL)
                testToRun.run();
            // FIXME Fix the problem with the executor service. Executor service lets us run things
            // on parallel, however it automatically schedules things, so the same test can be
            // scheduled multiple times (which we don't want). For now, I fallback to serial runner.
//                testRunner_.execute(testToRun);
            if (DEBUG_TEST_QUEUE)
                logInfo("There are " + testsToRun_.size() + " tests to run.");

            Thread.sleep(EXECUTION_DELAY);
        }
        catch (InterruptedException e)
        {
            logWarning("Could not retrieve the next test.", e);
        }
    }

    private void coalesceTests()
    {
        LinkedHashMap<String, LinkedHashSet<String>> coalesceMap = new LinkedHashMap<>();
        LinkedHashMap<String, LinkedHashSet<Test>> previousTests = new LinkedHashMap<>();

        while (!testsToRun_.isEmpty())
        {
            try
            {
                // This should be safe to do since this method is the only thing that will dequeue
                // tests. Another thread can enqueue new tests, but that is fine.
                Test test = testsToRun_.dequeue();
                String testQuery = test.getTestQuery();
                String[] queryParts = splitQuery(testQuery);
                String select = queryParts[0];

                // get the selects...
                String[] parts = select.split(",");
                LinkedHashSet<String> selects = new LinkedHashSet<>();
                for (String part: parts)
                    selects.add(part.trim());

                String from = queryParts[1];
                String where = queryParts[2];
                String groupby = queryParts[3];

                String key =
                    " from " + from + (where == null ? "" : " where " + where)
                        + (groupby == null ? "" : " group by " + groupby);
                if (!coalesceMap.containsKey(key))
                    coalesceMap.put(key, new LinkedHashSet<String>());
                coalesceMap.get(key).addAll(selects);

                if (!previousTests.containsKey(key))
                    previousTests.put(key, new LinkedHashSet<Test>());
                previousTests.get(key).addAll(test.getParts());
            }
            catch (InterruptedException e)
            {
                logWarning("Cannot coalesce tests.", e);
            }
        }

        for (String queryPostfix: coalesceMap.keySet())
        {
            LinkedHashSet<String> selects = coalesceMap.get(queryPostfix);
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull LinkedHashSet<Test> tests =
                previousTests.get(queryPostfix);

            if (selects.size() == 1)
            {
                // Suppressed due to complex program logic.
                @SuppressWarnings("null") @NonNull Test test =
                    previousTests.get(queryPostfix).iterator().next();
                if (DEBUG_COALESCE)
                    logInfo("Cannot coalesce test: " + test.getTestQuery()
                            + ", putting it back to the queue.");
                testsToRun_.enqueue(test);
            }
            else
            {
                String queryPrefix = "select ";
                int counter = 0;
                for (String select: selects)
                {
                    if (counter != 0)
                        queryPrefix += LS + "\t, ";
                    queryPrefix += select;
                    counter++;
                }
                String query = queryPrefix + LS + queryPostfix;
                Test coalescedTest = new CoalescedTest(query, new ArrayList<>(tests));
                if (DEBUG_COALESCE)
                    logInfo("Coalesced " + selects.size() + " tests into: "
                            + coalescedTest.getTestQuery());

                // If this test is not initialized before, initialize it...
                initializeIfNecessary(coalescedTest);
                testsToRun_.enqueue(coalescedTest);
            }
        }
    }

    private void initializeIfNecessary(Test test)
    {
        String key = test.getFinalID();
        if (!initializedTests_.containsKey(key))
        {
            // "False" because we don't want to create triggers for these new test queries. They are
            // just temporary and we should not rerun them when there is a new update.
            Runnable testInitializationJob = test.createInitializationJob();
            testInitializationJob.run();
        }
    }

    @SuppressWarnings("static-method")
    private void createInitialTests()
    {
        try
        {
            for (Test test: readTests())
            {
                Runnable testInitializationJob = test.createInitializationJob();
                // TODO KM: Changed for the performance experiments so that we can ensure that the
                // tests are initialized by the time query runner is running.
                testInitializationJob.run();
//                testsToRun_.enqueue(testInitializationJob);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void kill()
    {
        logInfo("Killing CDT...");
        super.kill();
        testsToRun_.enqueue(Test.NULL);
    }

    @Override
    protected void shutdown()
    {
        // Wait until all tests are finished running.
        shutdownTestRunner();
        // Cleanup initialized tests.
        cleanupInitializedTests();
        // Remove the trigger function (which will remove all triggers)
        removeCDTTriggerFunction();
        // Clear trigger caches.
        CDTTriggerOptimizer.getInstance().clear();
        // Shutdown server.
        shutdownServer();
        // Close the database connection.
        closeDBConnection();
    }

    private void cleanupInitializedTests()
    {
        for (Test test: initializedTests_.values())
        {
            try
            {
                test.removeFromDB();
            }
            catch (SQLException e)
            {
                logWarning("Cannot remove " + test + " from database.", e);
            }
        }
    }

    private void closeDBConnection()
    {
        logInfo("Closing the database connection.");
        try
        {
            DBInterface.close();
        }
        catch (SQLException e)
        {
            logWarning("Cannot close the database connection.", e);
        }
    }

    private void shutdownServer()
    {
        logInfo("Killing server thread.");
        server_.kill();
        try
        {
            serverThread_.join();
        }
        catch (InterruptedException e)
        {
            logInterruptedException("Server thread completes working.", e);
        }
    }

    private void removeCDTTriggerFunction()
    {
        try
        {
            DBOperations.dropFunctionIfExists(CDT_TRIGGER_FUNCTION_NAME);
            logWarning("Removed function: " + CDT_TRIGGER_FUNCTION_NAME + " with success.");
        }
        catch (SQLException e)
        {
            logWarning("Cannot drop function: " + CDT_TRIGGER_FUNCTION_NAME, e);
        }
    }

    private void shutdownTestRunner()
    {
        logInfo("Shutting down test runner...");
        testRunner_.shutdown();
        try
        {
            // No timeout...
            testRunner_.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
            logInterruptedException("Test runner completes.", e);
        }
    }

    @Override
    protected void preInit()
    {
        serverThread_.start();

        // Note that this has to be asserted after CDT Server starts listening for incoming
        // connections as we need the port information in the function.
        assertTriggerFunctionInstallation();
    }

    @Override
    protected void postInit()
    {
        createInitialTests();
    }

    private void assertTriggerFunctionInstallation()
    {
        try
        {
            // Read python function template
            try (InputStream is = DBInterface.class.getResourceAsStream("trigger_callback.py");
                 Scanner reader = new Scanner(is))
            {
                StringBuilder source = new StringBuilder();
                while (reader.hasNext())
                {
                    source.append(reader.nextLine());
                    source.append(LS);
                }
                String sourceCode = source.toString();
                sourceCode = sourceCode.replace("DYNAMIC_LOG_PATH", CDT_TRIGGER_FUNCTION_LOG_PATH);
                // Make sure that server is started.
                server_.waitUntilStarted();
                sourceCode = sourceCode.replace("DYNAMIC_SERVER_PORT", "" + server_.getPort());
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String safeSourceCode = sourceCode;
                DBOperations.createTriggerFunction(CDT_TRIGGER_FUNCTION_NAME, PL_PYTHON,
                                                   safeSourceCode);
                logInfo("Installed trigger function with success." + LS + safeSourceCode);
            }
        }
        catch (Throwable t)
        {
            // Shutdown the server and quit.
            shutdown();
            throw new RuntimeException(t);
        }
    }

    @Override
    public void testStartedInternal(Test test)
    {
//        logger_.logInfo(test + " started.");
    }

    @Override
    public void testCompletedInternal(Test test, TestResult result)
    {
        testCompletedInternal(test, result, null);
    }

    @Override
    public void testCompletedInternal(Test test, TestResult result, @Nullable SQLException error)
    {
        switch (result)
        {
            case FAIL:
                // For performance tests, we decided not to recahce failing tests.
//                try
//                {
//                    test.recacheResults();
//                }
//                catch (SQLException e)
//                {
//                    logWarning("Cannot cache results for failing test: " + test, e);
//                }
                break;
            case PASS:
            case ERROR:
                break;
        }
    }

    @Override
    public void testInitialized(Test test)
    {
        testInitialized(test, null);
    }

    @Override
    public void testInitialized(Test test, @Nullable Throwable error)
    {
        if (error == null)
        {
            logInfo(test + " initialized.");
            initializedTests_.put(test.getFinalID(), test);
        }
        else
            logWarning("Could not initalize " + test, error);
    }

    @Override
    public void scheduleTest(Test test)
    {
        timestampTest("SCHEDULE", test);
        testsToRun_.enqueue(test);
    }

    @Override
    public Test[] getInitializedTests()
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Test[] result =
            initializedTests_.values().toArray(new Test[initializedTests_.size()]);
        return result;
    }

    @Override
    protected void triggerActivatedInternal(HashMap<String, Object> triggerData)
    {
        timestamp("ACTIVATE", Debug.join(triggerData, ", "));
        testController_.triggerActivated(triggerData);
    }

    @Override
    public void scheduleTestWithID(String testID, Map<String, Object> previousData,
                                   Map<String, Object> currentData)
    {
        Test test = initializedTests_.get(testID);
        if (test != null)
        {
            boolean shallSheduleTest = true;

            if (INCREMENTAL_TESTS)
            {
                try
                {
                    // Ensure that there is only one relation in the FROM clause.
                    String testQuery = test.getTestQuery();
                    String[] queryParts = splitQuery(testQuery);
                    // Suppressed due to missing library annotations.
                    @SuppressWarnings("null") @NonNull String select = queryParts[0];
                    String from = queryParts[1].trim();
                    if (!from.contains(","))
                    {
                        // We know that there is only one relation in the FROM clause.
                        String where = queryParts[2];
                        boolean previousResult = true;
                        boolean currentResult = true;
                        if (where != null)
                        {
                            // Suppressed due to missing library annotations.
                            previousResult =
                                ClauseEvaluator.evaluateBooleanClause(where, previousData);
                            currentResult =
                                ClauseEvaluator.evaluateBooleanClause(where, currentData);
                        }

                        if (!previousResult && !currentResult)
                        {
                            if (DEBUG_INCREMENTAL_COMPUTATION)
                                logInfo("Incremental test computation realized that trigger is a false positive. No computation is needed.");
                        }
                        else
                            shallSheduleTest =
                                !incrementallyUpdateTestResults(previousData, currentData, test,
                                                                select, previousResult,
                                                                currentResult);
                    }
                    else
                    {
                        if (DEBUG_INCREMENTAL_COMPUTATION)
                            logInfo("Cannot compute test incrementally as it applies to multiple relations (from = "
                                    + from + "): " + testQuery);
                    }
                }
                catch (Throwable e)
                {
                    logWarning("Incremental computation faced unexpection exception.", e);
                }
            }
            if (shallSheduleTest)
                scheduleTest(test);
        }
        else
            logWarning("Cannot schedule test with id: " + testID + ". No such test exists.");
    }

    private static String[] splitQuery(String inputQuery)
    {
        String query = inputQuery;
        String temp = query.toLowerCase();

        String[] result = new String[4];

        if (temp.startsWith("select") && temp.contains("from"))
        {
            int endIndex = temp.indexOf("from");
            String select = query.substring("select".length(), endIndex).trim();
            result[0] = select;

            temp = temp.substring(endIndex + "from".length()).trim();
            query = query.substring(endIndex + "from".length()).trim();

            if (temp.contains("where"))
            {
                endIndex = temp.indexOf("where");
                String from = query.substring(0, endIndex).trim();
                result[1] = from;

                temp = temp.substring(endIndex + "where".length()).trim();
                query = query.substring(endIndex + "where".length()).trim();

                if (temp.contains("group by"))
                {
                    endIndex = temp.indexOf("group by");
                    String where = query.substring(0, endIndex).trim();
                    result[2] = where;

                    temp = temp.substring(endIndex + "group by".length()).trim();
                    query = query.substring(endIndex + "group by".length()).trim();
                    result[3] = query;
                }
                else
                    // There is no group by clause.
                    result[2] = query;
            }
            else if (temp.contains("group by"))
            {
                // There is no where clause.
                endIndex = temp.indexOf("group by");
                String from = query.substring(0, endIndex).trim();
                result[1] = from;

                result[2] = null;

                temp = temp.substring(endIndex + "group by".length()).trim();
                query = query.substring(endIndex + "group by".length()).trim();

                result[3] = query;
            }
            else
                // There are no where and group by clauses.
                result[1] = query;
        }
        return result;
    }

    private boolean incrementallyUpdateTestResults(Map<String, Object> previousData,
                                                   Map<String, Object> currentData, Test test,
                                                   String select, boolean previousResult,
                                                   boolean currentResult)
    {
        if (DEBUG_INCREMENTAL_COMPUTATION)
            logInfo("Incremental test computation: computing the WHERE clause on the previous and current data.");
        try
        {
            try
            {
                String[] testTables = test.getFinalIDs();
                ResultSet[] resultSets = new ResultSet[testTables.length];
                for (int a = 0; a < resultSets.length; a++)
                {
                    resultSets[a] = DBOperations.executeQuery("SELECT * FROM " + testTables[a]);
                    resultSets[a].next();
                }

                String[] selectParts = select.split(", ");

                for (String selectPart: selectParts)
                {
                    selectPart = selectPart.trim();
                    String alias = null;
                    if (selectPart.contains(" as "))
                        alias =
                            selectPart.substring(selectPart.indexOf(" as ") + " as ".length(),
                                                 selectPart.length()).trim();

                    boolean result = true;
                    AggregateComputer<?> computer = null;
                    if (selectPart.startsWith("sum(") && selectPart.contains(")"))
                    {
                        String attribute = computeAttribute(selectPart, "sum");
                        if (alias == null)
                            alias = attribute;

                        double previousSum = 0;
                        boolean found = false;
                        for (ResultSet resultSet: resultSets)
                        {
                            try
                            {
                                previousSum = resultSet.getFloat(alias);
                                found = true;
                                break;
                            }
                            catch (SQLException e)
                            {
                                // We could not find the alias on this table, continue the search
                            }
                        }

                        if (found)
                        {
                            // Suppressed due to missing library annotations.
                            @SuppressWarnings("null") @NonNull Double prevSum =
                                Double.valueOf(previousSum);
                            computer =
                                new SumComputer(attribute, prevSum, previousResult, currentResult,
                                                previousData, currentData);
                        }
                        else
                            logWarning("Cannot found " + alias + " in any of the test tables.");
                    }
                    else if (selectPart.startsWith("count(") && selectPart.contains(")"))
                    {
                        String attribute = computeAttribute(selectPart, "count");
                        if (alias == null)
                            alias = attribute;

                        int previousCount = 0;
                        boolean found = false;
                        for (ResultSet resultSet: resultSets)
                        {
                            try
                            {
                                previousCount = resultSet.getInt(alias);
                                found = true;
                                break;
                            }
                            catch (SQLException e)
                            {
                                // We could not find the alias on this table, continue the search
                            }
                        }

                        if (found)
                        {
                            // Suppressed due to missing library annotations.
                            @SuppressWarnings("null") @NonNull Integer prevCount =
                                Integer.valueOf(previousCount);
                            computer =
                                new CountComputer(attribute, prevCount, previousResult,
                                                  currentResult, previousData, currentData);
                        }
                        else
                            logWarning("Cannot found " + alias + " in any of the test tables.");
                    }
                    else if (selectPart.startsWith("avg(") && selectPart.contains(")"))
                    {
                        // TODO KM: We don't support 'avg' aggregate yet.
                        if (DEBUG_INCREMENTAL_COMPUTATION)
                            logInfo("Incremental test computation does not support avg aggregates. Falling back to normal test execution.");
                        // Average is not supported yet.
                        return false;
//                            String attribute = computeAttribute(selectPart, "avg");
//                            if (alias == null)
//                                alias = attribute;
//                            double previousAverage = resultSet.getFloat(alias);
//                            computer =
//                                new AverageComputer(attribute, previousAverage, previousResult,
//                                                    currentResult, previousData, currentData);
                    }

                    if (computer != null)
                    {
                        if (DEBUG_INCREMENTAL_COMPUTATION)
                            logInfo("Incremental test computation: incrementally updating the result.");
                        result = computer.incrementallyCompute();
                    }
                    else
                        logWarning("Incremental test computation. Unknown aggregate: " + selectPart);

                    if (!result)
                    {
                        // The test has already failed, no need to compute anymore.
                        testCompleted(test, TestResult.FAIL);
                        return true;
                    }
                }
            }
            finally
            {
                DBOperations.commit();
            }
            testCompleted(test, TestResult.PASS);
            return true;
        }
        catch (SQLException e)
        {
            logWarning("Cannot retrieve the cached results.", e);
        }
        return false;
    }

    private static String computeAttribute(String selectPart, String aggregate)
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String attribute =
            selectPart.substring((aggregate + "(").length(), selectPart.lastIndexOf(")"));
        return attribute;
    }

    @Override
    public void createTriggersForQuery(String testID, String query) throws Exception
    {
        triggerController_.createTriggersForQuery(testID, query);
    }

    @Override
    protected boolean hasWork()
    {
        return !testsToRun_.isEmpty();
    }

    @Override
    protected int getRemainingWork()
    {
        return testsToRun_.size();
    }

}
