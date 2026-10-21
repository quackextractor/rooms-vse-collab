package myExceptions;

public class ParsingLevelException extends Exception {
    public ParsingLevelException(String errorMessage) {
        super(errorMessage);
    }

    public ParsingLevelException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
