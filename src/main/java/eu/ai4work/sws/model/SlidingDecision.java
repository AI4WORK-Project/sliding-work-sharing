package eu.ai4work.sws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SlidingDecision {
    private Map<String, String> decisionResult;
    private SlidingDecisionExplanation decisionExplanation;
}
