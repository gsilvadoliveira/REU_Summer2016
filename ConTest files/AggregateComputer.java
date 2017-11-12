package conTest.incremental;

import java.util.Map;

public abstract class AggregateComputer<T>
{
    private final String attribute_;
    private final T previousValue_;
    private final boolean previousResult_;
    private final boolean currentResult_;

    private final Map<String, Object> previousData_;
    private final Map<String, Object> currentData_;

    protected AggregateComputer(String attribute, T previousValue, boolean previousResult,
                                boolean currentResult, Map<String, Object> previousData,
                                Map<String, Object> currentData)
    {
        attribute_ = attribute;
        previousValue_ = previousValue;
        previousResult_ = previousResult;
        currentResult_ = currentResult;

        previousData_ = previousData;
        currentData_ = currentData;
    }

    public boolean incrementallyCompute()
    {
        T newValue = compute();
        return newValue.equals(previousValue_);
    }

    protected abstract T compute();

    protected boolean getPreviousResult()
    {
        return previousResult_;
    }

    protected boolean getCurrentResult()
    {
        return currentResult_;
    }

    protected T getPreviousValue()
    {
        return previousValue_;
    }

    protected Map<String, Object> getPreviousData()
    {
        return previousData_;
    }

    protected Map<String, Object> getCurrentData()
    {
        return currentData_;
    }

    protected String getAttribute()
    {
        return attribute_;
    }
}