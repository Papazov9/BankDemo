package exception;

public class MaximumBalanceExceededException extends RuntimeException {
    public MaximumBalanceExceededException(String message) {
        super(message);
    }
}
