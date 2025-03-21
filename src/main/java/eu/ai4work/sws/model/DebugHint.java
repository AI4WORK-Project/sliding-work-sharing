package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DebugHint {
    @JsonProperty("Please check the provided sliding decision input parameter(s) and try again.")
    UNKNOWN_INPUT,
    @JsonProperty("An unexpected error occurred. Check the exception message or system logs for more details")
    UNEXPECTED_ERROR;
}
