package conTest.incremental;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public class CountComputer extends AggregateComputer<Integer>
{
    public CountComputer(String attribute, Integer previousValue, boolean previousResult,
                         boolean currentResult, Map<String, Object> previousData,
                         Map<String, Object> currentData)
    {
        super(attribute, previousValue, previousResult, currentResult, previousData, currentData);
    }

    @Override
    public Integer compute()
    {
        int result = getPreviousValue().intValue();
        if (getPreviousResult() && !getCurrentResult())
            result--;
        else if (!getPreviousResult() && getCurrentResult())
            result++;
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Integer res = Integer.valueOf(result);
        return res;
    }
}
