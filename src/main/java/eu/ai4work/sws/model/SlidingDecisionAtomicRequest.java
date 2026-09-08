package eu.ai4work.sws.model;

import lombok.Data;
import java.util.Map;

@Data
public class SlidingDecisionAtomicRequest {
    private String id;
    private Map<String, Object> slidingDecisionInputParameters;
}
