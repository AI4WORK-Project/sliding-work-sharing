package eu.ai4work.sws.controller;

import eu.ai4work.sws.model.generic.SlidingDecisionResult;
import eu.ai4work.sws.model.generic.SlidingDecisionStatus;
import eu.ai4work.sws.model.generic.SlidingDecisionRequest;
import eu.ai4work.sws.model.generic.SlidingDecisionResponse;
import eu.ai4work.sws.service.SlidingDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SlidingDecisionController {
    private final SlidingDecisionService slidingDecisionService;

    @PostMapping("/sliding-decision")
    public SlidingDecisionResponse processSlidingDecisionRequest(@RequestBody SlidingDecisionRequest request) {
        SlidingDecisionResponse response = new SlidingDecisionResponse();
        response.setDecisionStatus(SlidingDecisionStatus.RESPONSE);

        SlidingDecisionResult decisionResult = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());

        Map<String, Object> decisionResults = new HashMap<>();
        decisionResults.put("slidingDecision", decisionResult);
        decisionResults.put("description", decisionResult.getDisplayName());

        response.setDecisionResults(decisionResults);

        return response;
    }
}
