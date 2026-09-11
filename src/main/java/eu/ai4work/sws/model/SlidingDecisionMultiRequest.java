package eu.ai4work.sws.model;

import lombok.Data;

import java.util.List;

@Data
public class SlidingDecisionMultiRequest {
    private List<SlidingDecisionAtomicRequest> requests;
}
