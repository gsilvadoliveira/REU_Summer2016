package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.arrays.ArrayOperations;
import com.kivancmuslu.www.arrays.MapOperation;

import conTest.SharedOperations;

public class BreakageMap
{
    private final HashMap<Integer, ArrayList<Integer>> breakageMap_;
    private final HashMap<Integer, ArrayList<Integer>> reverseBreakageMap_;

    private void addBreakage(int queryIndex, int testIndex)
    {
        // I was lazy to fix the indexing starting at '1'.
        queryIndex--;
        testIndex--;

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Integer queryIndexObject = Integer.valueOf(queryIndex);
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Integer testIndexObject = Integer.valueOf(testIndex);

        updateMap(breakageMap_, queryIndexObject, testIndexObject);
        updateMap(reverseBreakageMap_, testIndexObject, queryIndexObject);
    }

    private static void updateMap(HashMap<Integer, ArrayList<Integer>> map, Integer key,
                                  Integer value)
    {
        if (!map.containsKey(key))
            map.put(key, new ArrayList<Integer>());
        map.get(key).add(value);
    }

    private final ArrayList<String> updates_;
    private final ArrayList<String> tests_;

    public BreakageMap(String updatePath, String testPath) throws FileNotFoundException
    {
        breakageMap_ = new HashMap<>();
        reverseBreakageMap_ = new HashMap<>();

        // Initialize the breakage map, hardcoded.
        addBreakage(1, 1);
        addBreakage(1, 5);

        addBreakage(2, 1);
        addBreakage(2, 2);
        addBreakage(2, 3);
        addBreakage(2, 4);
        addBreakage(2, 5);
        addBreakage(2, 6);
        addBreakage(2, 7);
        addBreakage(2, 8);

        addBreakage(3, 1);
        addBreakage(3, 2);
        addBreakage(3, 3);
        addBreakage(3, 4);
        addBreakage(3, 5);
        addBreakage(3, 6);
        addBreakage(3, 7);
        addBreakage(3, 8);

        addBreakage(4, 1);
        addBreakage(4, 2);
        addBreakage(4, 3);
        addBreakage(4, 4);
        addBreakage(4, 5);
        addBreakage(4, 6);
        addBreakage(4, 7);
        addBreakage(4, 8);

        addBreakage(5, 2);
        addBreakage(5, 3);
        addBreakage(5, 4);
        addBreakage(5, 6);

        addBreakage(6, 3);
        addBreakage(6, 4);
        addBreakage(6, 7);

        addBreakage(7, 4);

        addBreakage(8, 3);
        addBreakage(8, 4);
        addBreakage(8, 7);
        addBreakage(8, 9);

        addBreakage(9, 2);
        addBreakage(9, 3);
        addBreakage(9, 4);
        addBreakage(9, 6);
        addBreakage(9, 9);

        addBreakage(10, 9);

        addBreakage(11, 9);

        addBreakage(12, 2);
        addBreakage(12, 3);
        addBreakage(12, 4);
        addBreakage(12, 6);
        addBreakage(12, 10);

        addBreakage(13, 3);
        addBreakage(13, 4);
        addBreakage(13, 7);
        addBreakage(13, 10);

        addBreakage(14, 10);

        addBreakage(15, 1);
        addBreakage(15, 5);
        addBreakage(15, 10);

        addBreakage(16, 11);

        addBreakage(17, 11);

        addBreakage(18, 11);

        addBreakage(19, 11);

        addBreakage(20, 11);
        addBreakage(20, 12);

        addBreakage(21, 2);
        addBreakage(21, 3);
        addBreakage(21, 4);
        addBreakage(21, 6);
        addBreakage(21, 13);

        addBreakage(22, 3);
        addBreakage(22, 4);
        addBreakage(22, 7);
        addBreakage(22, 13);

        addBreakage(23, 13);

        addBreakage(24, 13);

        addBreakage(25, 1);
        addBreakage(25, 5);
        addBreakage(25, 13);

        addBreakage(26, 13);
        addBreakage(27, 13);
        addBreakage(28, 13);

        ArrayList<String> updates = SharedOperations.readQueries(new File(updatePath));
        updates_ = ArrayOperations.map(updates, new MapOperation<String, String>()
        {
            @Override
            public String map(@NonNull String update)
            {
                return SharedOperations.toOneLine(update);
            }
        });

        ArrayList<String> tests = SharedOperations.readQueries(new File(testPath));
        tests_ = ArrayOperations.map(tests, new MapOperation<String, String>()
        {
            @Override
            public String map(@NonNull String update)
            {
                return SharedOperations.toOneLine(update);
            }
        });
    }

    public boolean doesUpdateBreaksTest(String update, String test)
    {
        int updateIndex = updates_.indexOf(update);
        int testIndex = tests_.indexOf(test);
        if (updateIndex == -1 || testIndex == -1)
            throw new RuntimeException("Cannot find update or test in the corresponding pools.");

//        System.out.println("Searching for breakage: [" + updateIndex + ", " + testIndex + "]");

        ArrayList<Integer> tests = breakageMap_.get(Integer.valueOf(updateIndex));
        if (tests == null)
            return false;
//        System.out.println("Tests = " + Debug.join(tests, ", "));
        return tests.contains(Integer.valueOf(testIndex));
    }

}
