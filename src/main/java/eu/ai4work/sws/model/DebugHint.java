package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DebugHint {
    UNKNOWN_INPUT("Invalid sliding decision input. Please check the provided parameter and try again"),
    UNEXPECTED_ERROR("An unexpected error occurred. Check the exception message or system logs for more details"),
    INVALID_FCL_FILE("Failed to parse the FCL file. Please check the file format and syntax");

    private final String debugHintMessage;
    DebugHint(String debugHintMessage) {
        this.debugHintMessage = debugHintMessage;
    }
    @JsonValue
    public String getDebugHintMessage() {
        return debugHintMessage;
    }
}
