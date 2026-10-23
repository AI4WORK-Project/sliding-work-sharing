package eu.ai4work.sws.controller;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.exception.InvalidRequestIdentifierException;
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
        SlidingDecision slidingDecision = getSingleSlidingDecision(request.getSlidingDecisionInputParameters());
        return createResponse(slidingDecision);
    }

    /**
     * Processes multiple sliding decision requests by validating the input parameters of each individual request,
     * calling the decision logic for each request and returning the decisions in one response.
     *
     * @param multiRequest The request body containing the input parameters for multiple decision requests plus an id for each request
     * @return SlidingDecisionMultiResponse containing decision status and a list of decisions (each containing id, decision details and explanations)
     */
    @PostMapping("/sliding-decision-multi-request")
    public SlidingDecisionMultiResponse processSlidingDecisionMultiRequest(@RequestBody SlidingDecisionMultiRequest multiRequest) {
        assureSubRequestIdsAreValid(multiRequest);

        // make a list all decisions
        List<SlidingDecisionMultiResponseElement> decisions = multiRequest.getRequests()
                .stream()
                .map(this::processMultiRequestSubElement)
                .toList();

        // wrap all decisions into one multi response
        return SlidingDecisionMultiResponse.builder()
                .decisionStatus(SlidingDecisionStatus.MULTI_RESPONSE)
                .decisions(decisions)
                .build();
    }

    private SlidingDecisionMultiResponseElement processMultiRequestSubElement(SlidingDecisionMultiRequestElement request) {
        String id = request.getId();

        try {
            SlidingDecision slidingDecision = getSingleSlidingDecision(request.getSlidingDecisionInputParameters());
            return createMultiResponseSubElementForOneDecision(id, slidingDecision);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Error in request with ID '" + id + "': " + exception.getMessage());
        } catch (InvalidInputParameterException exception) {
            throw new InvalidInputParameterException("Error in request with ID '" + id + "': " + exception.getMessage());
        }
    }

    /**
     * Gets a single sliding decision, provided that the given sliding decision input parameters are valid.
     */
    private SlidingDecision getSingleSlidingDecision(Map<String, Object> slidingDecisionInputParameters) {
        assureInputParametersAreNotEmpty(slidingDecisionInputParameters);
        return slidingDecisionService.getSlidingDecision(slidingDecisionInputParameters);
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
    private SlidingDecisionMultiResponseElement createMultiResponseSubElementForOneDecision(String id, SlidingDecision slidingDecision) {

        return SlidingDecisionMultiResponseElement.builder()
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

    private void assureSubRequestIdsAreValid(SlidingDecisionMultiRequest multiRequest) {
        Set<String> setOfIds = new HashSet<>();

        for (SlidingDecisionMultiRequestElement request : multiRequest.getRequests()) {
            String id = request.getId();

            if (id == null || id.isBlank()) {
                throw new InvalidRequestIdentifierException("Found a sliding decision request with an empty or missing ID. Each request requires a non-empty ID.");
            }
            // it returns false, if ID is already present
            if (!setOfIds.add(id)) {
                throw new InvalidRequestIdentifierException("The IDs of sliding decision requests must be unique. Found a duplicate ID: '" + id + "'");
            }
        }
    }
}
