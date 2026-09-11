package eu.ai4work.sws.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Builder
@Data
public class SlidingDecisionAtomicResponse {
    private String id;
    private Map<String, ResultForOutputVariable> slidingDecisionOutputParameters;
    private SlidingDecisionExplanation decisionExplanation;
}
