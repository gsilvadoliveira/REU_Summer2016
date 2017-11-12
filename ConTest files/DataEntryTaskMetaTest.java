package conTest;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;

public abstract class DataEntryTaskMetaTest
{
    private final String formula_;
    private final String[] columns_;
    private final String dataSourceName_;
    private final double[] epsilons_;
    private final String[] failureMessages_;

    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") @NonNull public static final String LS = System.lineSeparator();
    // @formatter:off 
    private static final String USAGE =
        "Test query must be in the following format: " + LS
            + "<type(ROWSUM)> @ <formula> @ <columns>(colon seperated values) @ " 
            + "<data_source_name>(string) @ <epsilon_values>(double or colon seperated values) @ " 
            + "<failure_messages>(SAME or colon seperated values)";
    // @formatter:on


    protected DataEntryTaskMetaTest(String formula, String[] columns, String dataSourceName,
                                    double[] epsilons, String[] failureMessages)
    {
        formula_ = formula;
        columns_ = columns;
        dataSourceName_ = dataSourceName;
        epsilons_ = epsilons;
        failureMessages_ = failureMessages;
    }

    public abstract String constructTestQuery(String column);

    protected String getFormula()
    {
        return formula_;
    }

    protected String getDataSourceName()
    {
        return dataSourceName_;
    }

    public ArrayList<DataEntryTaskTest> generate()
    {
        ArrayList<DataEntryTaskTest> result = new ArrayList<>();
        for (int a = 0; a < columns_.length; a++)
        {
            // While parsing, we split for ":", so columns and failure messages might have leading
            // spaces. Remove them with trim().
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String column = columns_[a].trim();
            String testQuery = constructTestQuery(column);
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String failureMessage = failureMessages_[a].trim();
            result.add(new DataEntryTaskTest(testQuery, dataSourceName_, epsilons_[a],
                                             failureMessage));
        }
        return result;
    }

    private static void assertUsage(String test)
    {
        throw new RuntimeException(USAGE + LS + "Test query: '" + test + "'");
    }

    public static DataEntryTaskMetaTest parse(String test)
    {
        String[] parts = test.split("\\@");
        if (parts.length != 6)
            assertUsage(test);

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String type = parts[0].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String formula = parts[1].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String columnsAll = parts[2].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String dataSourceName = parts[3].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String epsilonAll = parts[4].trim();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String failureMessagesAll = parts[5].trim();

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String[] columns = columnsAll.split(":");
        String[] epsilonsString = epsilonAll.split(":");
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String[] failureMessages = failureMessagesAll.split(":");
        if (failureMessages.length == 1 && failureMessages[0].equals("SAME"))
            failureMessages = columns;

        if ((columns.length != epsilonsString.length && epsilonsString.length != 1)
            || columns.length != failureMessages.length)
            assertUsage(test);

        double[] epsilons = new double[columns.length];
        for (int a = 0; a < epsilonsString.length; a++)
        {
            try
            {
                String epsilonString = epsilonsString[a].trim();
                double epsilonValue = Double.parseDouble(epsilonString);
                epsilons[a] = epsilonValue;
            }
            catch (NumberFormatException e)
            {
                assertUsage(test);
            }
        }

        if (epsilonsString.length == 1)
        {
            for (int a = 1; a < epsilons.length; a++)
                epsilons[a] = epsilons[0];
        }

        if (type.equals("ROWSUM"))
            return new RowSumMetaTest(formula, columns, dataSourceName, epsilons, failureMessages);
        else if (type.equals("ROWDIV"))
            return new RowDivisionMetaTest(formula, columns, dataSourceName, epsilons,
                                           failureMessages);
        assertUsage(test);
        // dead code.
        throw new RuntimeException();
    }
}
