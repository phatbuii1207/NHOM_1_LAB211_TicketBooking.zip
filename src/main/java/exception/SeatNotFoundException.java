package exception;

public class SeatNotFoundException extends BookingException {
    public SeatNotFoundException(String message) {
        super(message);
    }
}
