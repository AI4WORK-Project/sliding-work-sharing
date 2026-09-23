package eu.ai4work.sws.controller;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.*;
import eu.ai4work.sws.service.SlidingDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
        boolean includeDecisionExplanationsInResponse = request.isIncludeDecisionExplanationsInResponse();

        SlidingDecision slidingDecision = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());

        return createResponse(slidingDecision, includeDecisionExplanationsInResponse);
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
        assureIdsAreValid(multiRequest);
        boolean includeDecisionExplanationsInResponse = multiRequest.isIncludeDecisionExplanationsInResponse();

        // make a list all decisions
        List<SlidingDecisionEachMultiResponse> decisions = multiRequest.getRequests()
                .stream()
                .map(this::processEachMultiRequest)
                .toList();

        // wrap all decisions into one multi response
        return SlidingDecisionMultiResponse.builder()
                .decisionStatus(SlidingDecisionStatus.MULTI_RESPONSE)
                .decisions(decisions)
                .build();
    }

    private SlidingDecisionEachMultiResponse processEachMultiRequest(SlidingDecisionEachMultiRequest request) {
        String id = request.getId();

        try {
            assureInputParametersAreNotEmpty(request.getSlidingDecisionInputParameters());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Error in request with ID '" + id + "': " + exception.getMessage());
        }

        try {
            SlidingDecision slidingDecision = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());
            return createEachMultiResponse(id, slidingDecision, includeDecisionExplanationInRepsonse);
        } catch (InvalidInputParameterException exception) {
            throw new InvalidInputParameterException("Error in request with ID '" + id + "': " + exception.getMessage());
        }
    }

    /**
     * build the results grouped by output-variable name,
     * where each output variable contains the result in linguistic term and human-readable description of the result
     *
     * @param slidingDecision calculated sliding decision
     * @return results grouped by output-variable name
     */
    private Map<String, ResultForOutputVariable> buildResultsByOutputVariables(SlidingDecision slidingDecision) {
        Map<String, ResultForOutputVariable> resultsByOutputVariables = new HashMap<>();

        slidingDecision.getDecisionResultPerOutputParameter().forEach((outputVariableName, resultAsLinguisticTerm) -> {
            ResultForOutputVariable resultForOutputVariable = new ResultForOutputVariable();
            resultForOutputVariable.setSlidingDecision(resultAsLinguisticTerm);
            resultForOutputVariable.setDescription(applicationScenarioConfiguration.getDecisionResultsDescription().get(resultAsLinguisticTerm));
            resultsByOutputVariables.put(outputVariableName, resultForOutputVariable);
        });

        return resultsByOutputVariables;
    }

    /**
     * Creates a response based on the sliding decision
     *
     * @param slidingDecision Evaluated sliding decision after applying the decision rules
     * @return SlidingDecisionResponse containing decision status, decision details and decision explanation.
     */
    private SlidingDecisionResponse createResponse(SlidingDecision slidingDecision, boolean includeDecisionExplanationsInResponse) {
        if(includeDecisionExplanationsInResponse) {
            return SlidingDecisionResponse.builder()
                    .decisionStatus(SlidingDecisionStatus.RESPONSE)
                    .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                    .decisionExplanation(slidingDecision.getDecisionExplanation())
                    .build();
        } else {
            return SlidingDecisionResponse.builder()
                    .decisionStatus(SlidingDecisionStatus.RESPONSE)
                    .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                    .build();
        }
    }

    /**
     * Creates the response for one individual sliding-decision request.
     *
     * @param id              ID of the original request
     * @param slidingDecision Evaluated sliding decision after applying the decision rules
     * @return response containing the id, decision results and explanation
     */
    private SlidingDecisionEachMultiResponse createEachMultiResponse(String id, SlidingDecision slidingDecision, boolean includeDecisionExplanationsInResponse) {
        if (includeDecisionExplanationsInResponse) {
            return SlidingDecisionEachMultiResponse.builder()
                    .id(id)
                    .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                    .decisionExplanation(slidingDecision.getDecisionExplanation())
                    .build();
        } else {
            return SlidingDecisionEachMultiResponse.builder()
                    .id(id)
                    .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                    .build();
        }
    }

    private void assureInputParametersAreNotEmpty(Map<String, Object> slidingDecisionInputParameters) {
        if (slidingDecisionInputParameters == null || slidingDecisionInputParameters.isEmpty()) {
            throw new IllegalArgumentException("The sliding decision input parameters must not be null or empty.");
        }
    }

    private void assureIdsAreValid(SlidingDecisionMultiRequest multiRequest) {
        Set<String> setOfIds = new HashSet<>();

        for (SlidingDecisionEachMultiRequest request : multiRequest.getRequests()) {
            String id = request.getId();

            if (id == null || id.isBlank()) {
                throw new InvalidInputParameterException("The ID in sliding decision request must not be empty.");
            }
            // it returns false, if ID is already present
            if (!setOfIds.add(id)) {
                throw new InvalidInputParameterException("The IDs in sliding decision request must be unique. Duplicate ID: '" + id + "'");
            }
        }
    }
}
