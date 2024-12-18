package eu.ai4work.sws.model.generic;

public enum SlidingDecisionResult {
    AI_AUTONOMOUSLY("AI can reschedule without human involvement"),
    HUMAN_ON_THE_LOOP("Human has to be informed about AI's rescheduling"),
    HUMAN_IN_THE_LOOP("Human has to check AI's suggestion"),
    HUMAN_MANUALLY("Human has to decide without AI support");

    private final String displayName;

    SlidingDecisionResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
