package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecisionExplanation;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.VariableExplanation;
import eu.ai4work.sws.model.RuleExplanation;
import eu.ai4work.sws.model.SlidingDecision;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private final FIS fuzzyInferenceSystem;
    private final List<String> requiredFuzzyInputParameters;
    private final List<String> outputVariableNamesFromFIS;

    /**
     * Evaluates the fuzzy inference rules based on the provided inputs, and it returns the sliding decision with its explanation.
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @return SlidingDecision containing the result and the explanation of the sliding decision.
     */
    public SlidingDecision applySlidingDecisionRules(Map<String, Object> slidingDecisionInputParameters) {

        verifySlidingDecisionInputParameters(slidingDecisionInputParameters);

        setInputParametersToFuzzyInferenceSystem(slidingDecisionInputParameters);

        fuzzyInferenceSystem.evaluate();

        Map<String, String> allDecisionResult = readAllSlidingDecisionResultsFromFIS();

        SlidingDecisionExplanation decisionExplanation = readSlidingDecisionExplanationFromFuzzyInferenceSystem();

        return new SlidingDecision(allDecisionResult, decisionExplanation);
    }

    /**
     * Checks if any required sliding decision input parameters are unknown or missing.
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @throws InvalidInputParameterException if one or more input parameters are unknown or missing.
     */
    private void verifySlidingDecisionInputParameters(Map<String, Object> slidingDecisionInputParameters)
            throws InvalidInputParameterException {
        Set<String> providedParameters = slidingDecisionInputParameters.keySet();

        // detect provided input parameters that are not required
        List<String> unknownParameters = providedParameters.stream()
                .filter(providedParameter -> !requiredFuzzyInputParameters.contains(providedParameter))
                .toList();

        // detect required parameters that are missing in the provided input
        List<String> missingParameters = requiredFuzzyInputParameters.stream()
                .filter(requiredParameter -> !providedParameters.contains(requiredParameter))
                .toList();

        if (!(unknownParameters.isEmpty() && missingParameters.isEmpty())) {
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

    /**
     * Reads all sliding decision results to their corresponding linguistic terms from the fuzzy inference system.
     *
     * @return Map of sliding decision results which contains output variable names and
     * to their result in linguistic term (the name of the membership function with the highest membership degree).
     */
    private Map<String, String> readAllSlidingDecisionResultsFromFIS() {
        Map<String, String> resultsByOutputVariable = new HashMap<>();
        for (String outputVariableNameFromFIS : outputVariableNamesFromFIS) {
            resultsByOutputVariable.put(outputVariableNameFromFIS, getLinguisticTermForOutputVariable(outputVariableNameFromFIS));
        }
        return resultsByOutputVariable;
    }

    private String getLinguisticTermForOutputVariable(String outputVariableNameFromFIS) {
        Variable resultAsFuzzyVariable = fuzzyInferenceSystem.getVariable(outputVariableNameFromFIS);
        return resultAsFuzzyVariable.getLinguisticTerms().entrySet().stream()
                // Map each linguistic term to its corresponding membership degree
                .map(linguisticTermWithMembershipDegree -> Map.entry(
                        // The key is the linguistic term name
                        linguisticTermWithMembershipDegree.getKey(),
                        // The value is membership degree for the latest defuzzified value
                        linguisticTermWithMembershipDegree.getValue().getMembershipFunction()
                                .membership(resultAsFuzzyVariable.getLatestDefuzzifiedValue())
                ))
                // Identify the linguistic term with the highest membership degree
                .max(Map.Entry.comparingByValue())
                // Retrieve the linguistic term name
                .get().getKey();
    }

    /**
     * Sets input parameters to the Fuzzy Inference System (FIS).
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     */
    private void setInputParametersToFuzzyInferenceSystem(Map<String, Object> slidingDecisionInputParameters) {
        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            if (parameterValue instanceof Number parameterValueAsNumber) {
                fuzzyInferenceSystem.getVariable(parameterName).setValue(parameterValueAsNumber.doubleValue());
            } else {
                throw new InvalidInputParameterException(
                        "Invalid sliding decision input: The parameter '" + parameterName + "' must be a number."
                );
            }
        });
    }

    /**
     * Reads the explanation for the sliding decision.
     *
     * @return SlidingDecisionExplanation containing explanation of the input variables, applied rules and output variables.
     */
    private SlidingDecisionExplanation readSlidingDecisionExplanationFromFuzzyInferenceSystem() {
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
                .map(rule -> new RuleExplanation(
                        rule.getName(),
                        "IF " + rule.getAntecedents().toString(),
                        "THEN " + rule.getConsequents().toString(),
                        Double.toString(rule.getWeight()),
                        Double.toString(rule.getDegreeOfSupport())
                ))
                .collect(Collectors.toList());
    }
}
