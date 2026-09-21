package eu.ai4work.sws.controller;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.*;
import eu.ai4work.sws.service.SlidingDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
        assureIdsAreValid(multiRequest);

        // process every sliding-decision request
        List<SlidingDecisionEachMultiResponse> decisions = new ArrayList<>();
        for (SlidingDecisionEachMultiRequest request : multiRequest.getRequests()) {
            String id =  request.getId();
            SlidingDecision slidingDecision;

            try {
                assureInputParametersAreNotEmpty(request.getSlidingDecisionInputParameters());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Error in request with following ID '"+id+"': "+exception.getMessage());
            }
            try {
                slidingDecision = slidingDecisionService.getSlidingDecision(request.getSlidingDecisionInputParameters());
            } catch (InvalidInputParameterException exception) {
                throw new InvalidInputParameterException("Error in request with following ID '"+id+"': "+exception.getMessage());
            }
            decisions.add(createEachMultiResponse(request.getId(), slidingDecision));
        }

        // wrap all individual decisions into one multi response
        return SlidingDecisionMultiResponse.builder()
                .decisionStatus(SlidingDecisionStatus.MULTI_RESPONSE)
                .decisions(decisions)
                .build();
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
    private SlidingDecisionResponse createResponse(SlidingDecision slidingDecision) {

        return SlidingDecisionResponse.builder()
                .decisionStatus(SlidingDecisionStatus.RESPONSE)
                .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                .decisionExplanation(slidingDecision.getDecisionExplanation())
                .build();
    }

    /**
     * Creates the response for one individual sliding-decision request.
     *
     * @param id              ID of the original request
     * @param slidingDecision Evaluated sliding decision after applying the decision rules
     * @return response containing the id, decision results and explanation
     */
    private SlidingDecisionEachMultiResponse createEachMultiResponse(String id, SlidingDecision slidingDecision) {

        return SlidingDecisionEachMultiResponse.builder()
                .id(id)
                .slidingDecisionOutputParameters(buildResultsByOutputVariables(slidingDecision))
                .decisionExplanation(slidingDecision.getDecisionExplanation())
                .build();
    }

    private void assureInputParametersAreNotEmpty(Map<String, Object> slidingDecisionInputParameters) {
        if (slidingDecisionInputParameters == null || slidingDecisionInputParameters.isEmpty()) {
            throw new IllegalArgumentException("The sliding decision input parameters must not be null or empty.");
        }
    }

    private void assureIdsAreValid(SlidingDecisionMultiRequest multiRequest) {
        List<String> ids = multiRequest.getRequests()
                .stream().map(SlidingDecisionEachMultiRequest::getId).toList();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                throw new InvalidInputParameterException("The request ID must not be empty.");
            }
            if (Collections.frequency(ids, id)>1) {
                throw new InvalidInputParameterException("The request IDs must be unique for each response.");
            }
        }
    }
}
