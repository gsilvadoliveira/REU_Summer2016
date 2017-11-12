package database;

import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import conTest.SharedOperations;

public final class DBInterface
{
    private static final DBInterface instance_ = new DBInterface();
    private static @Nullable DBConnection currentConnection_;

    public static DBInterface getInstance()
    {
        return instance_;
    }

    public synchronized static DBConnection getCurrentConnection() throws SQLException
    {
        DBConnection result = currentConnection_;
        if (result == null)
        {
            result = getInstance().createConnection();
            currentConnection_ = result;
        }
        return result;
    }

    public synchronized void changeDatabase(@Nullable String databaseName) throws SQLException
    {
        close();

        String dbName = databaseName;
        if (dbName != null)
        {
            if (!dbName.startsWith("jdbc:postgresql:"))
                dbName = "jdbc:postgresql:" + dbName;
        }
        databaseName_ = dbName;

        if (dbName != null)
            currentConnection_ = createConnection();
    }

    public @Nullable
    String getDatabaseName()
    {
        String result = databaseName_;
        if (result == null)
            return null;

        if (result.startsWith("jdbc:postgresql:"))
            result = result.substring("jdbc:postgresql:".length());
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String res = result;
        return res;
    }

    private @Nullable String databaseName_;
    private final String username_;
    private final String password_;

    /**
     * Opens a connection to the database defined in the config file
     */
    private DBInterface()
    {
        Properties config = SharedOperations.loadConfiguration();
        databaseName_ = SharedOperations.loadNonNullProperty(config, "dbUrl");

        String postgreSQLDriver = SharedOperations.loadNonNullProperty(config, "postgreSQLDriver");
        username_ = SharedOperations.loadNonNullProperty(config, "postgreSQLUser");
        password_ = SharedOperations.loadNonNullProperty(config, "postgreSQLPassword");

        /* load jdbc drivers */
        loadJDBCDrivers(postgreSQLDriver);
    }

    private static void loadJDBCDrivers(String postgreSQLDriver)
    {
        try
        {
            Class.forName(postgreSQLDriver).newInstance();
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
        {
            throw new RuntimeException("Cannot load JDBC drivers for: " + postgreSQLDriver + ".", e);
        }
    }

    public DBConnection createConnection() throws SQLException
    {
        String dbName = databaseName_;
        if (dbName == null)
            throw new RuntimeException("Cannot create connection, no database specified.");

        return new DBConnection(dbName, username_, password_);
    }

    public static void close() throws SQLException
    {
        DBConnection connection = currentConnection_;
        if (connection != null)
            connection.close();
        currentConnection_ = null;
    }
}
