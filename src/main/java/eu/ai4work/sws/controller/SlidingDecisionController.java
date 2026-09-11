package eu.ai4work.sws.controller;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.model.*;
import eu.ai4work.sws.service.SlidingDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class SlidingDecisionController {
    private final SlidingDecisionService slidingDecisionService;
    private final ApplicationScenarioConfiguration applicationScenarioConfiguration;

    /**
     * Processes a sliding decision request by validating the input parameters from the sliding decision request,
     * calling the decision logic and returns a decision response.
     *
     * This method describes the "happy flow", while all exceptions that may potentially happen will be handled by the GlobalException handler.
     *
     * @param request The request body containing input parameters for decision process
     * @return SlidingDecisionResponse containing decision status, decision details and decision explanation.
     */
    @PostMapping("/sliding-decision")
    public SlidingDecisionResponse processSlidingDecisionRequest(@RequestBody SlidingDecisionRequest request) {
        assureInputParametersAreNotEmpty(request.getSlidingDecisionInputParameters());

        SlidingDecision slidingDecision = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());

        return createResponse(slidingDecision);
    }

    /**
     * Processes multiple sliding decision requests by validating the input parameters from each sliding decision request,
     * calling for each request the decision logic and returns the decisions in one response.
     *
     * @param multiRequest The request body containing the input parameters for multiple decision requests
     * @return SlidingDecisionMultiResponse containing decision status and a list decisions (containing decision details and explanations)
     */
    @PostMapping("/sliding-decision-multi-request")
    public SlidingDecisionMultiResponse processSlidingDecisionMultiRequest(@RequestBody SlidingDecisionMultiRequest multiRequest) {
        List<Map.Entry<String, SlidingDecision>> slidingDecisionList = new ArrayList<>();

        List<SlidingDecisionAtomicRequest> requests = multiRequest.getRequests();
        for (SlidingDecisionAtomicRequest request : requests) {
            assureInputParametersAreNotEmpty(request.getSlidingDecisionInputParameters());

            String id =  request.getId();
            SlidingDecision slidingDecision = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());
            slidingDecisionList.add(new AbstractMap.SimpleEntry<>(id, slidingDecision));
        }

        return createMultiResponse(slidingDecisionList);
    }

    /**
     * Creates a response based on the sliding decision
     *
     * @param slidingDecision Evaluated sliding decision after applying the decision rules
     * @return SlidingDecisionResponse containing decision status, decision details and decision explanation.
     */
    private SlidingDecisionResponse createResponse(SlidingDecision slidingDecision) {
        Map<String, ResultForOutputVariable> resultsByOutputVariables = new HashMap<>();

        slidingDecision.getDecisionResultPerOutputParameter().forEach((outputVariableName, resultAsLinguisticTerm) -> {
            ResultForOutputVariable resultForOutputVariable = new ResultForOutputVariable();
            resultForOutputVariable.setSlidingDecision(resultAsLinguisticTerm);
            resultForOutputVariable.setDescription(applicationScenarioConfiguration.getDecisionResultsDescription().get(resultAsLinguisticTerm));
            resultsByOutputVariables.put(outputVariableName, resultForOutputVariable);
        });

        return SlidingDecisionResponse.builder()
                .decisionStatus(SlidingDecisionStatus.RESPONSE)
                .slidingDecisionOutputParameters(resultsByOutputVariables)
                .decisionExplanation(slidingDecision.getDecisionExplanation())
                .build();
    }

    private SlidingDecisionMultiResponse createMultiResponse(List<Map.Entry<String, SlidingDecision>> slidingDecisionList) {
        List<SlidingDecisionAtomicResponse> decisions = new ArrayList<>();

        for (Map.Entry<String, SlidingDecision> slidingDecisionEntry : slidingDecisionList) {
            Map<String, ResultForOutputVariable> resultsByOutputVariables = new HashMap<>();

            String id  = slidingDecisionEntry.getKey();
            SlidingDecision slidingDecision = slidingDecisionEntry.getValue();

            slidingDecision.getDecisionResultPerOutputParameter().forEach((outputVariableName, resultAsLinguisticTerm) -> {
                ResultForOutputVariable resultForOutputVariable = new ResultForOutputVariable();
                resultForOutputVariable.setSlidingDecision(resultAsLinguisticTerm);
                resultForOutputVariable.setDescription(applicationScenarioConfiguration.getDecisionResultsDescription().get(resultAsLinguisticTerm));
                resultsByOutputVariables.put(outputVariableName, resultForOutputVariable);
            });

            SlidingDecisionAtomicResponse slidingDecisionAtomicResponse = SlidingDecisionAtomicResponse.builder()
                .id(id)
                .slidingDecisionOutputParameters(resultsByOutputVariables)
                .decisionExplanation(slidingDecision.getDecisionExplanation())
                .build();

            decisions.add(slidingDecisionAtomicResponse);
        }

        return SlidingDecisionMultiResponse.builder()
                .decisionStatus(SlidingDecisionStatus.MULTI_RESPONSE)
                .decisions(decisions)
                .build();
    }

    private void assureInputParametersAreNotEmpty(Map<String, Object> slidingDecisionInputParameters) {
        if (slidingDecisionInputParameters == null || slidingDecisionInputParameters.isEmpty()) {
            throw new IllegalArgumentException("The sliding decision input parameters must not be null or empty.");
        }
    }
}
