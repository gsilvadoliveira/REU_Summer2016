package conTest;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jdt.annotation.NonNull;

import app.DataEntryTaskConstants;
import database.DBOperations;

public class DataEntryTaskTest implements DataEntryTaskConstants
{
    private final String query_;
    private final String dataSourceName_;
    private final double epsilon_;
    private final String failureMessage_;

    public static final double DOUBLE_ERROR_RATE = 0.0;

    public static final boolean DEBUG_QUERY_RESULTS = false;

    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") @NonNull public static final String LS = System.lineSeparator();
    // @formatter:off 
    public static final String USAGE =
        "Test query must be in the following format: " + LS
            + "<sql_query> @ <data_source_name>(string) @ <epsilon_value>(double) @ <failure_message>(string)" + LS
            + "Use \"Table\" in the sql query, which will be replaced by "
            + "expected and actual hardcoded table names before running the tests.";
    // @formatter:on

    DataEntryTaskTest(String query, String dataSourceName, double epsilon, String failureMessage)
    {
        query_ = query;
        dataSourceName_ = dataSourceName;
        epsilon_ = epsilon;
        failureMessage_ = failureMessage;
    }

    public String getDataSourceName()
    {
        return dataSourceName_;
    }

    public String getFailureMessage()
    {
        return failureMessage_;
    }

    public boolean execute(String table1, int dataID, String table2) throws SQLException
    {
        // @formatter:off
        String userData = 
            "(" + 
                "SELECT * " + 
                "FROM " + table1 + " " + 
                "WHERE " + DATA_ID_COLUMN_NAME + " = " + dataID + 
            ") AS UserData";
        // @formatter:on
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String query1 = query_.replace("Table", userData);

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String query2 = query_.replace("Table", table2);
        double result1 = getQueryResult(query1);
        if (DEBUG_QUERY_RESULTS)
            System.out.println("Executed query: " + query1 + "." + LS + "Result: " + result1);
        double result2 = getQueryResult(query2);
        if (DEBUG_QUERY_RESULTS)
            System.out.println("Executed query: " + query2 + "." + LS + "Result: " + result2);
        double resultDiff = Math.abs(result1 - result2);
        if (DEBUG_QUERY_RESULTS)
            System.out.println("Result diff: " + resultDiff + LS);
        return resultDiff <= epsilon_ + DOUBLE_ERROR_RATE;
    }

    private static double getQueryResult(String query) throws SQLException
    {
        try (ResultSet resultSet = DBOperations.executeQuery(query))
        {
            if (resultSet.next())
                return resultSet.getDouble(1);
        }
        return Double.NaN;
    }

    public static DataEntryTaskTest parse(String testQuery)
    {
        String[] parts = testQuery.split("\\@");
        if (parts.length != 4)
            throw new RuntimeException(USAGE + LS + "Test query: '" + testQuery + "'");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String query = parts[0].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String dataSourceName = parts[1].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String failureMessage = parts[3].trim();
        try
        {
            double epsilon = Double.parseDouble(parts[2].trim());
            return new DataEntryTaskTest(query, dataSourceName, epsilon, failureMessage);
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException(USAGE + LS + "Test query: '" + testQuery + "'", e);
        }
    }

    @Override
    public String toString()
    {
        return query_ + " @ " + dataSourceName_ + " @ " + epsilon_ + " @ " + failureMessage_;
    }
}
