package app;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import conTest.SharedConstants;
import database.DBInterface;
import database.DBOperations;
import utility.QueryGenerator;

public class QueryGeneratorApp implements SharedConstants
{
    public static void main(String[] args) throws FileNotFoundException, SQLException
    {
        ArrayList<String> excludedTables = new ArrayList<>();
        List<String> cdtTables = DBOperations.getTablesStartingWith(CDT_PREFIX);
        excludedTables.addAll(cdtTables);
        excludedTables.add("stop");
        excludedTables.add("temp");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String[] excludedTablesArray = excludedTables.toArray(new String[excludedTables.size()]);
        QueryGenerator generator = new QueryGenerator("100_10.sql", excludedTablesArray);
        generator.excludeTableColumn("actor", "gender");
        generator.excludeTableColumn("casts", "role");

        generator.generate(100, 0.1);

        DBInterface.close();
    }
}
