package conTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.gibello.zql.ParseException;
import org.gibello.zql.ZConstant;
import org.gibello.zql.ZExp;
import org.gibello.zql.ZExpression;
import org.gibello.zql.ZFromItem;
import org.gibello.zql.ZGroupBy;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZSelectItem;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZqlParser;

import database.DBOperations;

public class StaticTriggerController implements TriggerController, SharedConstants
{
    @Override
    public void createTriggersForQuery(String testID, String query) throws SQLException,
        IOException, ParseException
    {
        String inputQuery = query;
        if (!inputQuery.endsWith(";"))
            inputQuery += ";";

        HashSet<FutureTrigger> triggers = computeTriggersForQuery(inputQuery);
        for (FutureTrigger trigger: triggers)
            trigger.createYourself(testID);
    }

    static HashSet<FutureTrigger> computeTriggersForQuery(String query) throws IOException,
        ParseException, SQLException
    {
        String inputQuery = preprocess(query);
//        System.out.println(inputQuery);

        try (InputStream is = new ByteArrayInputStream(inputQuery.getBytes()))
        {
            ZqlParser parser = new ZqlParser(is);
            ZStatement statement = parser.readStatement();
            if (statement instanceof ZQuery)
                return computeTriggersForZQuery((ZQuery) statement);
            throw new IllegalArgumentException(inputQuery + " is not a ZQuery. Type: "
                                               + statement.getClass());
        }
    }

    private static String preprocess(String query)
    {
        String temp = query;
        temp = temp.replaceAll("date '\\d+(-\\d+)*' [-\\+] interval '\\d+ \\w+'",
                               "'<date_literal>'");
        temp = temp.replaceAll("date '\\d+(-\\d+)*'", "'<date_literal>'");
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String result = temp;
        return result;
    }

    private static HashSet<FutureTrigger> computeTriggersForZQuery(ZQuery query)
        throws SQLException
    {
        HashMap<String, String> tableAliases = new HashMap<>();
        HashSet<String> tables = new HashSet<>();

        // Suppressed due to legacy code.
        Vector<ZFromItem> fromStatements = query.getFrom();
        for (ZFromItem from: fromStatements)
        {
            String alias = from.getAlias();
            String tableName = from.getTable();
            if (alias != null)
                tableAliases.put(alias, tableName);
            tables.add(tableName);
        }

        HashSet<FutureTrigger> triggers = new HashSet<>();
        // Suppressed due to missing library annotations.
        // Suppressed due to legacy code.
        @SuppressWarnings({"null"}) @NonNull Vector<ZSelectItem> selectStatements = query.getSelect();
        boolean starSelection = processSelectStatements(tableAliases, tables, triggers,
                                                        selectStatements);

        // If this is not a star selection, look at the where clause.
        if (!starSelection)
        {
            // Taken from the tutorial, the following cast should be safe.
            ZExpression where = (ZExpression) query.getWhere();
            processWhereExpression(tableAliases, tables, triggers, where);

            ZGroupBy groupBy = query.getGroupBy();
            processGroupByStatement(tableAliases, tables, triggers, groupBy);
        }


        return triggers;
    }

    private static void processGroupByStatement(HashMap<String, String> tableAliases,
                                                HashSet<String> tables,
                                                HashSet<FutureTrigger> triggers,
                                                @Nullable ZGroupBy groupBy) throws SQLException
    {
        if (groupBy == null)
            return;
        // Suppressed due to legacy code.
        Vector<ZConstant> groups = groupBy.getGroupBy();
        for (ZConstant group: groups)
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String column = group.getValue();
            triggers.add(resolveFutureTrigger(column, tables, tableAliases));
        }
    }

    private static void processWhereExpression(HashMap<String, String> tableAliases,
                                               HashSet<String> tables,
                                               HashSet<FutureTrigger> triggers,
                                               @Nullable ZExpression where) throws SQLException
    {
        if (where == null)
            return;
        HashSet<String> variables = recurseExpression(where);
        for (String variable: variables)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull String column = variable;
            triggers.add(resolveFutureTrigger(column, tables, tableAliases));
        }
    }

    private static boolean processSelectStatements(HashMap<String, String> tableAliases,
                                                   HashSet<String> tables,
                                                   HashSet<FutureTrigger> triggers,
                                                   Vector<ZSelectItem> selectStatements)
        throws SQLException
    {
        boolean result = false;
        for (ZSelectItem select: selectStatements)
        {
            String tableName = select.getTable();

            HashSet<String> columns = new HashSet<>();

            ZExp selectExp = select.getExpression();
            if (selectExp instanceof ZConstant)
            {
                ZConstant selectConstant = (ZConstant) selectExp;
                columns.add(selectConstant.getValue());
            }
            else if (selectExp instanceof ZExpression)
            {
                ZExpression selectExpression = (ZExpression) selectExp;
                columns = recurseExpression(selectExpression);
            }

            for (String column: columns)
            {
                if (column.equals("*"))
                {
                    result = true;
                    // Create triggers for tables in the FROM clause...
                    for (String table: tables)
                    {
                        // Suppressed due to missing array element operations.
                        @SuppressWarnings("null") @NonNull String safeTable = table;
                        triggers.add(new FutureTrigger(safeTable, column));
                    }
                }
                else
                {
                    if (tableName == null)
                        triggers.add(resolveFutureTrigger(column, tables, tableAliases));
                    else
                        triggers.add(new FutureTrigger(tableName, column));
                }
            }
        }
        return result;
    }

    private static FutureTrigger resolveFutureTrigger(String column, Set<String> tables,
                                                      Map<String, String> tableAliases)
        throws SQLException
    {
        String columnName = column;

        String table = null;
        String alias = null;
        // Search for '.' in the variable.
        if (columnName.contains("."))
        {
            int dotIndex = columnName.indexOf('.');
            alias = columnName.substring(0, dotIndex);
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String temp = columnName.substring(dotIndex + 1);
            columnName = temp;
        }

        if (alias != null)
        {
            // alias can be the table name itself.
            if (tables.contains(alias))
                table = alias;
            else
            {
                // We have to find the alias in tableAliases.
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String safeTable = tableAliases.get(alias);
                table = safeTable;
            }
        }
        else
            table = findTableContainingColumn(columnName, tables);

        return new FutureTrigger(table, columnName);
    }

    private static HashSet<String> recurseExpression(ZExpression expression)
    {
        HashSet<String> result = new HashSet<>();
        // Suppressed due to legacy code.
        Vector<ZExp> operands = expression.getOperands();
        for (ZExp operand: operands)
        {
            if (operand instanceof ZExpression)
                result.addAll(recurseExpression((ZExpression) operand));
            else if (operand instanceof ZConstant)
            {
                ZConstant constant = (ZConstant) operand;
                if (!isPrimitive(constant))
                    result.add(constant.getValue());
            }
            else
                throw new UnsupportedOperationException("Unsopported operand: " + operand
                                                        + ". Type = " + operand.getClass());
        }
        return result;
    }

    private static boolean isPrimitive(ZConstant constant)
    {
        // There is also the constant type 'unknown', however I guess we cannot say much about it.
        int type = constant.getType();
        return type == ZConstant.STRING || type == ZConstant.NUMBER || type == ZConstant.NULL;
    }

    // Used for debugging.
    static String getConstantType(ZConstant constant)
    {
        switch (constant.getType())
        {
            case ZConstant.COLUMNNAME:
                return "column name";
            case ZConstant.NUMBER:
                return "number";
            case ZConstant.STRING:
                return "string";
            case ZConstant.NULL:
                return "null";
            case ZConstant.UNKNOWN:
                return "unknown";
            default:
                return "unknwon constant type";
        }
    }

    static class FutureTrigger
    {
        private String table_;
        private String column_;

        FutureTrigger(String table, String column)
        {
            table_ = table;
            column_ = column;
        }

        public void createYourself(String testID) throws SQLException
        {
            // For update events we add triggers only for the column in the FutureTrigger. However,
            // for insert and delete events, we add trigger for the whole table.

            // create trigger for the whole table for insert and delete events.
            CDTTriggerOptimizer.getInstance().createTrigger(testID,
                                                            TD.AFTER_INSERT_OR_DELETE.toTD(testID),
                                                            table_);

            // create a trigger for the update column.
            if (column_.equals("*"))
                // Need to create for the whole table.
                CDTTriggerOptimizer.getInstance().createTrigger(testID,
                                                                TD.AFTER_UPDATE.toTD(testID),
                                                                table_);
            else
                CDTTriggerOptimizer.getInstance().createTrigger(testID,
                                                                TD.updateOf(testID, column_),
                                                                table_);
        }

        @Override
        public int hashCode()
        {
            int result = table_.hashCode();
            result = result * 17 + column_.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable Object object)
        {
            if (object instanceof FutureTrigger)
                return equals((FutureTrigger) object);
            return false;
        }

        public boolean equals(FutureTrigger other)
        {
            return table_.equals(other.table_) && column_.equals(other.column_);
        }

        @Override
        public String toString()
        {
            return "[FutureTrigger: table = " + table_ + ", column = " + column_ + "]";
        }
    }

    private static String findTableContainingColumn(String columnName, Set<String> tables)
        throws SQLException
    {
        for (String table: tables)
        {
            // Suppressed due to missing array element operations.
            @SuppressWarnings("null") @NonNull String safeTable = table;
            // Suppressed due to missing array element operations.
            @SuppressWarnings("null") @NonNull String lowerCaseTableName = safeTable.toLowerCase();
            // This is done once per test, so maybe doing it without caching results is alright.
            List<String> columns = DBOperations.getTableColumns(lowerCaseTableName);
            if (columns.contains(columnName))
                return safeTable;
        }
        throw new IllegalStateException("Cannot find the column: " + columnName
                                        + " in the database catalog.");
    }
}
