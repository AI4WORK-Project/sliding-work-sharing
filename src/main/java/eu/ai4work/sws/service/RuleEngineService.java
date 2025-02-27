package eu.ai4work.sws.service;

import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private static final String SUGGESTED_WORK_SHARING_APPROACH = "suggestedWorkSharingApproach";
    private final FIS fuzzyInferenceSystem;

    /**
     * Evaluates the rules based on the provided inputs and returns the sliding decision result.
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @return SlidingDecisionResult representing the outcome of the sliding decision.
     */
    public SlidingDecisionResult applySlidingDecisionRules(Map<String, Object> slidingDecisionInputParameters) {
        verifySlidingDecisionInputParameters(fuzzyInferenceSystem, slidingDecisionInputParameters);

        setInputParametersToFuzzyInferenceSystem(fuzzyInferenceSystem, slidingDecisionInputParameters);

        fuzzyInferenceSystem.evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getVariable(SUGGESTED_WORK_SHARING_APPROACH));

        return SlidingDecisionResult.valueOf(resultAsLinguisticTerm);
    }

    /**
     * Checks if any required sliding decision input parameters are unknown or missing.
     *
     * @param fuzzyInferenceSystem           The FIS instance to get the required input parameters.
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @throws InvalidInputParameterException if one or more input parameters are unknown or missing.
     */
    private void verifySlidingDecisionInputParameters(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters)
            throws InvalidInputParameterException {
        List<String> requiredParameters = getRequiredInputParametersFromFIS(fuzzyInferenceSystem);
        Set<String> providedParameters = slidingDecisionInputParameters.keySet();

        // detect provided input parameters that are not required
        List<String> unknownParameters = providedParameters.stream()
                .filter(providedParameter -> !requiredParameters.contains(providedParameter))
                .toList();

        // detect required parameters that are missing in the provided input
        List<String> missingParameters = requiredParameters.stream()
                .filter(requiredParameter -> !providedParameters.contains(requiredParameter))
                .toList();

        if (! (unknownParameters.isEmpty() && missingParameters.isEmpty())) {
            String exceptionMessage = "Invalid sliding decision input.";
            if (!unknownParameters.isEmpty()) {
                exceptionMessage += " - Unknown parameter(s): " + unknownParameters;
            }
            if (!missingParameters.isEmpty()) {
                exceptionMessage += " - Missing parameter(s): " + missingParameters;
            }
            throw new InvalidInputParameterException(exceptionMessage);
        }
    }

    private List<String> getRequiredInputParametersFromFIS(FIS fuzzyInferenceSystem) {
        return fuzzyInferenceSystem.getFunctionBlock(null)  // Get default function block
                // get all variables
                .getVariables().values().stream()
                // keep only input variables
                .filter(Variable::isInput)
                // extract variable names
                .map(Variable::getName)
                // convert to list
                .toList();
    }

    /**
     * Maps a fuzzy inference result to its corresponding linguistic term based on the highest membership degree.
     *
     * @param suggestedWorkSharingApproachAsFuzzyVariable The fuzzy output variable to evaluate.
     * @return The output as a linguistic term, i.e., the name of the membership function with the highest membership degree.
     */
    private String mapFuzzyInferenceResultToLinguisticTerm(Variable suggestedWorkSharingApproachAsFuzzyVariable) {
        return suggestedWorkSharingApproachAsFuzzyVariable.getLinguisticTerms().entrySet().stream()
                // Map each linguistic term to its corresponding membership degree
                .map(linguisticTermWithMembershipDegree -> Map.entry(
                        // The key is the linguistic term name
                        linguisticTermWithMembershipDegree.getKey(),
                        // The value is membership degree for the latest defuzzified value
                        linguisticTermWithMembershipDegree.getValue().getMembershipFunction()
                                .membership(suggestedWorkSharingApproachAsFuzzyVariable.getLatestDefuzzifiedValue())
                ))
                // Identify the linguistic term with the highest membership degree
                .max(Map.Entry.comparingByValue())
                // Retrieve the linguistic term name
                .get().getKey();
    }

    /**
     * Sets input parameters to the Fuzzy Inference System (FIS).
     *
     * @param fuzzyInferenceSystem           The FIS instance where input parameters will be set.
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     */
    private void setInputParametersToFuzzyInferenceSystem(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters) {
        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            fuzzyInferenceSystem.getVariable(parameterName).setValue(((Number) parameterValue).doubleValue());
        });
    }
}
