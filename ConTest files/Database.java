package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class Database
{
    private final HashMap<String, DBTable> tables_;
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") private static final String LS = System.lineSeparator();

    public Database()
    {
        tables_ = new HashMap<>();
    }

    public void addTable(DBTable table)
    {
        tables_.put(table.getName(), table);
    }

    public @Nullable
    DBTable getTable(String tableName)
    {
        return tables_.get(tableName);
    }

    public List<DBTable> getTables()
    {
        return new ArrayList<>(tables_.values());
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Database with ");
        result.append(tables_.size());
        result.append(" tables.");
        result.append(LS);

        for (DBTable table: tables_.values())
        {
            result.append("\t");
            result.append(table);
            result.append(LS);
        }
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String res = result.toString();
        return res;
    }
}
