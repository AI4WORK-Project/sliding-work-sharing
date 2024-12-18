package eu.ai4work.sws.model.generic;

import lombok.Data;
import java.util.Map;

@Data
public class SlidingDecisionResponse {
    private String decisionStatus;
    private Map<String, Object> decisionResults;

}