package conTest.incremental;

public class PrecisionException extends RuntimeException
{
    private static final long serialVersionUID = -6815767598326680284L;

    public PrecisionException(String message)
    {
        super(message);
    }
    
    public PrecisionException(String message, Throwable reason)
    {
        super(message, reason);
    }
}
