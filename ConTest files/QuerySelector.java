package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;

import org.eclipse.jdt.annotation.Nullable;

import conTest.SharedOperations;

public class QuerySelector
{
    public static void run(File selectPool, File updatePool, File outputFolder, boolean overwrite,
                           double selectPercentage, int noQueries, int runIndex)
    {
        File outputFile = new File(outputFolder, getOutputFileName(selectPercentage, noQueries,
                                                                   Integer.valueOf(runIndex)));
        if (outputFile.exists() && !overwrite)
        {
            System.out.println(outputFile.getAbsolutePath()
                               + " already exists and overwrite is set to false. "
                               + "Not running query selector.");
            return;
        }

        try
        {
            ArrayList<String> selectQueries = SharedOperations.readQueries(selectPool);
            ArrayList<String> updateQueries = SharedOperations.readQueries(updatePool);

            int noSelectQueries = (int) Math.round(noQueries * selectPercentage);
            int noUpdateQueries = noQueries - noSelectQueries;

            ArrayList<String> queries = new ArrayList<>();
            queries.addAll(subsample(selectQueries, noSelectQueries));
            queries.addAll(subsample(updateQueries, noUpdateQueries));
            Collections.shuffle(queries);

            try (Formatter writer = new Formatter(outputFile))
            {
                for (String query: queries)
                    writer.format("%s%s%n%n", query, ";");
            }

            System.out.println("Successfully selected " + noQueries + " queries with "
                               + selectPercentage + " select percentage.");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Cannot find the file for select or update pool.");
            e.printStackTrace(System.out);
        }
    }
    
    private static ArrayList<String> subsample(ArrayList<String> population, int noQueries)
    {
        ArrayList<String> randomizedPopulation = new ArrayList<>(population);
        Collections.shuffle(randomizedPopulation);

        int remainingNoQueries = noQueries;
        ArrayList<String> result = new ArrayList<>();
        while (remainingNoQueries > randomizedPopulation.size())
        {
            result.addAll(randomizedPopulation);
            remainingNoQueries -= randomizedPopulation.size();
        }
        result.addAll(randomizedPopulation.subList(0, remainingNoQueries));
        return result;
    }

    public static String getOutputFileName(double selectPercentage, int noQueries,
                                           @Nullable Integer runIndex)
    {
        long updatePercentage = Math.round((1 - selectPercentage) * 100);
        String updatePercentageS = "0." + updatePercentage;
        String result = noQueries + "_" + "s(" + selectPercentage + ")_" + "u(" + updatePercentageS
                        + ")";
        if (runIndex != null)
            result += "_run_" + runIndex;
        result += ".txt";
        return result;
    }
}
