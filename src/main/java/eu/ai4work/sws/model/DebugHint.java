package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DebugHint {
    UNKNOWN_INPUT("Please check the provided sliding decision input parameter(s) and try again."),
    UNEXPECTED_ERROR("An unexpected error occurred. Check the exception message or system logs for more details");

    private final String debugHintMessage;
    DebugHint(String debugHintMessage) {
        this.debugHintMessage = debugHintMessage;
    }
    @JsonValue
    public String getDebugHintMessage() {
        return debugHintMessage;
    }
}
