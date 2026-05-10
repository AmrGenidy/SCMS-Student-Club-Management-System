package scms.application.exception;

public class OverQuotaException extends Exception
{
    public OverQuotaException(String message)
    {
        super(message);
    }
}
