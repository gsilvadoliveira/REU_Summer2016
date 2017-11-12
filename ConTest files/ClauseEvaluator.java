package conTest.incremental;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public class ClauseEvaluator
{
    public static boolean DEBUG_CLAUSE = false;

    private static final String BETWEEN_SUBSTITUTE = " !B! ";
    private static final String AND_SUBSTITUTE = " !A! ";

    public static boolean evaluateBooleanClause(String inputClause, Map<String, Object> data)
    {
        String clause = inputClause;
        while (clause.contains(" between "))
        {
            int betweenIndex = clause.indexOf(" between ");
            int andIndex = clause.indexOf(" and ", betweenIndex);

            String firstPart = clause.substring(0, betweenIndex);
            String secondPart = clause.substring(betweenIndex + " between ".length(), andIndex);
            String thirdPart = clause.substring(andIndex + " and ".length(), clause.length());

            clause = firstPart + BETWEEN_SUBSTITUTE + secondPart + AND_SUBSTITUTE + thirdPart;
        }
        return evaluateBooleanClauseInternal(clause, data);
    }

    public static boolean evaluateBooleanClauseInternal(String clause, Map<String, Object> data)
    {
        debugClause(clause);
        if (clause.contains("and "))
        {
            int index = clause.indexOf("and ");
            @SuppressWarnings("null") @NonNull String subClause = clause.substring(0, index).trim();
            @SuppressWarnings("null") @NonNull String remaining = clause.substring(index
                                                                                       + "and ".length(),
                                                                                   clause.length());
            return evaluateBooleanClause(subClause, data) && evaluateBooleanClause(remaining, data);
        }

        if (clause.contains("or "))
        {
            int index = clause.indexOf("or ");
            @SuppressWarnings("null") @NonNull String subClause = clause.substring(0, index).trim();
            @SuppressWarnings("null") @NonNull String remaining = clause.substring(index
                                                                                       + "or ".length(),
                                                                                   clause.length());
            return evaluateBooleanClause(subClause, data) || evaluateBooleanClause(remaining, data);
        }

        if (clause.contains(BETWEEN_SUBSTITUTE) && clause.contains(AND_SUBSTITUTE))
        {
            int firstIndex = clause.indexOf(BETWEEN_SUBSTITUTE);
            int secondIndex = clause.indexOf(AND_SUBSTITUTE);
            // The pattern looks like <... between ... and ...>
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, firstIndex).trim();
            Number lhsValue = evaluateNumericClause(lhs, data);

            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String firstPart = clause.substring(firstIndex
                                                                                       + BETWEEN_SUBSTITUTE.length(),
                                                                                   secondIndex)
                                                                        .trim();
            Number firstPartValue = evaluateNumericClause(firstPart, data);

            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String secondPart = clause.substring(secondIndex
                                                                                        + AND_SUBSTITUTE.length(),
                                                                                    clause.length());
            Number secondPartValue = evaluateNumericClause(secondPart, data);
            return Number.greaterThanOrEqualTo(lhsValue, firstPartValue)
                   && Number.lessThanOrEqualTo(lhsValue, secondPartValue);
        }

        if (clause.contains(" < "))
        {
            int index = clause.indexOf(" < ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index + " < ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.lessThan(evaluateNumericClause(lhs, data),
                                   evaluateNumericClause(rhs, data));
        }

        if (clause.contains(" <= "))
        {
            int index = clause.indexOf(" <= ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index
                                                                                 + " <= ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.lessThanOrEqualTo(evaluateNumericClause(lhs, data),
                                            evaluateNumericClause(rhs, data));
        }

        if (clause.contains(" >= "))
        {
            int index = clause.indexOf(" >= ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index
                                                                                 + " >= ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.greaterThanOrEqualTo(evaluateNumericClause(lhs, data),
                                               evaluateNumericClause(rhs, data));
        }

        throw new RuntimeException("Unknown sub-clause: " + clause);
    }

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    // TODO KM: I am not sure if this representation is correct.
    public static final long YEAR = 365 * DAY + 6 * HOUR;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static Number evaluateNumericClause(String clause, Map<String, Object> data)
    {
        debugClause(clause);
        if (clause.contains("(") && clause.contains(")"))
        {
            int startIndex = clause.indexOf("(");
            int endIndex = clause.indexOf(")", startIndex);
            String between = clause.substring(startIndex + 1, endIndex);

            int currentStart = startIndex;
            while (between.contains("("))
            {
                currentStart = clause.indexOf("(", currentStart + 1);
                endIndex = clause.indexOf(")", endIndex + 1);
                between = clause.substring(currentStart + 1, endIndex);
            }
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String subClause = clause.substring(startIndex
                                                                                       + "(".length(),
                                                                                   endIndex).trim();
            Number subClauseResult = evaluateNumericClause(subClause, data);

            if (startIndex == 0 && endIndex == clause.length() - ")".length())
                return subClauseResult;

            String lhs = "";
            if (startIndex != 0)
                lhs = clause.substring(0, startIndex);

            String rhs = "";
            if (endIndex != clause.length() - ")".length())
                rhs = clause.substring(endIndex + ")".length());

            if (lhs.trim().equals("") && rhs.trim().equals(""))
                return subClauseResult;

            return evaluateNumericClause(lhs + subClauseResult.toString() + rhs, data);
        }

        if (clause.contains(" - "))
        {
            int index = clause.indexOf(" - ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index + " - ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.subtract(evaluateNumericClause(lhs, data),
                                   evaluateNumericClause(rhs, data));
        }

        if (clause.contains(" * "))
        {
            int index = clause.indexOf(" * ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index + " * ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.multiply(evaluateNumericClause(lhs, data),
                                   evaluateNumericClause(rhs, data));
        }

        if (clause.contains(" + "))
        {
            int index = clause.indexOf(" + ");
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String lhs = clause.substring(0, index).trim();
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String rhs = clause.substring(index + " + ".length(),
                                                                             clause.length())
                                                                  .trim();
            return Number.add(evaluateNumericClause(lhs, data), evaluateNumericClause(rhs, data));
        }

        if (clause.startsWith("date '") && clause.endsWith("'"))
        {
            String date = clause.substring("date '".length(), clause.length() - "'".length());
            try
            {
                return new LongNumber(new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime());
            }
            catch (Throwable e)
            {
                throw new RuntimeException("Cannot parse date constant: '" + date + "'", e);
            }
        }

        if (clause.startsWith("interval '") && clause.endsWith("'"))
        {
            String interval = clause.substring("interval '".length(),
                                               clause.length() - "'".length());
            if (interval.endsWith(" days"))
            {
                String daysS = interval.substring(0, interval.length() - " days".length());
                int days = Integer.parseInt(daysS);
                return new LongNumber(DAY * days);
            }
            else if (interval.endsWith(" year"))
            {
                String yearS = interval.substring(0, interval.length() - " days".length());
                int year = Integer.parseInt(yearS);
                return new LongNumber(YEAR * year);
            }
            throw new RuntimeException("Unknown interval: '" + interval + "'");
        }

        // Assume that the clause is an attribute.
        if (data.containsKey(clause))
        {
            String value = data.get(clause).toString();
            // Try to directly convert to a long.
            try
            {
                return new DoubleNumber(Double.parseDouble(value));
            }
            catch (NumberFormatException e)
            {
                // Assume that it is a date.
                try
                {
                    return new LongNumber(new SimpleDateFormat("yyyy-MM-dd").parse(value).getTime());
                }
                catch (Throwable e1)
                {
                    throw new RuntimeException("Cannot parse numeric attribute value: '" + value
                                               + "'", e1);
                }
            }
        }

        // Assume that the clause is a double constant...
        return new DoubleNumber(Double.parseDouble(clause));
    }

    private static void debugClause(String clause)
    {
        if (DEBUG_CLAUSE)
            System.out.println("Evaluating clause = " + clause);
    }
}
