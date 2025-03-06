package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecisionExplanation;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.SlidingDecisionResult;
import eu.ai4work.sws.model.VariableExplanation;
import eu.ai4work.sws.model.RuleExplanation;
import eu.ai4work.sws.model.SlidingDecision;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private static final String SUGGESTED_WORK_SHARING_APPROACH = "suggestedWorkSharingApproach";
    private final FIS fuzzyInferenceSystem;

    /**
     * Evaluates the fuzzy inference rules based on the provided inputs, and it returns the sliding decision with its explanation.
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @return SlidingDecision containing the result and the explanation of the sliding decision.
     */
    public SlidingDecision applySlidingDecisionRules(Map<String, Object> slidingDecisionInputParameters) {
        verifySlidingDecisionInputParameters(fuzzyInferenceSystem, slidingDecisionInputParameters);

        setInputParametersToFuzzyInferenceSystem(fuzzyInferenceSystem, slidingDecisionInputParameters);

        fuzzyInferenceSystem.evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getVariable(SUGGESTED_WORK_SHARING_APPROACH));

        SlidingDecisionExplanation decisionExplanation = createSlidingDecisionExplanation(fuzzyInferenceSystem);

        return new SlidingDecision(SlidingDecisionResult.valueOf(resultAsLinguisticTerm), decisionExplanation);
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

    /**
     * Creates the explanation for the sliding decision.
     *
     * @return SlidingDecisionExplanation containing explanation of the input variables, applied rules and output variables.
     */
    private SlidingDecisionExplanation createSlidingDecisionExplanation(FIS fuzzyInferenceSystem) {
        var functionBlock = fuzzyInferenceSystem.getFunctionBlock(null); // selects the default function block
        return new SlidingDecisionExplanation(extractFuzzyVariableExplanation(functionBlock, Variable::isInput),
                getAppliedRules(functionBlock),
                extractFuzzyVariableExplanation(functionBlock, Variable::isOutput));
    }

    /**
     * extracts explanation for all fuzzy variables that match the given filter.
     */
    private Map<String, VariableExplanation> extractFuzzyVariableExplanation(FunctionBlock functionBlock, Predicate<Variable> variableFilter) {
        return functionBlock.getVariables().values().stream()
                .filter(variableFilter)
                // return the map with fuzzy variables name, value and linguistic terms (term name and membership value).
                .collect(Collectors.toMap(
                        Variable::getName,
                        fuzzyVariable -> new VariableExplanation(
                                fuzzyVariable.getValue(),
                                extractLinguisticTermNameAndMembershipValue(fuzzyVariable)
                        )));
    }

    private Map<String, Double> extractLinguisticTermNameAndMembershipValue(Variable fuzzyVariable) {
        return fuzzyVariable.getLinguisticTerms().entrySet().stream()
                // filter the linguistic term which has the membership value greater than 0
                .filter(linguisticTerm -> linguisticTerm.getValue().getMembershipFunction().membership(fuzzyVariable.getValue()) > 0)
                // return the map of linguistic name and membership value.
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // Linguistic term name
                        linguisticTerm -> linguisticTerm.getValue().getMembershipFunction().membership(fuzzyVariable.getValue())
                ));
    }

    private List<RuleExplanation> getAppliedRules(FunctionBlock functionBlock) {
        return functionBlock.getRuleBlocks().values().stream()
                .flatMap(ruleBlock -> ruleBlock.getRules().stream())
                // filters the rules with a degree of support greater than zero.
                .filter(rule -> rule.getDegreeOfSupport() > 0)
                .map(rule -> new RuleExplanation(rule.toString().replace("\t", " ")))
                .collect(Collectors.toList());
    }
}
