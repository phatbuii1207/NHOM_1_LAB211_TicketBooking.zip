package exception;

public class OptimisticLockConflictException extends BookingException {
    public OptimisticLockConflictException(String message) {
        super(message);
    }
}
