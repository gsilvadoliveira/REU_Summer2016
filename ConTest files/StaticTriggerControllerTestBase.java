package conTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import org.gibello.zql.ParseException;
import org.junit.Assert;

import conTest.StaticTriggerController.FutureTrigger;

public abstract class StaticTriggerControllerTestBase
{
    protected static void testQueryTriggers(String query, HashSet<FutureTrigger> expectedTriggers)
        throws IOException, ParseException, SQLException
    {
        HashSet<FutureTrigger> triggers = StaticTriggerController.computeTriggersForQuery(query);
        assertSetsAreEqual(expectedTriggers, triggers);
    }

    private static <T> void assertSetsAreEqual(HashSet<T> expectedSet, HashSet<T> set)
    {
        Assert.assertEquals("Sets have different size.", expectedSet.size(), set.size());

        for (T expected: expectedSet)
            Assert.assertTrue("Expected element: " + expected + " does not exist in the result.",
                              set.contains(expected));
    }
}
