package conTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import org.gibello.zql.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import conTest.StaticTriggerController.FutureTrigger;

// For these tests to pass, program should be connected to the IMDB database.
@Ignore public class StaticTriggerControllerIMDBTest extends StaticTriggerControllerTestBase
{
    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery1() throws IOException, ParseException, SQLException
    {
        //@formatter:off
        String query = "select count(distinct mid) " +  
                       "from actor a, casts c " + 
                       "where a.fname = 'Kevin' " +
                           "and a.lname = 'Bacon' " + 
                           "and c.pid = a.id;";
        //@formatter:on
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("casts", "mid"));
        expectedTriggers.add(new FutureTrigger("actor", "fname"));
        expectedTriggers.add(new FutureTrigger("actor", "lname"));
        expectedTriggers.add(new FutureTrigger("casts", "pid"));
        expectedTriggers.add(new FutureTrigger("actor", "id"));

        testQueryTriggers(query, expectedTriggers);
    }
}
