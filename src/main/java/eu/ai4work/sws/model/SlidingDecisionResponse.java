package eu.ai4work.sws.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Builder
@Data
public class SlidingDecisionResponse {
    private SlidingDecisionStatus decisionStatus;
    private Map<String, ResultForOutputVariable> slidingDecisionOutputParameters;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SlidingDecisionExplanation decisionExplanation;
}
