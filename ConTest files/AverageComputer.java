package conTest.incremental;

import java.util.Map;

public class AverageComputer extends AggregateComputer<Double>
{
    public AverageComputer(String attribute, Double previousValue, boolean previousResult,
                              boolean currentResult, Map<String, Object> previousData,
                              Map<String, Object> currentData)
    {
        super(attribute, previousValue, previousResult, currentResult, previousData, currentData);
    }

    @Override
    protected Double compute()
    {
        throw new RuntimeException("Not supported yet.");
    }
}
