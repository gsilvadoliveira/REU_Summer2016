package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.arrays.ArrayOperations;

import database.DBOperations;

public class QueryGenerator
{
    private final String outputPath_;
    private final String[] excludedTables_;
    private final HashMap<String, ArrayList<String>> excludedTableColumns_;

    public QueryGenerator(String outputPath, String... excludedTables)
    {
        outputPath_ = outputPath;
        excludedTables_ = excludedTables;
        excludedTableColumns_ = new HashMap<>();
    }

    public void excludeTableColumn(String table, String column)
    {
        if (!excludedTableColumns_.containsKey(table))
            excludedTableColumns_.put(table, new ArrayList<String>());
        excludedTableColumns_.get(table).add(column);
    }

    private Database init() throws SQLException
    {
        Database result = new Database();
        List<String> tables = DBOperations.getPublicTables();
        for (String table: tables)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull String safeTable = table;

            if (ArrayOperations.contains(excludedTables_, safeTable))
                continue;

            List<String> tableColumns = DBOperations.getTableColumns(safeTable);
            ArrayList<String> excludedColumns = excludedTableColumns_.get(safeTable);
            if (excludedColumns != null)
                tableColumns.removeAll(excludedColumns);
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String[] columns = tableColumns.toArray(new String[tableColumns.size()]);
            result.addTable(new DBTable(safeTable, columns));
        }
        return result;
    }

    public void generate(int noQueries, double selectRatio) throws FileNotFoundException,
        SQLException
    {
        Database database = init();
        File rootFolder = new File("queries");
        if (!rootFolder.exists())
            rootFolder.mkdirs();

        int noSelectQueries = (int) (noQueries * selectRatio);
        int noUpdateQueries = noQueries - noSelectQueries;
        ArrayList<String> queries = new ArrayList<>();

        queries.addAll(generateSelectQueries(database, noSelectQueries));
        queries.addAll(generateUpdateQueries(database, noUpdateQueries));

        try (Formatter writer = new Formatter(new File(rootFolder, outputPath_)))
        {
            Collections.shuffle(queries);
            for (String query: queries)
                writer.format("%s%n", query);
        }
        System.out.println("Process completed...");
    }

    private static List<String> generateUpdateQueries(Database database, int noUpdateQueries)
        throws SQLException
    {
        ArrayList<String> result = new ArrayList<>();
        for (int a = 0; a < noUpdateQueries; a++)
        {
            DBTable table = getRandomTable(database);
            String column = getRandomColumn(table);

            String query = "SELECT " + column + " FROM " + table.getName();
            List<String> columnValues = DBOperations.executeSingleProjectionQuery(query, column);
            String columnValue = null;
            while (columnValue == null || columnValue.equals("null"))
                columnValue = ArrayOperations.getRandomElement(columnValues);

            columnValue = columnValue.replace("'", "''");
//            String updateQuery =
//                "UPDATE " + table.getName() + " SET " + column + " = 'cdt_" + columnValue
//                    + "' WHERE " + column + " = '" + columnValue + "';";
            // TODO: Note that this is okay for now as even if we don't change the value (set
            // it to the same), there will be a trigger fire.
            String updateQuery = "UPDATE " + table.getName() + " SET " + column + " = '"
                                 + columnValue + "' WHERE " + column + " = '" + columnValue + "';";
            System.out.println(updateQuery);
            result.add(updateQuery);
        }
        return result;
    }

    private static List<String> generateSelectQueries(Database database, int noSelectQueries)
    {
        ArrayList<String> result = new ArrayList<>();
        for (int a = 0; a < noSelectQueries; a++)
        {
            DBTable table = getRandomTable(database);
            String column = getRandomColumn(table);

//            String query = "SELECT COUNT(distinct " + column + ") FROM " + table.getName() + ";";
            String query = "SELECT COUNT(" + column + ") FROM " + table.getName() + ";";
            System.out.println(query);
            result.add(query);
        }
        return result;
    }

    private static DBTable getRandomTable(Database database)
    {
        List<DBTable> tables = database.getTables();
        return ArrayOperations.getRandomElement(tables);
    }

    private static String getRandomColumn(DBTable table)
    {
        String[] columns = table.getColumns();
        return ArrayOperations.getRandomElement(columns);
    }
}
