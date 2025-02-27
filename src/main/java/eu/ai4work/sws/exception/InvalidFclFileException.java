package eu.ai4work.sws.exception;

public class InvalidFclFileException extends RuntimeException {
    public InvalidFclFileException(String message, Exception exception) {
        super(message, exception);
    }
}
