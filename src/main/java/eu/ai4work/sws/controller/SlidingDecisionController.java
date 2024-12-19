package eu.ai4work.sws.controller;

import eu.ai4work.sws.model.generic.SlidingDecisionStatus;
import eu.ai4work.sws.model.generic.SlidingDecisionRequest;
import eu.ai4work.sws.model.generic.SlidingDecisionResponse;
import eu.ai4work.sws.service.SlidingDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SlidingDecisionController {
    private final SlidingDecisionService slidingDecisionService;

    @PostMapping("/sliding-decision")
    public SlidingDecisionResponse processSlidingDecisionRequest(@RequestBody SlidingDecisionRequest request) {

        SlidingDecisionResponse response = new SlidingDecisionResponse();
        response.setDecisionStatus(SlidingDecisionStatus.RESPONSE);

        Map<String, Object> decisionResults = slidingDecisionService.getSlidingDecision(request.getInputParameters());
        response.setDecisionResults(decisionResults);

        return response;
    }
}
