package eu.ai4work.sws.exception;

import eu.ai4work.sws.model.DebugHint;
import eu.ai4work.sws.model.SlidingDecisionStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String DEBUG_HINT = "debugHint";
    private static final String EXCEPTION_NAME = "exceptionName";
    private static final String EXCEPTION_MESSAGE = "exceptionMessage";
    private static final String DECISION_STATUS = "decisionStatus";
    private static final String ERROR_DETAILS = "errorDetails";
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidInputParameterException.class)
    public ResponseEntity<Map<String, Object>> handleUnknownInputParameterException(InvalidInputParameterException ex) {
        return createErrorResponse(ex, DebugHint.UNKNOWN_INPUT, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedExceptions(Exception ex) {
        logger.error("An unexpected exception occurred.", ex);
        return createErrorResponse(ex, DebugHint.UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(Exception exception, DebugHint debugHint, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(EXCEPTION_NAME, exception.getClass().getName());
        errorDetails.put(EXCEPTION_MESSAGE, exception.getMessage());
        errorDetails.put(DEBUG_HINT, debugHint);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(DECISION_STATUS, SlidingDecisionStatus.ERROR);
        errorResponse.put(ERROR_DETAILS, errorDetails);

        return new ResponseEntity<>(errorResponse, status);
    }
}
