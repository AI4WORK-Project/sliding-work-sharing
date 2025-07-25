package eu.ai4work.sws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlidingDecision {
    private String decisionResult;
    private SlidingDecisionExplanation decisionExplanation;
}
