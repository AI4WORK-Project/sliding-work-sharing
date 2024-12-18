package eu.ai4work.sws.model.generic;

import lombok.Data;
import java.util.Map;

@Data
public class SlidingDecisionRequest {
    private String decisionStatus;
    private Map<String, Object> inputParameters;

}
