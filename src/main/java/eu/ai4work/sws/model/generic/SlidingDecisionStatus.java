package eu.ai4work.sws.model.generic;

public enum SlidingDecisionStatus {
    REQUEST("Sliding Decision Request"),
    RESPONSE("Sliding Decision Response");

    private final String displayName;

    SlidingDecisionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
