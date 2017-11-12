package conTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import org.gibello.zql.ParseException;
import org.junit.Test;

import conTest.StaticTriggerController.FutureTrigger;

// For these tests to pass, the program should connect to the TPCH benchmark database.
public class StaticTriggerControllerTPCHTest extends StaticTriggerControllerTestBase
{
    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery1() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_quantity) as sum_qty " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_quantity"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery2() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice) as sum_base_price " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery3() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice * (1 - l_discount)) " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_discount"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery4() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) as sum_charge " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_discount"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_tax"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery5() throws IOException, ParseException, SQLException
    {
        String query = "select avg(l_quantity) as avg_qty " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_quantity"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery6() throws IOException, ParseException, SQLException
    {
        String query = "select avg(l_extendedprice) as avg_price " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery7() throws IOException, ParseException, SQLException
    {
        String query = "select avg(l_discount) as avg_disc " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_discount"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_returnflag"));
        expectedTriggers.add(new FutureTrigger("LINEITEM", "l_linestatus"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery8() throws IOException, ParseException, SQLException
    {
        String query = "select count(*) as count_order " //
                       + "from LINEITEM " //
                       + "where l_shipdate <= date '1998-12-01' - interval '90 days' " //
                       + "group by l_returnflag, l_linestatus;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("LINEITEM", "*"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery9() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice * (1 - l_discount)) as revenue " //
                       + "from customer, orders, lineitem, supplier, nation, region " //
                       + "where c_custkey = o_custkey " //
                       + "and l_orderkey = o_orderkey " //
                       + "and l_suppkey = s_suppkey " //
                       + "and c_nationkey = s_nationkey " //
                       + "and s_nationkey = n_nationkey " //
                       + "and n_regionkey = r_regionkey " //
                       + "and r_name = 'ASIA' " //
                       + "and o_orderdate >= date '1994-01-01' " //
                       + "and o_orderdate < date '1994-01-01' + interval '1 year' " //
                       + "group by n_name;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("lineitem", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_discount"));
        expectedTriggers.add(new FutureTrigger("customer", "c_custkey"));
        expectedTriggers.add(new FutureTrigger("orders", "o_custkey"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_orderkey"));
        expectedTriggers.add(new FutureTrigger("orders", "o_orderkey"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_suppkey"));
        expectedTriggers.add(new FutureTrigger("supplier", "s_suppkey"));
        expectedTriggers.add(new FutureTrigger("customer", "c_nationkey"));
        expectedTriggers.add(new FutureTrigger("supplier", "s_nationkey"));
        expectedTriggers.add(new FutureTrigger("nation", "n_nationkey"));
        expectedTriggers.add(new FutureTrigger("nation", "n_regionkey"));
        expectedTriggers.add(new FutureTrigger("region", "r_regionkey"));
        expectedTriggers.add(new FutureTrigger("region", "r_name"));
        expectedTriggers.add(new FutureTrigger("orders", "o_orderdate"));
        expectedTriggers.add(new FutureTrigger("nation", "n_name"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery10() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice * l_discount) as revenue " //
                       + "from lineitem " //
                       + "where l_shipdate >= date '1994-01-01' " //
                       + "and l_shipdate < date '1994-01-01' + interval '1 year' " //
                       + "and l_discount between .06 - 0.01 and .06 + 0.01 " //
                       + "and l_quantity < 24;";
        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("lineitem", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_discount"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_quantity"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery11() throws IOException, ParseException, SQLException
    {
        String query = "select count(*) as high_line_count " //
                       + "from orders, lineitem " //
                       + "where o_orderkey = l_orderkey " //
                       + "and l_shipmode in ('MAIL', 'SHIP') " //
                       + "and l_commitdate < l_receiptdate " //
                       + "and l_shipdate < l_commitdate " //
                       + "and l_receiptdate >= date '1994-01-01' " //
                       + "and l_receiptdate < date '1994-01-01' + interval '1 year' " //
                       + "and (o_orderpriority = '1-URGENT' or o_orderpriority = '2-HIGH') " //
                       + "group by l_shipmode;";

        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("orders", "*"));
        expectedTriggers.add(new FutureTrigger("lineitem", "*"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery12() throws IOException, ParseException, SQLException
    {
        String query = "select count(*) as high_line_count " //
                       + "from orders, lineitem " //
                       + "where o_orderkey = l_orderkey " //
                       + "and l_shipmode in ('MAIL', 'SHIP') " //
                       + "and l_commitdate < l_receiptdate " //
                       + "and l_shipdate < l_commitdate " //
                       + "and l_receiptdate >= date '1994-01-01' " //
                       + "and l_receiptdate < date '1994-01-01' + interval '1 year' " //
                       + "and (o_orderpriority <> '1-URGENT' and o_orderpriority <> '2-HIGH') " //
                       + "group by l_shipmode;";

        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("orders", "*"));
        expectedTriggers.add(new FutureTrigger("lineitem", "*"));

        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery13() throws IOException, ParseException, SQLException
    {
        String query = "select " //
                       + "( select 100.00 * sum(l_extendedprice * (1 - l_discount)) " //
                       + " from lineitem, part " //
                       + "where l_partkey = p_partkey " //
                       + "and l_shipdate >= date '1995-09-01' " //
                       + "and l_shipdate < date '1995-09-01' + interval '1 month' " //
                       + "and p_type like 'PROMO%') " //
                       + "/ " //
                       + "( select sum(l_extendedprice * (1 - l_discount)) as promo_revenue " //
                       + "from lineitem, part " //
                       + "where l_partkey = p_partkey " //
                       + "and l_shipdate >= date '1995-09-01' " //
                       + "and l_shipdate < date '1995-09-01' + interval '1 month');";

        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("lineitem", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_discount"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_partkey"));
        expectedTriggers.add(new FutureTrigger("part", "p_partkey"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_shipdate"));
        expectedTriggers.add(new FutureTrigger("part", "p_type"));
                
        testQueryTriggers(query, expectedTriggers);
    }

    // Suppressed since test methods should be instance methods.
    @SuppressWarnings("static-method")
    @Test
    public void testQuery14() throws IOException, ParseException, SQLException
    {
        String query = "select sum(l_extendedprice* (1 - l_discount)) as revenue " //
                       + "from lineitem, part " //
                       + "where ( " //
                       + "p_partkey = l_partkey " //
                       + "and p_brand = 'Brand#12' " //
                       + "and p_container in ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG') " //
                       + "and l_quantity >= 1 and l_quantity <= 1 + 10 " //
                       + "and p_size between 1 and 5 " //
                       + "and l_shipmode in ('AIR', 'AIR REG') " //
                       + "and l_shipinstruct = 'DELIVER IN PERSON' ) " //
                       + "or ( " //
                       + "p_partkey = l_partkey " //
                       + "and p_brand = 'Brand#23' " //
                       + "and p_container in ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK') " //
                       + "and l_quantity >= 10 and l_quantity <= 10 + 10 " //
                       + "and p_size between 1 and 10 " //
                       + "and l_shipmode in ('AIR', 'AIR REG') " //
                       + "and l_shipinstruct = 'DELIVER IN PERSON' ) " //
                       + "or ( " //
                       + "p_partkey = l_partkey " //
                       + "and p_brand = 'Brand#34' " //
                       + "and p_container in ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG') " //
                       + "and l_quantity >= 20 and l_quantity <= 20 + 10 " //
                       + "and p_size between 1 and 15 " //
                       + "and l_shipmode in ('AIR', 'AIR REG') " //
                       + "and l_shipinstruct = 'DELIVER IN PERSON' );";

        HashSet<FutureTrigger> expectedTriggers = new HashSet<>();
        expectedTriggers.add(new FutureTrigger("lineitem", "l_extendedprice"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_discount"));
        expectedTriggers.add(new FutureTrigger("part", "p_partkey"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_partkey"));
        expectedTriggers.add(new FutureTrigger("part", "p_brand"));
        expectedTriggers.add(new FutureTrigger("part", "p_container"));
        expectedTriggers.add(new FutureTrigger("part", "p_partkey"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_quantity"));
        expectedTriggers.add(new FutureTrigger("part", "p_size"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_shipmode"));
        expectedTriggers.add(new FutureTrigger("lineitem", "l_shipinstruct"));

        testQueryTriggers(query, expectedTriggers);
    }

}
