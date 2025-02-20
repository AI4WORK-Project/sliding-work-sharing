package eu.ai4work.sws.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Builder
@Data
public class SlidingDecisionResponse {
    private SlidingDecisionStatus decisionStatus;
    private Map<String, Object> decisionResult;
    private Map<String, Object> decisionExplanation;
}
