package conTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import testing.Test;
import testing.TestResult;
import app.DataEntryTaskConstants;
import database.DBInterface;
import database.DBOperations;

public class CDTDataEntryTask extends CDTBase implements DataEntryTaskConstants
{
    public static String FORMAT_DATE_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";

    // @formatter:off
    private static final String GET_ACTIVE_USERS_QUERY =
        "SELECT " + USER_ID_COLUMN_NAME + " " + 
        "FROM " + ACTIVE_USERS_TABLE_NAME;    
    
    private static final String GET_NUMBER_OF_DATA_COLUMNS_QUERY =
        "SELECT COUNT(*) " + 
        "FROM information_schema.columns " +
        "WHERE table_name = " + "'" + DATA_TABLE_NAME.toLowerCase() + "'" + " " +
            "AND column_name LIKE " + "'" + COLUMN_COLUMN_NAME + "_%" + "'";
    // @formatter:on 

    static
    {
        CDTBase.CLEAN_EXECUTION = false;
    }

    private static boolean DEBUG_TEST_PARSING = true;
    private static final boolean DEBUG_TEST_SKIP = false;

    int noDataColumns_ = 0;
    private List<String> columns_ = new ArrayList<>();

    private HashMap<Integer, UserData> userData_ = new HashMap<>();

    private class UserData
    {
        private double[] entries_;
        private final HashMap<DataEntryTaskTest, Boolean> testResults_;

        UserData()
        {
            entries_ = new double[noDataColumns_];
            testResults_ = new HashMap<>();
            for (DataEntryTaskTest test: tests_)
                testResults_.put(test, Boolean.TRUE);
        }

        boolean updateTestResults(DataEntryTaskTest test, boolean result)
        {
            boolean previousResult = testResults_.get(test).booleanValue();
            if (previousResult != result)
            {
                testResults_.put(test, Boolean.valueOf(result));
                return true;
            }
            return false;
        }

        String getAlertMessage()
        {
            String message = "";
            for (DataEntryTaskTest test: testResults_.keySet())
            {
                if (!testResults_.get(test).booleanValue())
                    message += test.getFailureMessage() + ",";
            }
            if (message.endsWith(","))
                message = message.substring(0, message.length() - ",".length());
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String result = message;
            return result;
        }

        boolean updateEntries(double[] entries)
        {
            for (int a = 0; a < entries_.length; a++)
            {
                if (entries_[a] != entries[a])
                {
                    entries_ = entries;
                    return true;
                }
            }
            return false;
        }
    }

    CDTDataEntryTask()
    {}

    final ArrayList<DataEntryTaskTest> tests_ = new ArrayList<>();

    private void readDataEntryTaskTests()
    {
        Properties properties = SharedOperations.loadConfiguration();
        String testFilePath = SharedOperations.loadNonNullProperty(properties, "cdtTestFilePath");
        try
        {
            ArrayList<String> testDescriptions = SharedOperations.readQueries(new File(testFilePath));
            for (String testDescription: testDescriptions)
            {
                if (testDescription.startsWith("META:"))
                {
                    // Suppressed due to missing library annotations.
                    @SuppressWarnings("null") @NonNull String metaDescription = testDescription.substring("META:".length())
                                                                                               .trim();
                    DataEntryTaskMetaTest metaTest = DataEntryTaskMetaTest.parse(metaDescription);
                    tests_.addAll(metaTest.generate());
                }
                else
                    tests_.add(DataEntryTaskTest.parse(testDescription));
            }
        }
        catch (FileNotFoundException e)
        {
            logWarning("Test file: " + testFilePath + " does not exist.", e);
        }

        if (DEBUG_TEST_PARSING)
        {
            logInfo("Parsed the following tests queries:");
            for (DataEntryTaskTest test: tests_)
                logInfo(test.toString());
        }
    }

    @Override
    public void shutdown()
    {
        logInfo("CDT Data Entry task completed with success...");

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
        // Read the tests.
        readDataEntryTaskTests();

        // Get the number of columns in the Data table.
        try (ResultSet resultSet = DBOperations.executeQuery(GET_NUMBER_OF_DATA_COLUMNS_QUERY))
        {
            if (resultSet.next())
                // Note: The column indices start with '1'. This is documented, but still what the
                // heck :-)
                noDataColumns_ = resultSet.getInt(1);
            DBOperations.commit();
        }
        catch (SQLException e)
        {
            String message = "Cannot get the number of columns in " + DATA_TABLE_NAME
                             + " table. Query: " + GET_NUMBER_OF_DATA_COLUMNS_QUERY + ".";
            throw new RuntimeException(message, e);
        }

        // Create column names
        for (int a = 0; a < noDataColumns_; a++)
            columns_.add(COLUMN_COLUMN_NAME + "_" + (a + 1));
    }

    private @Nullable
    List<Integer> getActiveUsers()
    {
        List<Integer> result = null;
        try
        {
            result = DBOperations.executeSingleIntegerProjectionQuery(GET_ACTIVE_USERS_QUERY,
                                                                      USER_ID_COLUMN_NAME);
        }
        catch (SQLException e)
        {
            logWarning("Cannot get active users.", e);
        }
        return result;
    }

    @Override
    protected void runInternal()
    {
        // Get active users.
        List<Integer> users = getActiveUsers();
        if (users == null)
            return;

        // Update temporary tables and user data.
        removeObsoleteTempTables(users);
        updateUserData(users);

        // Go over each user and run tests for that user.
        for (Integer userID: users)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull Integer safeID = userID;
            // Check whether the user has changed any data at all since the last time we have run
            // the tests. If not, there is no need to run the tests for this user.
            if (!shallRunTests(safeID))
                continue;
            runTests(safeID);
        }
    }

    private void updateUserData(List<Integer> users)
    {
        ArrayList<Integer> existingUsers = new ArrayList<>(userData_.keySet());
        for (Integer existingUser: existingUsers)
        {
            if (!users.contains(existingUser))
                userData_.remove(existingUser);
        }

        for (Integer userID: users)
        {
            if (!userData_.containsKey(userID))
                userData_.put(userID, new UserData());
        }
    }

    private void runTests(Integer userID)
    {
        try
        {
            String tempView = TEMP_VIEW_PREFIX + userID;
            for (DataEntryTaskTest test: tests_)
            {
                // Check whether the test should be run for this user or not.
                // @formatter:off
                String query = 
                    "SELECT " + DATA_SOURCES_TABLE_NAME + "." + DATA_ID_COLUMN_NAME + ", " + DATA_NAME_COLUMN_NAME + " " +
                    "FROM " + DATA_SOURCES_TABLE_NAME + ", " + TASKS_TABLE_NAME + " " +
                    "WHERE " + DATA_SOURCES_TABLE_NAME + "." + DATA_ID_COLUMN_NAME + " = " + TASKS_TABLE_NAME + "." + DATA_ID_COLUMN_NAME + " " +
                        " AND " + TASKS_TABLE_NAME + "." + USER_ID_COLUMN_NAME + " = " + userID;
                // @formatter:on
                try (ResultSet resultSet = DBOperations.executeQuery(query))
                {
                    if (resultSet.next())
                    {
                        int dataID = resultSet.getInt(DATA_ID_COLUMN_NAME);
                        String dataName = resultSet.getString(DATA_NAME_COLUMN_NAME);

                        if (test.getDataSourceName().equals(dataName))
                        {
                            try
                            {
                                boolean testResult = test.execute(DATA_TABLE_NAME, dataID, tempView);
                                UserData userData = userData_.get(userID);
                                if (userData.updateTestResults(test, testResult))
                                    reportAlert(userID.intValue(), userData.getAlertMessage());
                            }
                            catch (SQLException e)
                            {
                                logWarning("Cannot execute test: " + test + " for user: " + userID,
                                           e);
                            }
                        }
                        else
                        {
                            if (DEBUG_TEST_SKIP)
                                System.out.println("Skipping test: " + test
                                                   + " since it is not defined for user #" + userID);
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            logWarning("Cannot update the temp table for user: " + userID, e);
        }
        finally
        {
            try
            {
                DBOperations.commit();
            }
            catch (SQLException e)
            {
                logWarning("Cannot commit.", e);
            }
        }
    }

    private void removeObsoleteTempTables(List<Integer> users)
    {
        // Go over the existing temp tables and drop the ones that are no longer needed.
        List<String> tempTables = null;
        try
        {
            tempTables = DBOperations.getTablesStartingWith(TEMP_TABLE_PREFIX);
            for (String tempTable: tempTables)
            {
                int userID = Integer.parseInt(tempTable.substring(TEMP_TABLE_PREFIX.length()));
                if (!users.contains(Integer.valueOf(userID)))
                {
                    // This user is not active, drop the temp table with CASCADE.
                    DBOperations.dropTable(tempTable, true);
                }
            }
        }
        catch (SQLException e)
        {
            logWarning("Cannot get temp tables.", e);
        }
    }

    private boolean shallRunTests(Integer userID)
    {
        String tempTable = TEMP_TABLE_PREFIX + userID;
        String tempView = TEMP_VIEW_PREFIX + userID;
        // Make sure that the temp table and view exists.
        try
        {
            if (DBOperations.getTablesStartingWith(tempTable).isEmpty()
                || DBOperations.getTablesStartingWith(tempView).isEmpty())
                return false;
        }
        catch (SQLException e)
        {
            logWarning("Cannot check whether the temp table is created for user #" + userID
                       + " or not.", e);
        }

        // @formatter:off
        String query = 
            "SELECT * " +
            "FROM " + tempTable; 
        // @formatter:on

        // Get current user data.
        double[] currentUserData = new double[noDataColumns_];
        try (ResultSet resultSet = DBOperations.executeQuery(query))
        {
            if (resultSet.next())
            {
                for (int a = 0; a < noDataColumns_; a++)
                {
                    String columnName = COLUMN_COLUMN_NAME + "_" + (a + 1);
                    currentUserData[a] = resultSet.getDouble(columnName);
                }
            }
            DBOperations.commit();
        }
        catch (SQLException e)
        {
            logWarning("Cannot retrieve user's current data. Query: " + query, e);
            return true;
        }

        return userData_.get(userID).updateEntries(currentUserData);
    }

    private void reportAlert(int userID, String message)
    {
        logInfo("Reporting test failure: " + message + " for user: " + userID);

        Date now = new Date();

        TimeZone timeZone = TimeZone.getDefault();
        DateFormat dateFormat = new SimpleDateFormat(FORMAT_DATE_ISO);
        dateFormat.setTimeZone(timeZone);
        String dateString = dateFormat.format(now);

        // @formatter:off
        String query =
            "INSERT INTO " + ALERTS_TABLE_NAME + 
            " VALUES (" + userID + ", " + "'" + message + "'" + ", " + "'" + dateString + "'" + ")";
        // @formatter:on
        try
        {
            DBOperations.executeUpdate(query);
        }
        catch (SQLException e)
        {
            logWarning("Cannot insert an alert for user: " + userID + ", message = " + message
                       + ". Query: " + query + ".", e);
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
        // We use the naive approach for the data entry task, there is nothing to schedule.
        // The method becomes noop.
    }

    @Override
    public void testInitialized(Test test)
    {
        // For the data entry task, we use special tests. This method becomes noop.
    }

    @Override
    public void testInitialized(Test test, Throwable e)
    {
        // For the data entry task, we use special tests. This method becomes noop.
    }

    @Override
    public void createTriggersForQuery(String testID, String testQuery) throws Exception
    {
        // For the data entry task, we don't create triggers. This method becomes noop.
    }

    @Override
    public void testStartedInternal(Test test)
    {
        // For the data entry task, we use special tests. This method becomes noop.
    }

    @Override
    public void testCompletedInternal(Test test, TestResult result)
    {
        // For the data entry task, we use special tests. This method becomes noop.
    }

    @Override
    public void testCompletedInternal(Test test, TestResult result, SQLException e)
    {
        // For the data entry task, we use special tests. This method becomes noop.
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
