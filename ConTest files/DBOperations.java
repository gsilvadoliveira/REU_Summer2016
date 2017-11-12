package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.arrays.ArrayOperations;
import com.kivancmuslu.www.debug.Debug;

import conTest.CDTBase;
import conTest.CDTTriggerOptimizer;

public class DBOperations
{
    private static final String[] PERMITTED_TRIGGER_CREATORS = new String[] {CDTTriggerOptimizer.class.getName()};

    public static boolean DEBUG_TRIGGER_CREATION = true;

    private DBOperations()
    {}

    public static List<String> getTableColumns(String tableName) throws SQLException
    {
        //@formatter:off
        /*
         SELECT column_name 
         FROM information_schema.columns 
         WHERE table_name = '<tableName>'
         */
        String query = "SELECT column_name " +
                       "FROM information_schema.columns " +
                       "WHERE table_name = '" + tableName + "'";
        //@formatter:on
        return executeSingleProjectionQuery(query, "column_name");
    }

    public static void truncateTable(String tableName) throws SQLException
    {
        String query = "TRUNCATE TABLE " + tableName;
        executeUpdate(query);
    }

    public static void createTable(String tableName, String initialContentQuery)
        throws SQLException
    {
        createRelation("TABLE", tableName, initialContentQuery);
    }

    public static void createView(String viewName, String initialContentQuery) throws SQLException
    {
        createRelation("VIEW", viewName, initialContentQuery);
    }

    private static void createRelation(String relationKeyword, String relationName,
                                       String initialContentQuery) throws SQLException
    {
        // @formatter:off
        String query = 
            "CREATE " + relationKeyword + " " + relationName + " AS " + 
            "(" + initialContentQuery + ")";
        // @formatter:on
        executeUpdate(query);
    }

    public static List<String> getTablesStartingWith(String prefix) throws SQLException
    {
        //@formatter:off
        String query = "SELECT table_name " + 
                       "FROM information_schema.tables " + 
                       "WHERE table_name LIKE '" + prefix + "%' " + 
                           "AND table_schema = 'public'";
        //@formatter:on
        return executeSingleProjectionQuery(query, "table_name");
    }

    public static List<String> getPublicTables() throws SQLException
    {
        //@formatter:off
        String query = "SELECT table_name " + 
                       "FROM information_schema.tables " + 
                       "WHERE table_schema = 'public'";
        //@formatter:on
        return executeSingleProjectionQuery(query, "table_name");
    }

    public static void dropTable(String tableName) throws SQLException
    {
        dropTable(tableName, false, false);
    }

    public static void dropTable(String tableName, boolean ifExists) throws SQLException
    {
        dropTable(tableName, ifExists, false);
    }

    public static void dropTable(String tableName, boolean ifExists, boolean cascade)
        throws SQLException
    {
        String query = "DROP TABLE ";
        if (ifExists)
            query += "IF EXISTS ";
        query += tableName;
        if (cascade)
            query += " CASCADE";
        executeUpdate(query);
    }

    public static ResultSet executeQuery(String query) throws SQLException
    {
        return DBInterface.getCurrentConnection().executeQuery(query);
    }

    public static List<String> executeSingleStringProjectionQuery(String query, String columnName)
        throws SQLException
    {
        return executeListQuery(query, columnName, new ResultSetToStringConverter());
    }

    public static List<Integer> executeSingleIntegerProjectionQuery(String query, String columnName)
        throws SQLException
    {
        return executeListQuery(query, columnName, new ResultSetToIntegerConverter());
    }

    public static List<Timestamp> executeSingleTimestampProjectionQuery(String query,
                                                                        String columnName)
        throws SQLException
    {
        return executeListQuery(query, columnName, new ResultSetToTimestampConverter());
    }

    private static <T> List<T> executeListQuery(String query, String columnName,
                                                ResultSetConverter<T> converter)
        throws SQLException
    {
        List<T> result = new ArrayList<>();
        try (ResultSet resultSet = DBOperations.executeQuery(query))
        {
            while (resultSet.next())
            {
                T element = converter.convert(resultSet, columnName);
                result.add(element);
            }
        }
        DBOperations.commit();
        return result;
    }

    private static class ResultSetToStringConverter implements ResultSetConverter<String>
    {
        ResultSetToStringConverter()
        {}

        @Override
        public @Nullable
        String convert(ResultSet resultSet, String columnName) throws SQLException
        {
            return resultSet.getString(columnName);
        }
    }

    private static class ResultSetToTimestampConverter implements ResultSetConverter<Timestamp>
    {
        ResultSetToTimestampConverter()
        {}

        @Override
        @Nullable
        public Timestamp convert(ResultSet resultSet, String columnName) throws SQLException
        {
            return resultSet.getTimestamp(columnName);
        }
    }

    private static class ResultSetToIntegerConverter implements ResultSetConverter<Integer>
    {
        ResultSetToIntegerConverter()
        {}

        @Override
        public @Nullable
        Integer convert(ResultSet resultSet, String columnName) throws SQLException
        {
            return Integer.valueOf(resultSet.getInt(columnName));
        }
    }

    private static interface ResultSetConverter<T>
    {
        @Nullable
        T convert(ResultSet resultSet, String columnName) throws SQLException;
    }

    public static void commit() throws SQLException
    {
        DBInterface.getCurrentConnection().commit();
    }

    public static void executeUpdate(String query) throws SQLException
    {
        DBInterface.getCurrentConnection().executeUpdate(query);
    }
    
    public static PreparedStatement prepareStatement(String query) throws SQLException
    {
        return DBInterface.getCurrentConnection().prepareStatement(query);
    }

    public static void dropTrigger(DBTrigger trigger) throws SQLException
    {
        String query = "DROP TRIGGER " + trigger.getName() + " ON " + trigger.getTableName();
        executeQuery(query);
    }

    public static List<String> getInstalledLanguages() throws SQLException
    {
        String query = "SELECT lanname FROM pg_language";
        return executeSingleProjectionQuery(query, "lanname");
    }

    public static List<DBTrigger> getTestTriggersStarting(String prefix) throws SQLException
    {
        //@formatter:off
        /*
         SELECT tgname, relname
         FROM pg_trigger, pg_stat_all_tables
         WHERE pg_trigger.tgrelid = pg_stat_all_tables.relid
             AND tgname LIKE '%';
         */
        String query = "SELECT tgname, relname " + 
                       "FROM pg_trigger, pg_stat_all_tables " + 
                       "WHERE pg_trigger.tgrelid = pg_stat_all_tables.relid " + 
                           "AND tgname LIKE '" + prefix + "%'";
        //@formatter:on
        List<DBTrigger> result = executeProjectionQuery(query, new IteratorParser<DBTrigger>()
        {
            @Override
            public DBTrigger parse(List<String> values)
            {
                // Suppressed due to complex program logic: the trigger names cannot be null.
                @SuppressWarnings("null") @NonNull String triggerName = values.get(0);
                // Suppressed due to complex program logic: the table names cannot be null.
                @SuppressWarnings("null") @NonNull String tableName = values.get(1);
                return new DBTrigger(triggerName, tableName);
            }
        }, "tgname", "relname");
        return result;
    }

    public static List<String> executeSingleProjectionQuery(String query, String projectionColumn)
        throws SQLException
    {
        return executeProjectionQuery(query, new StringParser(), projectionColumn);
    }

    private static <T> List<T> executeProjectionQuery(String query, IteratorParser<T> parser,
                                                      String... projectionColumns)
        throws SQLException
    {
        try (ResultSet resultSet = executeQuery(query))
        {
            ArrayList<T> result = new ArrayList<>();
            while (resultSet.next())
            {
                ArrayList<String> currentResult = new ArrayList<>();
                for (String projectionColumn: projectionColumns)
                    currentResult.add(resultSet.getString(projectionColumn));
                result.add(parser.parse(currentResult));
            }
            commit();
            return result;
        }
    }

    private static class StringParser implements IteratorParser<String>
    {
        StringParser()
        {}

        @Override
        public @Nullable
        String parse(List<String> values)
        {
            return values.get(0);
        }
    }

    private static interface IteratorParser<T>
    {
        @Nullable
        T parse(List<String> values);
    }

    public static void installLanguage(String langName) throws SQLException
    {
        String query = "CREATE LANGUAGE " + langName;
        executeUpdate(query);
    }

    public static void dropFunctionIfExists(String functionName) throws SQLException
    {
        String query = "DROP FUNCTION IF EXISTS " + functionName + "() CASCADE";
        executeUpdate(query);
    }

    public static void createTriggerFunction(String functionName, String language, String sourceCode)
        throws SQLException
    {
        dropFunctionIfExists(functionName);

        //@formatter:off
        String query = "CREATE FUNCTION " + functionName + "()" + 
                       "RETURNS TRIGGER AS" + 
                       "'" +
                       sourceCode + 
                       "'" +
                       "LANGUAGE " + language;
        //@formatter:on
        executeUpdate(query);
    }

    public static void createTrigger(Object caller, String triggerName, String tableName,
                                     String when, String triggerFunctionName) throws SQLException
    {
        createTrigger(caller, triggerName, tableName, when, null, triggerFunctionName, true);
    }

    public static void createTrigger(Object caller, String triggerName, String tableName,
                                     String when, @Nullable String condition,
                                     String triggerFunctionName, boolean forEachStatement)
        throws SQLException
    {
        assertTriggerCreator(caller);

        //@formatter:off
        String query = "CREATE TRIGGER " + triggerName + " " + 
                       when + " " + 
                       "ON " + tableName + " " +  
                       "FOR EACH " + (forEachStatement ? "STATEMENT" : "ROW") + " " +
                       (condition == null ? "" : "WHEN " + condition + " ") + 
                       "EXECUTE PROCEDURE " + triggerFunctionName + "()";
        //@formatter:on
        if (DEBUG_TRIGGER_CREATION)
            CDTBase.getInstance().logInfo("Creating trigger with: " + query);
        executeUpdate(query);
    }

    private static void assertTriggerCreator(Object creator)
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String creatorClassName = creator.getClass().getName();
        if (!ArrayOperations.contains(PERMITTED_TRIGGER_CREATORS, creatorClassName))
            throw new IllegalStateException(creatorClassName
                                            + " cannot create a trigger. Permitted creators: "
                                            + Debug.join(PERMITTED_TRIGGER_CREATORS, ", "));
    }
}
