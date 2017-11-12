package conTest;

import org.eclipse.jdt.annotation.NonNull;

import app.DataEntryTaskConstants;

public class RowDivisionMetaTest extends DataEntryTaskMetaTest implements DataEntryTaskConstants
{
    private final static String USAGE = "Usage: row_<number> = row_<number> / row_<number>";

    protected RowDivisionMetaTest(String formula, String[] columns, String dataSourceName,
                                  double[] epsilons, String[] failureMessages)
    {
        super(formula, columns, dataSourceName, epsilons, failureMessages);
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
        if (!rhs.startsWith("row_") || !rhs.contains("/"))
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

        result.append("    (");
        result.append(LS);

        parts = rhs.split("/");
        if (parts.length != 2)
            assertUsage();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String dividend = parts[0].trim();
        dividend = strip(dividend, "row_");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String divisor = parts[1].trim();
        divisor = strip(divisor, "row_");

        // (SELECT column_<column> FROM Table WHERE row = <dividend>)
        result.append("        (SELECT ");
        result.append(COLUMN_COLUMN_NAME);
        result.append("_");
        result.append(column);
        result.append(" FROM Table WHERE ");
        result.append(ROW_COLUMN_NAME);
        result.append(" = ");
        result.append(dividend);
        result.append(")");
        result.append(LS);

        result.append("        /");
        result.append(LS);

        // (SELECT column_<column> FROM Table WHERE row = <divisor>)
        result.append("        (SELECT ");
        result.append(COLUMN_COLUMN_NAME);
        result.append("_");
        result.append(column);
        result.append(" FROM Table WHERE ");
        result.append(ROW_COLUMN_NAME);
        result.append(" = ");
        result.append(divisor);
        result.append(")");
        result.append(LS);

        result.append("    )");

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
