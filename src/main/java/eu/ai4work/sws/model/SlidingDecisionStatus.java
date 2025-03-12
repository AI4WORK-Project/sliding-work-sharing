package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SlidingDecisionStatus {
    @JsonProperty("Sliding Decision Request")
    REQUEST,
    @JsonProperty("Sliding Decision Response")
    RESPONSE,
    @JsonProperty("Error - Sliding Decision not possible")
    ERROR;
}
