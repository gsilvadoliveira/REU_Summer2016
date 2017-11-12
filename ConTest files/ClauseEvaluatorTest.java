package conTest.incremental;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Assert;
import org.junit.Test;

public class ClauseEvaluatorTest
{
    /*
     * Example test: select sum(l_quantity) as sum_qty from LINEITEM where l_shipdate <= date
     * '1998-12-01' - interval '90 days' group by l_returnflag, l_linestatus
     */
    public static final double ERROR_RATE = 0.00001;

    @SuppressWarnings("static-method")
    @Test
    public void testInteger()
    {
        int value = generateRandomInteger();
        Number result = ClauseEvaluator.evaluateNumericClause(value + "",
                                                              new HashMap<String, Object>());
        Assert.assertEquals(value, result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testDouble()
    {
        double value = generateRandomDouble();
        Number result = ClauseEvaluator.evaluateNumericClause(value + "",
                                                              new HashMap<String, Object>());
        Assert.assertEquals(value, result.getDoubleValue(), ERROR_RATE);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testDate() throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String dateInput = dateFormat.format(now);
        Number result = ClauseEvaluator.evaluateNumericClause("date " + "'" + dateInput + "'",
                                                              new HashMap<String, Object>());
        Assert.assertEquals(dateFormat.parse(dateInput).getTime(), result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testParentheses()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        int value3 = generateRandomInteger();
        String cluase = "(" + value1 + " + " + value2 + ") * " + value3;
        Number result = ClauseEvaluator.evaluateNumericClause(cluase, new HashMap<String, Object>());
        Assert.assertEquals((value1 + value2) * value3, result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testParentheses2()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        int value3 = generateRandomInteger();
        String cluase = "(" + value1 + " - (" + value2 + " * " + value3 + "))";
        Number result = ClauseEvaluator.evaluateNumericClause(cluase, new HashMap<String, Object>());
        Assert.assertEquals(value1 - value2 * value3, result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testLongToDoubleConversion()
    {
        int value1 = generateRandomInteger();
        double value2 = generateRandomDouble();
        Number result = ClauseEvaluator.evaluateNumericClause(value1 + " + " + value2,
                                                              new HashMap<String, Object>());
        Assert.assertEquals(value1 + value2, result.getDoubleValue(), ERROR_RATE);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testDoubleToLongConversion()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        Number result = ClauseEvaluator.evaluateNumericClause(value1 + " + " + value2 + ".0",
                                                              new HashMap<String, Object>());
        Assert.assertEquals(value1 + value2, result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testAttributeRetrieval()
    {
        int value = generateRandomInteger();
        String attribute = "attribute";
        HashMap<String, Object> data = new HashMap<>();
        data.put(attribute, Integer.valueOf(value));
        Number result = ClauseEvaluator.evaluateNumericClause(attribute, data);
        Assert.assertEquals(value, result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testLessThanOrEqualTo()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        boolean result = ClauseEvaluator.evaluateBooleanClause(value1 + " <= " + value2,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 <= value2) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testGreaterThanOrEqualTo()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        boolean result = ClauseEvaluator.evaluateBooleanClause(value1 + " >= " + value2,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 >= value2) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testLessThan()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        boolean result = ClauseEvaluator.evaluateBooleanClause(value1 + " < " + value2,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 < value2) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testAnd()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        int value3 = generateRandomInteger();
        int value4 = generateRandomInteger();
        String clause = value1 + " < " + value2 + " and " + value3 + " < " + value4;
        boolean result = ClauseEvaluator.evaluateBooleanClause(clause,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 < value2 && value3 < value4) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testOr()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        int value3 = generateRandomInteger();
        int value4 = generateRandomInteger();
        String clause = value1 + " < " + value2 + " or " + value3 + " < " + value4;
        boolean result = ClauseEvaluator.evaluateBooleanClause(clause,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 < value2 || value3 < value4) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testRegression() throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String clause = "date '1998-12-01'";
        Number result = ClauseEvaluator.evaluateNumericClause(clause, new HashMap<String, Object>());
        Assert.assertEquals(dateFormat.parse("1998-12-01").getTime(), result.getLongValue());
    }

    @SuppressWarnings("static-method")
    @Test
    public void testBetween()
    {
        int value1 = generateRandomInteger();
        int value2 = generateRandomInteger();
        int value3 = generateRandomInteger();
        String clause = value1 + " between " + value2 + " and " + value3;
        boolean result = ClauseEvaluator.evaluateBooleanClause(clause,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value1 >= value2 && value1 <= value3) == result);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testRegression2()
    {
        double value = .06;
        String clause = "2 < 3 and " + value + " between .06 - 0.01 and .06 + 0.01 and " + "2 < 3";
        boolean result = ClauseEvaluator.evaluateBooleanClause(clause,
                                                               new HashMap<String, Object>());
        Assert.assertTrue((value >= (.06 - 0.01) && value <= (.06 + 0.01)) == result);
    }

    private static double generateRandomDouble()
    {
        return Math.random();
    }

    private static int generateRandomInteger()
    {
        return (int) (Math.random() * 1000);
    }
}
