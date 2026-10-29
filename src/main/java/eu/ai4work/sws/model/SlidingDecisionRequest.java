package eu.ai4work.sws.model;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
public class SlidingDecisionRequest {
    private Map<String, Object> slidingDecisionInputParameters;
    @Builder.Default
    private boolean includeDecisionExplanationsInResponse = false;
}
