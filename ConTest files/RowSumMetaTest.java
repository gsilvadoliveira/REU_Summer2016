package conTest;

import org.eclipse.jdt.annotation.NonNull;

import app.DataEntryTaskConstants;

public class RowSumMetaTest extends DataEntryTaskMetaTest implements DataEntryTaskConstants
{
    private final static String USAGE = "Usage: row_<number> = SUM(row_<start>::row_<end>) "
                                        + "( + [SUM(row_<start>::row_<end>)|row_<number>])*";

    protected RowSumMetaTest(String formula, String[] columns, String dataSourceName,
                             double[] epsilonValues, String[] failureMessages)
    {
        super(formula, columns, dataSourceName, epsilonValues, failureMessages);
    }

    private void assertUsage()
    {
        throw new RuntimeException("Wrong formula: " + getFormula() + LS + USAGE);
    }

    @Override
    public String constructTestQuery(String column)
    {
        String formula = getFormula();
        String[] parts = formula.split("=");
        if (parts.length != 2)
            assertUsage();

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String lhs = parts[0].trim();
        lhs = strip(lhs, "row_");

        String rhs = parts[1].trim();
        if (!rhs.startsWith("SUM(") || !rhs.contains(")") || !rhs.contains("::"))
            assertUsage();

        StringBuilder result = new StringBuilder();
        result.append("SELECT");
        result.append(LS);
        // (SELECT column_<column> FROM Table WHERE row = <lhs>)
        result.append("    (SELECT ");
        result.append(COLUMN_COLUMN_NAME);
        result.append("_");
        result.append(column);
        result.append(" FROM Table WHERE ");
        result.append(ROW_COLUMN_NAME);
        result.append(" = ");
        result.append(lhs);
        result.append(")");
        result.append(LS);

        result.append("    -");
        result.append(LS);

        // (SELECT SUM(column_<column>) FROM Table WHERE)
        result.append("    (SELECT ");
        result.append("SUM (");
        result.append(COLUMN_COLUMN_NAME);
        result.append("_");
        result.append(column);
        result.append(")");
        result.append(" FROM Table WHERE");

        String[] terms = rhs.split("\\+");
        for (String term: terms)
        {
            result.append(" ");
            result.append(ROW_COLUMN_NAME);

            term = term.trim();
            // SUM(row_<start>::row_end)
            // row BETWEEN <start> AND <end> OR
            if (term.contains("::"))
            {
                term = term.substring("SUM(".length(), term.length() - ")".length());
                parts = term.split("::");
                if (parts.length != 2)
                    assertUsage();

                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String part1 = parts[0].trim();
                String start = strip(part1, "row_");
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String part2 = parts[1].trim();
                String end = strip(part2, "row_");

                result.append(" BETWEEN ");
                result.append(start);
                result.append(" AND ");
                result.append(end);
            }
            // row_<number>
            // row = <number> OR
            else
            {
                result.append(" = ");
                result.append(strip(term, "row_"));
            }
            result.append(" OR");
        }
        result.delete(result.length() - " OR".length(), result.length());
        result.append(")");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String query = result.toString();
        return query;
    }

    private String strip(String string, String prefix)
    {
        if (!string.startsWith(prefix))
            assertUsage();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String result = string.substring(prefix.length());
        return result;
    }
}
