package conTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import org.gibello.zql.ParseException;
import org.junit.Test;

import conTest.StaticTriggerController.FutureTrigger;

public class StaticTriggerControllerTest extends StaticTriggerControllerTestBase
{
    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery1() throws IOException, ParseException, SQLException
    {
        String query = "select count(*) from actor;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("actor", "*"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery2() throws IOException, ParseException, SQLException
    {
        String query = "select count(*) from directors;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("directors", "*"));


        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery3() throws IOException, ParseException, SQLException
    {
        String query = "select temp.x from temp;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("temp", "x"));

        testQueryTriggers(query, expectedTriggers);
    }
}
