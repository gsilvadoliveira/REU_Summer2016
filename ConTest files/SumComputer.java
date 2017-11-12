package conTest.incremental;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public class SumComputer extends AggregateComputer<Double>
{
    public SumComputer(String attribute, Double previousValue, boolean previousResult,
                       boolean currentResult, Map<String, Object> previousData,
                       Map<String, Object> newData)
    {
        super(attribute, previousValue, previousResult, currentResult, previousData, newData);
    }

    @Override
    protected Double compute()
    {
        double previousDataValue =
            ClauseEvaluator.evaluateNumericClause(getAttribute(), getPreviousData())
                           .getDoubleValue();
        double newDataValue =
            ClauseEvaluator.evaluateNumericClause(getAttribute(), getCurrentData())
                           .getDoubleValue();

        double result = getPreviousValue().doubleValue();
        if (getPreviousResult() && getCurrentResult())
            result += (newDataValue - previousDataValue);
        else if (getPreviousResult() && !getCurrentResult())
            result -= previousDataValue;
        else if (!getPreviousResult() && getCurrentResult())
            result += newDataValue;
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Double res = Double.valueOf(result);
        return res;
    }
}