package conTest;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import testing.Test;
import database.DBInterface;

public class CDTNaive extends CDTBase
{
    private List<Test> tests_;

    // Suppressed due to complex program logic.
    @SuppressWarnings("null")
    CDTNaive()
    {}

    @Override
    public void shutdown()
    {
        logInfo("CDT Naive completed with success...");

        try
        {
            DBInterface.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void postInit()
    {
        try
        {
            tests_ = readTests();
            for (Test test: tests_)
            {
                Runnable initializationJob = test.createInitializationJob();
                initializationJob.run();
            }
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("Cannot find tests file.", e);
        }
    }

    @Override
    protected void runInternal()
    {
        for (Test test: tests_)
        {
            logInfo("!!! " + SharedOperations.timestamp() + " @ "
                    + SharedOperations.toOneLine(test.getTestQuery()));
            test.run();
            try
            {
                Thread.sleep(EXECUTION_DELAY);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if (!isAlive())
                break;
        }
    }

    @Override
    public Test[] getInitializedTests()
    {
        throw new RuntimeException("Functionality not supported.");
    }

    @Override
    public void scheduleTest(Test safeTest)
    {
        throw new RuntimeException("Functionality not supported.");
    }

    @Override
    public void scheduleTestWithID(String testID, Map<String, Object> oldData, Map<String, Object> newData)
    {
        // The default implementation is empty.
    }

    @Override
    public void testInitialized(Test test)
    {
        logInfo(test + " initialized.");
    }

    @Override
    public void testInitialized(Test test, Throwable e)
    {
        logWarning(test + " cannot be initialized.", e);
    }

    @Override
    public void createTriggersForQuery(String testID, String testQuery)
    {
        // The default implementation is empty.
    }

    @Override
    protected boolean hasWork()
    {
        return false;
    }
    
    @Override
    protected int getRemainingWork()
    {
        return 0;
    }
}
