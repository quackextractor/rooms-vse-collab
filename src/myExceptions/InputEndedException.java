package myExceptions;

public class InputEndedException extends RuntimeException {
    public InputEndedException(String errorMessage) {
        super(errorMessage);
    }
}
