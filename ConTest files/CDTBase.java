package conTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import testing.Test;
import testing.TestResult;
import app.CDTApp;
import app.ExperimentRunner;
import app.CDTApp.ExperimentType;

import com.kivancmuslu.www.arrays.ArrayOperations;
import com.kivancmuslu.www.arrays.MapOperation;
import com.kivancmuslu.www.concurrent.BoolConditional;
import com.kivancmuslu.www.debug.Debug;
import com.kivancmuslu.www.log.FileLogger;

import database.DBOperations;

public abstract class CDTBase implements Runnable, SharedConstants
{
    protected static final long EXECUTION_DELAY = 0;
    static boolean CLEAN_EXECUTION = true;

    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") private static final String LOG_IDENTIFIER = CDT.class.getName();

    private static CDTBase instance_ = initialize();

    public synchronized static void reinitialize(@Nullable ExperimentType experimentType)
    {
        if (experimentType != null)
            experimentType.setup();

        counter_++;
        instance_ = initialize();
    }

    private synchronized static CDTBase initialize()
    {
        System.out.println("CDT Base: app type = " + CDTApp.APP_TYPE);
        System.out.println("CDT Base: log path = " + CDTApp.LOG_PATH);

        switch (CDTApp.APP_TYPE)
        {
            case NAIVE:
                return new CDTNaive();
            case NORMAL:
                return new CDT();
            case TASK:
                return new CDTDataEntryTask();
            default:
                throw new RuntimeException("Unknown CDT Type: " + CDTApp.APP_TYPE
                                           + ", cannot initialize.");
        }
    }

    private final FileLogger logger_;

    private volatile boolean alive_;
    private long noTriggerActivations_;
    private long noTestsRun_;

    public static int counter_ = 0;

    private final BoolConditional initializationCondition_ = new BoolConditional(false);

    public synchronized static CDTBase getInstance()
    {
        return instance_;
    }

    private static final int getCounter()
    {
        return counter_;
    }

    protected CDTBase()
    {
        logger_ = new FileLogger(LOG_IDENTIFIER + "_" + getCounter(), CDTApp.LOG_PATH);
        logger_.disableConsoleLogging();

        alive_ = true;
        noTriggerActivations_ = 0;
        noTestsRun_ = 0;
    }

    protected boolean isAlive()
    {
        return alive_;
    }

    @Override
    public void run()
    {
        preInit();
        init();
        postInit();
        initializationCondition_.signalAll();

        while (alive_ || (ExperimentRunner.PROFILE && hasWork()))
            runInternal();

        shutdown();

        printStatistics();
    }

    protected abstract boolean hasWork();

    public void waitUntilInitialization() throws InterruptedException
    {
        initializationCondition_.await();
    }

    private void printStatistics()
    {
        logInfo("Statistics:");
        long noTriggerActivations;
        synchronized (this)
        {
            noTriggerActivations = noTriggerActivations_;
        }
        logInfo("Number of trigger activations: " + noTriggerActivations);
        logInfo("Number of tests run: " + noTestsRun_);
        logInfo("Remaining tests in the queue: " + getRemainingWork());
    }

    protected abstract int getRemainingWork();

    protected void postInit()
    {
        // Default implementation is empty.
    }

    public void kill()
    {
        logInfo("CDT is being killed.");
        alive_ = false;
    }

    protected void shutdown()
    {
        // Default implementation is empty.
    }

    protected abstract void runInternal();

    private void init()
    {
        if (CLEAN_EXECUTION)
            clearTestTables();
    }

    private void clearTestTables()
    {
        try
        {
            List<String> testTables = DBOperations.getTablesStartingWith(CDT_PREFIX);
            if (!testTables.isEmpty())
                logInfo("Dropping test tables: " + Debug.join(testTables, ", "));
            for (String testTable: testTables)
            {
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String safeTestTable = testTable;
                DBOperations.dropTable(safeTestTable);
            }
        }
        catch (SQLException e)
        {
            logWarning("Cannot drop the test tables.");
        }
    }

    protected void preInit()
    {
        // Default implementation is empty.
    }

    public abstract Test[] getInitializedTests();

    public abstract void scheduleTest(Test test);

    public final void logInfo(String message)
    {
        logger_.logInfo(message);
    }

    protected final void logInfo(String message, Throwable e)
    {
        logger_.logInfo(message, e);
    }

    protected final void logWarning(String message, Throwable e)
    {
        logger_.logWarning(message, e);
    }

    protected final void logWarning(String message)
    {
        logger_.logWarning(message);
    }

    protected final void logInterruptedException(String event, InterruptedException e)
    {
        logger_.logInterruptedException(event, e);
    }

    public final void triggerActivated(HashMap<String, Object> triggerData)
    {
        synchronized (this)
        {
            noTriggerActivations_++;
        }
        triggerActivatedInternal(triggerData);
    }

    /**
     * @param triggerData
     */
    protected void triggerActivatedInternal(HashMap<String, Object> triggerData)
    {
        // Default implementation is empty.
    }

    public abstract void scheduleTestWithID(String testID, Map<String, Object> oldData, Map<String, Object> newData);

    public abstract void testInitialized(Test test);

    public abstract void testInitialized(Test test, Throwable e);

    public abstract void createTriggersForQuery(String testID, String testQuery) throws Exception;

    public final void testStarted(Test test)
    {
        timestampTest("START", test);
        testStartedInternal(test);
    }

    /**
     * @param test
     */
    protected void testStartedInternal(Test test)
    {
        // The default implementation is empty.
    }

    public final void testCompleted(Test test, TestResult result)
    {
        logTestCompleted(test, result, null);
        testCompletedInternal(test, result);
    }

    protected void timestamp(String event, String information)
    {
        logInfo("!!! " + SharedOperations.timestamp() + " @ " + event + " @ " + information);
    }

    protected void timestampTest(String event, Test test)
    {
        timestamp(event, SharedOperations.toOneLine(test.getTestQuery()));
    }

    private void logTestCompleted(Test test, TestResult result, @Nullable SQLException error)
    {
        noTestsRun_++;
        switch (result)
        {
            case PASS:
                timestampTest("SUCCESS", test);
                break;
            case FAIL:
                timestampTest("FAIL", test);
                break;
            case ERROR:
                if (error != null)
                    logWarning("Cannot run " + test, error);
                else
                    logWarning("Cannot run " + test);
                break;
            default:
                throw new RuntimeException("Unreachable code.");
        }
    }

    /**
     * @param test
     * @param result
     */
    protected void testCompletedInternal(Test test, TestResult result)
    {
        // The default implementation is empty.
    }

    public final void testCompleted(Test test, TestResult result, SQLException e)
    {
        logTestCompleted(test, result, e);
        testCompletedInternal(test, result, e);
    }

    /**
     * @param test
     * @param result
     * @param e
     */
    protected void testCompletedInternal(Test test, TestResult result, SQLException e)
    {
        // The default implementation is empty.
    }

    protected static ArrayList<Test> readTests() throws FileNotFoundException
    {
        Properties config = SharedOperations.loadConfiguration();
        String testFilePath = config.getProperty(CDT_TEST_FILE_PATH);
        File testFile = new File(testFilePath);

        ArrayList<String> testDescriptions = SharedOperations.readQueries(testFile);
        ArrayList<Test> tests = ArrayOperations.map(testDescriptions,
                                                    new MapOperation<String, Test>()
                                                    {
                                                        @Override
                                                        public Test map(String testDescription)
                                                        {
                                                            return new Test(testDescription);
                                                        }
                                                    });
        return tests;
    }
}
