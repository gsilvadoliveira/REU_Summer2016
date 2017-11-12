package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.arrays.ArrayOperations;
import com.kivancmuslu.www.arrays.FilterOperation;
import com.kivancmuslu.www.file.FileOperations;

import conTest.SharedOperations;
import database.DBOperations;

public class DataEntryTaskDataInserter implements DataEntryTaskConstants
{
    public static String DATA_ROOT_PATH = "";
    private static final boolean DEBUG_QUERY = true;

    public static void main(String[] args) throws SQLException, FileNotFoundException
    {
        Properties properties = SharedOperations.loadConfiguration();
        DATA_ROOT_PATH = SharedOperations.loadNonNullProperty(properties, "dataSourcesRoot");

        // Cannot do this as we have started using primary/foreign keys.
//        truncateTables();

        File dataRoot = new File(DATA_ROOT_PATH);
        ArrayList<File> dataSources = new ArrayList<>();
        getFiles(dataRoot, dataSources);
        ArrayList<File> csvFiles = ArrayOperations.filter(dataSources, new FilterOperation<File>()
        {
            @Override
            public boolean shallInclude(@Nullable File file)
            {
                return file != null && file.getName().endsWith(".csv");
            }
        });
        Collections.sort(csvFiles, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                return file1.getName().compareTo(file2.getName());
            }
        });
        System.out.println("Found " + csvFiles.size() + " data sources.");
        System.out.println();
        for (File csvFile: csvFiles)
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull File safeFile = csvFile;
            loadDataSource(safeFile);
        }
    }

    private static void getFiles(File root, ArrayList<File> result)
    {
        if (root.isDirectory())
        {
            for (File file: root.listFiles())
            {
                // Suppressed due to missing array element annotations.
                @SuppressWarnings("null") @NonNull File safeFile = file;
                getFiles(safeFile, result);
            }
        }
        else
            result.add(root);
    }

    private static void loadDataSource(File dataSource) throws SQLException, FileNotFoundException
    {
        System.out.println("Processing data source: " + dataSource.getName());

        String baseName = FileOperations.baseName(dataSource);
        // Insert the file into the DataSources table.
        String query = "INSERT INTO " + DATA_SOURCES_TABLE_NAME + " VALUES('" + baseName + "')";
        debugQuery(query);
        DBOperations.executeUpdate(query);

        // Get the data id for this data source.
        // @formatter:off
        query = "SELECT " + DATA_ID_COLUMN_NAME + 
                    " FROM " + DATA_SOURCES_TABLE_NAME + 
                    " WHERE " + DATA_NAME_COLUMN_NAME + " = " + "'" + baseName + "'";
        // @formatter:on
        debugQuery(query);
        int dataID = DBOperations.executeSingleIntegerProjectionQuery(query, DATA_ID_COLUMN_NAME)
                                 .get(0).intValue();

        // Read the CSV file and insert every row as a new Data.
        try (Scanner reader = new Scanner(dataSource))
        {
            int rowCounter = 1;
            while (reader.hasNext())
            {
                String row = reader.nextLine();
                String[] columns = row.split(",");
                StringBuilder insertQuery = new StringBuilder();
                insertQuery.append("INSERT INTO ");
                insertQuery.append(DATA_TABLE_NAME);
                insertQuery.append(" ");
                insertQuery.append("VALUES ");
                insertQuery.append("(");
                insertQuery.append(dataID);
                insertQuery.append(",");
                insertQuery.append(rowCounter);
                for (int a = 0; a < columns.length; a++)
                {
                    String columnValue = columns[a].trim();
                    insertQuery.append(",");
                    insertQuery.append(columnValue);
                }
                insertQuery.append(")");

                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String safeQuery = insertQuery.toString();
                debugQuery(safeQuery);
                DBOperations.executeUpdate(safeQuery);

                rowCounter++;
            }
        }

        System.out.println("Completed processing data source: " + dataSource.getName());
        System.out.println();
    }

    private static void debugQuery(String query)
    {
        if (DEBUG_QUERY)
            System.out.println("Executing: " + query);
    }

    // This method was used beforehand, however cannot be used anymore because of constraints.
    @SuppressWarnings("unused")
    private static void truncateTables() throws SQLException
    {
        System.out.println("Truncating " + DATA_TABLE_NAME + " and " + DATA_SOURCES_TABLE_NAME
                           + " tables...");
        DBOperations.truncateTable(DATA_TABLE_NAME);
        DBOperations.truncateTable(DATA_SOURCES_TABLE_NAME);
        System.out.println("Truncation completed...");
        System.out.println();
    }
}
