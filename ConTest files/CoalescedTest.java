package testing;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import database.DBOperations;

public class CoalescedTest extends Test
{
    private final List<Test> parts_;

    public CoalescedTest(String testQuery, List<Test> parts)
    {
        super(testQuery);
        parts_ = parts;
    }

    @Override
    protected boolean shallCreateTriggers()
    {
        return false;
    }

    @Override
    public String[] getFinalIDs()
    {
        String[] result = new String[0];
        for (int a = 0; a < parts_.size(); a++)
            result[a] = parts_.get(a).getFinalID();
        return result;
    }

    @Override
    public void initializeTempTable() throws SQLException
    {
        StringBuilder tableQuery = new StringBuilder();
        tableQuery.append("SELECT * FROM ");

        for (int a = 0; a < parts_.size(); a++)
        {
            Test part = parts_.get(a);
            String columnName = "s" + (a + 1);
            DBOperations.executeUpdate("ALTER TABLE " + part.getFinalID() + " ADD COLUMN "
                                       + columnName + " serial");

            tableQuery.append(part.getFinalID());
            if (a != parts_.size() - 1)
                tableQuery.append(", ");
        }

        tableQuery.append(" WHERE ");
        String firstColumnName = "s1";
        for (int a = 1; a < parts_.size(); a++)
        {
            String otherColumnname = "s" + (a + 1);
            tableQuery.append(firstColumnName);
            tableQuery.append("=");
            tableQuery.append(otherColumnname);
            if (a != parts_.size() - 1)
                tableQuery.append(" and ");
        }

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String tableQueryFinal = tableQuery.toString();
        DBOperations.createTable(getFinalID(), tableQueryFinal);

        StringBuilder dropColumnQuery = new StringBuilder();
        dropColumnQuery.append("ALTER TABLE ");
        dropColumnQuery.append(getFinalID());
        dropColumnQuery.append(" ");

        for (int a = 0; a < parts_.size(); a++)
        {
            Test part = parts_.get(a);
            String columnName = "s" + (a + 1);
            DBOperations.executeUpdate("ALTER TABLE " + part.getFinalID() + " DROP COLUMN "
                                       + columnName);

            dropColumnQuery.append("DROP COLUMN " + columnName);
            if (a != parts_.size() - 1)
                dropColumnQuery.append(", ");
        }

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String dropColumnQueryFinal = dropColumnQuery.toString();
        DBOperations.executeUpdate(dropColumnQueryFinal);
    }

    @Override
    public List<Test> getParts()
    {
        return parts_;
    }
}
