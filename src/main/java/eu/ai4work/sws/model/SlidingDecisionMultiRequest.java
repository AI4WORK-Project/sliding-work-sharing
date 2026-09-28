package eu.ai4work.sws.model;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class SlidingDecisionMultiRequest {
    private List<SlidingDecisionEachMultiRequest> requests;
    @Builder.Default
    private boolean includeDecisionExplanationsInResponse = false;
}
