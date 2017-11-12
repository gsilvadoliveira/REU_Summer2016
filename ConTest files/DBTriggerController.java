package conTest;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import database.DBOperations;

public class DBTriggerController implements TriggerController, SharedConstants
{
    @Override
    public void createTriggersForQuery(String testID, String query) throws SQLException
    {
        // We don't care about the syntax of the query, we will add triggers to all existing tables,
        // for the whole table.
        List<String> tables = DBOperations.getPublicTables();
        for (String table: tables)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull String tableName = table;
            // We don't want to create triggers for CDT specific tables.
            if (tableName.startsWith(CDT_PREFIX))
                continue;

            CDTTriggerOptimizer.getInstance().createTrigger(testID, TD.AFTER_ALL.toTD(testID),
                                                            tableName);
        }
    }
}
