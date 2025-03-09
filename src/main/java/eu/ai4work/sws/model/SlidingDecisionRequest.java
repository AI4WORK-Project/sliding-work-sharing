package eu.ai4work.sws.model;

import lombok.Data;
import java.util.Map;

@Data
public class SlidingDecisionRequest {
    private Map<String, Object> slidingDecisionInputParameters;
}
