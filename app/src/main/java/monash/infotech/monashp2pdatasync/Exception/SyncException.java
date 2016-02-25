package monash.infotech.monashp2pdatasync.Exception;

/**
 * Created by ahas36 on 25/02/16.
 */
public class SyncException extends Exception{
    public SyncException(String message)
    {
        super(message);
    }
    public SyncException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
