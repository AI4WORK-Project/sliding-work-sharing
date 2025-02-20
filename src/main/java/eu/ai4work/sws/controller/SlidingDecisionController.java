package eu.ai4work.sws.controller;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.model.SlidingDecisionResult;
import eu.ai4work.sws.model.SlidingDecisionStatus;
import eu.ai4work.sws.model.SlidingDecisionRequest;
import eu.ai4work.sws.model.SlidingDecisionResponse;
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
    private static final String SLIDING_DECISION = "slidingDecision";
    private static final String DESCRIPTION = "description";
    private final SlidingDecisionService slidingDecisionService;
    private final ApplicationScenarioConfiguration applicationScenarioConfiguration;

    /**
     * Processes a sliding decision request by validating the input parameters from the sliding decision request,
     * calling the decision logic and returns a decision response.
     *
     * This method describes the "happy flow", while all exceptions that may potentially happen will be handled by the GlobalException handler.
     *
     * @param request The request body containing input parameters for decision process
     * @return SlidingDecisionResponse containing decision status and decision details
     */
    @PostMapping("/sliding-decision")
    public SlidingDecisionResponse processSlidingDecisionRequest(@RequestBody SlidingDecisionRequest request) throws Exception {
        assureInputParametersAreNotEmpty(request.getSlidingDecisionInputParameters());

        SlidingDecisionResult decisionResult = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());

        return createResponse(decisionResult);
    }

    /**
     * Creates a response based on the sliding decision result
     *
     * @param decisionResult Evaluated sliding decision result after applying the decision rules
     * @return SlidingDecisionResponse containing decision status and decision details
     */
    private SlidingDecisionResponse createResponse(SlidingDecisionResult decisionResult) {
        Map<String, Object> decisionResultDetails = new HashMap<>();
        decisionResultDetails.put(SLIDING_DECISION, decisionResult);
        decisionResultDetails.put(DESCRIPTION, applicationScenarioConfiguration.getDecisionResultsDescription().get(decisionResult.name()));

        return SlidingDecisionResponse.builder()
                .decisionStatus(SlidingDecisionStatus.RESPONSE)
                .decisionResult(decisionResultDetails)
                .decisionExplanation(slidingDecisionService.ruleEngineService.getSlidingDecisionExplanation())
                .build();
    }

    private void assureInputParametersAreNotEmpty(Map<String, Object> slidingDecisionInputParameters) {
        if (slidingDecisionInputParameters == null || slidingDecisionInputParameters.isEmpty()) {
            throw new IllegalArgumentException("The sliding decision input parameters must not be null or empty.");
        }
    }
}
