package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SlidingDecisionStatus {
    @JsonProperty("Sliding Decision Request")
    REQUEST,
    @JsonProperty("Sliding Decision Response")
    RESPONSE,
    @JsonProperty("Sliding Decision Multi-Request")
    MULTI_REQUEST,
    @JsonProperty("Sliding Decision Multi-Response")
    MULTI_RESPONSE,
    @JsonProperty("Error - Sliding Decision not possible")
    ERROR;
}
