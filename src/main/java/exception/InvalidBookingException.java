package exception;

public class InvalidBookingException extends BookingException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
