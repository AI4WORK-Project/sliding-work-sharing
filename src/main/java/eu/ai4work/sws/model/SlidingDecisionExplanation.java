package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SlidingDecisionExplanation {
    INPUT_PARAMETERS("slidingDecisionInputParameters"),
    OUTPUT_PARAMETERS("slidingDecisionOutputParameters"),
    APPLIED_RULES("appliedRules"),
    VALUE("Value"),
    TERMS("Terms");

    private final String explanationString;

    SlidingDecisionExplanation(String explanationString) {
        this.explanationString = explanationString;
    }

    @JsonValue
    public String explanationString() {
        return explanationString;
    }
}
