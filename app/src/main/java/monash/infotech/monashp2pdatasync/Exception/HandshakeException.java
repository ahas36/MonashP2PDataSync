package monash.infotech.monashp2pdatasync.Exception;

/**
 * Created by ahas36 on 25/02/16.
 */
public class HandshakeException extends Exception {
    public HandshakeException(String message)
    {
        super(message);
    }
    public HandshakeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
