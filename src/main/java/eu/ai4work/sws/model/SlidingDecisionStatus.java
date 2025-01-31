package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SlidingDecisionStatus {
    REQUEST("Sliding Decision Request"),
    RESPONSE("Sliding Decision Response"),
    ERROR("Error Occurred");

    private final String displayName;

    SlidingDecisionStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
