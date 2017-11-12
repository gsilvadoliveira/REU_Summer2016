package app;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.debug.Debug;

import database.DBInterface;
import database.DBOperations;

public class DataEntryTaskAnalyzer implements DataEntryTaskConstants
{
    public static final String ANALYSIS_TABLE = "Analysis";
    public static final String MISTAKES_TABLE = "Mistakes";
    public static final String CORRECTIONS_TABLE = "Corrections";
    public static final String ENTRIES_TABLE = "Entries";

    public static final String PRIZE_CODES_TABLE = "PrizeCodes";
    public static final String CELL_VALUE_CHANGES_TABLE = "CellValueChanges";
    public static final String USERS_TABLE_NAME = "Users";

    public static final String WORKER_ID_COLUMN_NAME = "workerid";
    public static final String COMPLETED_COLUMN_NAME = "completed";
    public static final String NO_MISTAKES_COLUMN_NAME = "nomistakes";
    public static final String NO_CORRECTED_MISTAKES_COLUMN_NAME = "nocorrectedmistakes";
    public static final String NO_SUBMITTED_MISTAKES_COLUMN_NAME = "nosubmittedmistakes";
    public static final String NO_ATTEMPTS_COLUMN_NAME = "noattempts";
    public static final String START_TIME_COLUMN_NAME = "starttime";
    public static final String END_TIME_COLUMN_NAME = "endtime";
    public static final String LOSS_TIME_COLUMN_NAME = "losstime";
    public static final String DURATION_COLUMN_NAME = "duration";

    public static final String CORRECT_VALUE_COLUMN_NAME = "correctvalue";
    public static final String PREVIOUS_VALUE_COLUMN_NAME = "previousvalue";
    public static final String VALUE_COLUMN_NAME = "value";
    public static final String SUBMITTED_COLUMN_NAME = "submitted";
    public static final String DECIMAL_MISTAKE_COLUMN_NAME = "decimalmistake";
    public static final String SIGN_MISTAKE_COLUMN_NAME = "signmistake";

    public static final String TIMESTAMP_COLUMN_NAME = "timestamp";

    public static final boolean FRESH_START = true;

    public static void main(String[] args) throws SQLException
    {
        DBInterface.getInstance().changeDatabase(args[0]);

        if (FRESH_START)
            dropTables();

        createTables();

        List<Integer> userIDs = getUsers();
        for (Integer userID: userIDs)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull Integer safeUserID = userID;
            System.out.println("Processing user #" + safeUserID);
            processUser(safeUserID);
        }

        System.out.println("Total number of workers: " + workers.size());
        System.out.println("Total number of entries: " + totalEntries);
        System.out.println("Process completed...");
    }

    private static void createTables() throws SQLException
    {
        createAnalysisTable();
        createMistakesTable();
        createCorrectionsTable();
        createEntriesTable();
    }

    public static int totalEntries = 0;
    private static HashSet<String> workers = new HashSet<>();

    private static void processUser(Integer userID) throws SQLException
    {
        ArrayList<Double> solution = getSolution(userID);
        ArrayList<Double> userData = new ArrayList<>(solution);
        boolean[] filledColumns = new boolean[solution.size()];

        Timestamp startTime = getStartTime(userID);
        Timestamp endTime = getEndTime(userID);
        String workerID = getWorkerID(userID);
        workers.add(workerID);
        String dataName = getDataName(userID);

        System.out.println("Worker id = " + workerID);
        System.out.println("Start time = " + startTime);
        System.out.println("End time = " + endTime);

        String query = "SELECT * " //
                       + "FROM " + CELL_VALUE_CHANGES_TABLE + " " //
                       + "WHERE " + USER_ID_COLUMN_NAME + " = " + userID + " " //
                       + "ORDER BY " + TIMESTAMP_COLUMN_NAME + " ASC";
        ArrayList<Integer> columnIndices = new ArrayList<>();
        ArrayList<Double> values = new ArrayList<>();
        ArrayList<Timestamp> timestamps = new ArrayList<>();

        try (ResultSet resultSet = DBOperations.executeQuery(query))
        {
            while (resultSet.next())
            {
                columnIndices.add(Integer.valueOf(resultSet.getInt(COLUMN_COLUMN_NAME)));
                values.add(Double.valueOf(resultSet.getDouble(VALUE_COLUMN_NAME)));
                timestamps.add(resultSet.getTimestamp(TIMESTAMP_COLUMN_NAME));
            }
        }
        finally
        {
            DBOperations.commit();
        }
        System.out.println("Retrieved analysis-related values.");

        int noAttempts = 0;
        Timestamp start = null;
        long totalLossTime = 0;
        for (int a = 0; a < columnIndices.size(); a++)
        {
            int columnIndex = columnIndices.get(a).intValue();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull Double value = values.get(a);
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull Timestamp timestamp = timestamps.get(a);

            columnIndex--;
            if (columnIndex == -1)
            {
                // Focus out
                if (value.doubleValue() == 0)
                    start = timestamp;
                // Focus in
                else if (value.doubleValue() == 1)
                {
                    if (start != null)
                    {
                        totalLossTime += timestamp.getTime() - start.getTime();
                        start = null;
                    }
                }
                else if (value.doubleValue() == 2)
                    noAttempts++;
            }
            else
            {
                totalEntries++;
                filledColumns[columnIndex] = true;

                Double correctValue = solution.get(columnIndex);
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull Double previousValue = userData.get(columnIndex);
                boolean beforeMistake = !correctValue.equals(previousValue);
                userData.set(columnIndex, value);
                boolean afterMistake = !correctValue.equals(userData.get(columnIndex));

                // Figure out whether this cell has been modified in the future ever again.
                boolean submitted = true;
                for (int b = a + 1; b < columnIndices.size(); b++)
                {
                    int futureColumn = columnIndices.get(b).intValue() - 1;
                    if (futureColumn == columnIndex)
                        submitted = false;
                }

                insertEntry(userID, workerID, dataName, value, (columnIndex + 1), submitted,
                            timestamp);

                if (afterMistake)
                {
                    // Entry is a mistake
                    insertMistake(userID, workerID, dataName, correctValue, value,
                                  (columnIndex + 1), submitted, timestamp);

                }
                else if (beforeMistake)
                    // Entry is a correction.
                    insertCorrection(userID, workerID, dataName, previousValue, correctValue,
                                     (columnIndex + 1), timestamp);
            }
        }

        ArrayList<Integer> missingColumns = new ArrayList<>();
        for (int a = 0; a < filledColumns.length; a++)
        {
            if (!filledColumns[a])
                missingColumns.add(Integer.valueOf(a));
        }

        if (missingColumns.size() != 0)
            System.err.println("User #" + userID + " (worker = " + workerID
                               + ") submitted the task without filling the following columns: "
                               + Debug.join(missingColumns, ", "));

        long duration = endTime.getTime() - startTime.getTime() - totalLossTime;
        insertUserData(userID, workerID, dataName, noAttempts, startTime, endTime,
                       new Date(duration), new Date(totalLossTime));
    }

    private static String getDataName(Integer userID) throws SQLException
    {
        // @formatter:off
        String query = "SELECT " + DATA_NAME_COLUMN_NAME + " "
                        + "FROM " + DATA_SOURCES_TABLE_NAME + ", " + TASKS_TABLE_NAME + " "
                        + "WHERE " + TASKS_TABLE_NAME + "." + USER_ID_COLUMN_NAME + " = " + userID + " "
                            + " AND " + DATA_SOURCES_TABLE_NAME + "." + DATA_ID_COLUMN_NAME 
                                + " = " + TASKS_TABLE_NAME + "." + DATA_ID_COLUMN_NAME; 
        // @formatter:on
        List<String> result = DBOperations.executeSingleStringProjectionQuery(query,
                                                                              DATA_NAME_COLUMN_NAME);
        // Suppressed due to complex program logic.
        @SuppressWarnings("null") @NonNull String res = result.get(0);
        return res;
    }

    private static String getWorkerID(Integer userID) throws SQLException
    {
        // @formatter:off
        String query = "SELECT " + WORKER_ID_COLUMN_NAME + " "
                        + "FROM " + PRIZE_CODES_TABLE + " "
                        + "WHERE " + USER_ID_COLUMN_NAME + " = " + userID; 
        // @formatter:on
        List<String> result = DBOperations.executeSingleStringProjectionQuery(query,
                                                                              WORKER_ID_COLUMN_NAME);
        // Suppressed due to complex program logic.
        @SuppressWarnings("null") @NonNull String res = result.get(0);
        return res;
    }

    private static Timestamp getEndTime(Integer userID) throws SQLException
    {
        // @formatter:off
        String query = "SELECT " + TIMESTAMP_COLUMN_NAME + " "
                        + "FROM " + PRIZE_CODES_TABLE + " "
                        + "WHERE " + USER_ID_COLUMN_NAME + " = " + userID; 
        // @formatter:on
        List<Timestamp> result = DBOperations.executeSingleTimestampProjectionQuery(query,
                                                                                    TIMESTAMP_COLUMN_NAME);
        // Suppressed due to complex program logic.
        @SuppressWarnings("null") @NonNull Timestamp res = result.get(0);
        return res;
    }

    private static Timestamp getStartTime(Integer userID) throws SQLException
    {
        // @formatter:off
        String query = "SELECT " + TIMESTAMP_COLUMN_NAME + " "
                        + "FROM " + USERS_TABLE_NAME + " "
                        + "WHERE " + USER_ID_COLUMN_NAME + " = " + userID; 
        // @formatter:on
        List<Timestamp> result = DBOperations.executeSingleTimestampProjectionQuery(query,
                                                                                    TIMESTAMP_COLUMN_NAME);
        // Suppressed due to complex program logic.
        @SuppressWarnings("null") @NonNull Timestamp res = result.get(0);
        return res;
    }

    private static ArrayList<Double> getSolution(Integer userID) throws SQLException
    {
        // @formatter:off
        String query = "SELECT " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_1 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_2 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_3 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_4 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_5 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_6 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_7 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_8 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_9 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_10 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_11 "
                        + ", " + DATA_TABLE_NAME + "." + COLUMN_COLUMN_NAME + "_12 "
                        + "FROM " + DATA_TABLE_NAME + ", " + TASKS_TABLE_NAME + " "
                        + "WHERE " + DATA_TABLE_NAME + "." + ROW_COLUMN_NAME + " = " 
                            + TASKS_TABLE_NAME + "." + ROW_COLUMN_NAME + " "
                        +   "AND " + DATA_TABLE_NAME + "." + DATA_ID_COLUMN_NAME + " = " 
                            + TASKS_TABLE_NAME + "." + DATA_ID_COLUMN_NAME + " "
                        +   "AND " + TASKS_TABLE_NAME + "." + USER_ID_COLUMN_NAME + " = " + userID; 
        // @formatter:on
        ArrayList<Double> result = new ArrayList<>();
        try (ResultSet resultSet = DBOperations.executeQuery(query))
        {
            if (!resultSet.next())
                throw new RuntimeException("Cannot find solution for user id = " + userID
                                           + ". Query = " + query);

            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_1")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_2")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_3")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_4")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_5")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_6")));
            result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_7")));

            if (resultSet.getObject(COLUMN_COLUMN_NAME + "_8") != null)
                result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_8")));
            if (resultSet.getObject(COLUMN_COLUMN_NAME + "_9") != null)
                result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_9")));
            if (resultSet.getObject(COLUMN_COLUMN_NAME + "_10") != null)
                result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_10")));
            if (resultSet.getObject(COLUMN_COLUMN_NAME + "_11") != null)
                result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_11")));
            if (resultSet.getObject(COLUMN_COLUMN_NAME + "_12") != null)
                result.add(Double.valueOf(resultSet.getDouble(COLUMN_COLUMN_NAME + "_12")));
        }
        finally
        {
            DBOperations.commit();
        }
        return result;
    }

    private static List<Integer> getUsers() throws SQLException
    {
        String query = "SELECT " + USER_ID_COLUMN_NAME + " " //
                       + "FROM " + PRIZE_CODES_TABLE + " ";

        return DBOperations.executeSingleIntegerProjectionQuery(query, USER_ID_COLUMN_NAME);
    }

    private static void insertCorrection(Integer userID, String workerID, String dataName,
                                         Double previousValue, Double correctValue, int column,
                                         Timestamp timestamp) throws SQLException
    {
        // @formatter:off
        String query = "INSERT INTO " + CORRECTIONS_TABLE + " "
                        + "VALUES ( " 
                        + userID 
                        + ", " + quote(workerID)
                        + ", " + quote(dataName)
                        + ", " + previousValue
                        + ", " + correctValue
                        + ", " + column
                        + ", " + quote(timestamp)
                        + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void createCorrectionsTable() throws SQLException
    {
        // @formatter:off
        String query = "CREATE TABLE " + CORRECTIONS_TABLE + " "
                       + "( "
                       + doubleQuote(USER_ID_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(WORKER_ID_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(DATA_NAME_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(PREVIOUS_VALUE_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(CORRECT_VALUE_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(COLUMN_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(TIMESTAMP_COLUMN_NAME) + " timestamp with time zone not null "
                       + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void insertMistake(Integer userID, String workerID, String dataName,
                                      Double correctValue, Double value, int column,
                                      boolean submitted, Timestamp timestamp) throws SQLException
    {
        double correctValueDouble = correctValue.doubleValue();
        double valueDouble = value.doubleValue();

        // compute the type of the mistake...
        boolean decimalMistake = isDecimalMistake(correctValueDouble, valueDouble);
        boolean signMistake = isSignMistake(correctValueDouble, valueDouble);
        if (isDecimalMistake(-correctValueDouble, valueDouble))
        {
            decimalMistake = true;
            signMistake = true;
        }

        // @formatter:off
        String query = "INSERT INTO " + MISTAKES_TABLE + " "
                        + "VALUES ( " 
                        + userID 
                        + ", " + quote(workerID)
                        + ", " + quote(dataName)
                        + ", " + correctValue
                        + ", " + value
                        + ", " + column
                        + ", " + toBit(submitted)
                        + ", " + quote(timestamp)
                        + ", " + toBit(decimalMistake)
                        + ", " + toBit(signMistake)
                        + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void insertEntry(Integer userID, String workerID, String dataName, Double value,
                                    int column, boolean submitted, Timestamp timestamp)
        throws SQLException
    {
        // @formatter:off
        String query = "INSERT INTO " + ENTRIES_TABLE + " "
                        + "VALUES ( " 
                        + userID 
                        + ", " + quote(workerID)
                        + ", " + quote(dataName)
                        + ", " + value
                        + ", " + column
                        + ", " + toBit(submitted)
                        + ", " + quote(timestamp)
                        + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static String toBit(boolean value)
    {
        return (value ? "1" : "0") + "::bit";
    }

    private static boolean isSignMistake(double value1, double value2)
    {
        return value1 == -value2;
    }

    private static boolean isDecimalMistake(double value1, double value2)
    {
        if ((value1 < 0 && value2 > 0) || (value1 > 0 && value2 < 0))
            return false;

        double first = Math.abs(value1);
        double second = Math.abs(value2);
        double small = first;
        double large = second;
        if (small > large)
        {
            small = second;
            large = first;
        }

        double temp = small;
        while (temp < large)
            temp *= 10;
        return temp == large;
    }

    private static void createEntriesTable() throws SQLException
    {
        // @formatter:off
        String query = "CREATE TABLE " + ENTRIES_TABLE + " "
                       + "( "
                       + doubleQuote(USER_ID_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(WORKER_ID_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(DATA_NAME_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(VALUE_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(COLUMN_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(SUBMITTED_COLUMN_NAME) + " bit "
                       + ", " + doubleQuote(TIMESTAMP_COLUMN_NAME) + " timestamp with time zone not null "
                       + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void createMistakesTable() throws SQLException
    {
        // @formatter:off
        String query = "CREATE TABLE " + MISTAKES_TABLE + " "
                       + "( "
                       + doubleQuote(USER_ID_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(WORKER_ID_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(DATA_NAME_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(CORRECT_VALUE_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(VALUE_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(COLUMN_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(SUBMITTED_COLUMN_NAME) + " bit "
                       + ", " + doubleQuote(TIMESTAMP_COLUMN_NAME) + " timestamp with time zone not null "
                       + ", " + doubleQuote(DECIMAL_MISTAKE_COLUMN_NAME) + " bit "
                       + ", " + doubleQuote(SIGN_MISTAKE_COLUMN_NAME) + " bit "
                       + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void insertUserData(Integer userID, String workerID, String dataName,
                                       int noAttempts, Timestamp startTime, Timestamp endTime,
                                       Date duration, Date lossTime) throws SQLException
    {
        // @formatter:off
        String query = "INSERT INTO " + ANALYSIS_TABLE + " "
                        + "VALUES ( " 
                        + userID 
                        + ", " + quote(workerID)
                        + ", " + quote(dataName)
                        + ", " + noAttempts
                        + ", " + quote(startTime)
                        + ", " + quote(endTime)
                        + ", " + duration.getTime()
                        + ", " + lossTime.getTime()
                        + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static void createAnalysisTable() throws SQLException
    {
        // @formatter:off
        String query = "CREATE TABLE " + ANALYSIS_TABLE + " "
                       + "( "
                       + doubleQuote(USER_ID_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(WORKER_ID_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(DATA_NAME_COLUMN_NAME) + " varchar(200) "
                       + ", " + doubleQuote(NO_ATTEMPTS_COLUMN_NAME) + " int "
                       + ", " + doubleQuote(START_TIME_COLUMN_NAME) + " timestamp with time zone not null "
                       + ", " + doubleQuote(END_TIME_COLUMN_NAME) + " timestamp with time zone not null "
                       + ", " + doubleQuote(DURATION_COLUMN_NAME) + " numeric "
                       + ", " + doubleQuote(LOSS_TIME_COLUMN_NAME) + " numeric "
                       + ")";
        // @formatter:on
        DBOperations.executeUpdate(query);
    }

    private static String doubleQuote(String value)
    {
        return "\"" + value + "\"";
    }

    private static String quote(Object value)
    {
        return "'" + value + "'";
    }

    private static void dropTables() throws SQLException
    {
        DBOperations.dropTable(ANALYSIS_TABLE, true);
        DBOperations.dropTable(MISTAKES_TABLE, true);
        DBOperations.dropTable(CORRECTIONS_TABLE, true);
        DBOperations.dropTable(ENTRIES_TABLE, true);
    }
}
