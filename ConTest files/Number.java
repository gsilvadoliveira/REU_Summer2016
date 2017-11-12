package conTest.incremental;

public abstract class Number
{
    abstract double getDoubleValue();

    abstract long getLongValue();

    @Override
    public abstract String toString();

    public static Number add(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return new DoubleNumber(double1 + double2);
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return new LongNumber(long1 + long2);
    }

    public static Number subtract(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return new DoubleNumber(double1 - double2);
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return new LongNumber(long1 - long2);
    }

    public static Number divide(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return new DoubleNumber(double1 / double2);
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return new LongNumber(long1 / long2);
    }

    public static Number multiply(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return new DoubleNumber(double1 * double2);
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return new LongNumber(long1 * long2);
    }

    public static boolean lessThanOrEqualTo(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return double1 <= double2;
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return long1 <= long2;
    }

    public static boolean lessThan(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return double1 < double2;
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return long1 < long2;
    }

    public static boolean greaterThanOrEqualTo(Number n1, Number n2)
    {
        try
        {
            double double1 = n1.getDoubleValue();
            try
            {
                double double2 = n2.getDoubleValue();
                return double1 >= double2;
            }
            catch (PrecisionException e)
            {
                // Second number cannot be represented as a double.
            }
        }
        catch (PrecisionException e)
        {
            // First number cannot be represented as as a double.
        }

        long long1 = n1.getLongValue();
        long long2 = n2.getLongValue();
        return long1 >= long2;
    }
}
