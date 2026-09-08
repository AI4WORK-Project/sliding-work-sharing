package eu.ai4work.sws.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SlidingDecisionMultiResponse {
    private SlidingDecisionStatus decisionStatus;
    private List<SlidingDecisionAtomicResponse> decisions;
}
