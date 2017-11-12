package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jdt.annotation.NonNull;

// Made public so that query runner can run in the same java program as CDT.
public class DBConnection
{
    private Connection connection_;
    private Statement statement_;

    DBConnection(String databaseName, String username, String password) throws SQLException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Connection connection = DriverManager.getConnection(databaseName,
                                                                                               username,
                                                                                               password);
        connection_ = connection;

        /*
         * ATTENTION: this is what is causing the execute command to silently fail! Use db.commit()
         * for update statements to manually commit!
         */
        connection_.setAutoCommit(false); // apparently a requirement to set the fetch size of
                                          // the statement
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Statement statement = connection_.createStatement();
        statement_ = statement;
        statement_.setFetchSize(50); // restricting the fetch size of the result to avoid
                                     // memory errors
    }

    public void close() throws SQLException
    {
        connection_.close();
    }

    public PreparedStatement prepareStatement(String query) throws SQLException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings({"null"}) @NonNull PreparedStatement result = connection_.prepareStatement(query);
        return result;
    }

    public void commit() throws SQLException
    {
        connection_.commit();
    }

    /**
     * Executes the given query against the default DB
     * 
     * @param st the command to execute
     * @throws Exception
     */
    public ResultSet executeQuery(String query) throws SQLException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull ResultSet resultSet = statement_.executeQuery(query);
        return resultSet;
    }

    /**
     * Executes an update command
     * 
     * @param updateQuery the command to execute
     * @throws SQLException
     */
    public void executeUpdate(String updateQuery) throws SQLException
    {
        try
        {
            statement_.executeUpdate(updateQuery);
        }
        finally
        {
            commit();
        }
    }
}
