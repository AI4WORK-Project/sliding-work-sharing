package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DebugHint {
    @JsonProperty("Please revise the sliding decision input parameter(s) and try again.")
    INVALID_INPUT_PARAMETERS,
    @JsonProperty("Please revise the IDs of the sliding decision requests and try again.")
    INVALID_ID,
    @JsonProperty("An unexpected error occurred. Check the exception message or system logs for more details")
    UNEXPECTED_ERROR;
}
