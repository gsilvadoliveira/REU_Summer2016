package conTest.incremental;

public class LongNumber extends Number
{
    private long value_;

    public LongNumber()
    {
        this(0L);
    }

    public LongNumber(long initialValue)
    {
        value_ = initialValue;
    }

    @Override
    public double getDoubleValue()
    {
        double result = value_;
        if (result != value_)
            throw new PrecisionException("Precision lost while converting long to double: "
                                         + value_);
        return result;
    }

    @Override
    public long getLongValue()
    {
        return value_;
    }
    
    @Override
    public String toString()
    {
        return value_ + "";
    }
}
