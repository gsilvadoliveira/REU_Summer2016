package conTest.incremental;

public class DoubleNumber extends Number
{
    private double value_;

    public DoubleNumber()
    {
        this(0.0);
    }

    public DoubleNumber(double initialValue)
    {
        value_ = initialValue;
    }

    @Override
    public double getDoubleValue()
    {
        return value_;
    }

    @Override
    public long getLongValue()
    {
        long result = (long) value_;
        if (result != value_)
            throw new PrecisionException("Precision lost while converting double to long: "
                                         + value_);
        return result;
    }

    @Override
    public String toString()
    {
        return value_ + "";
    }
}
