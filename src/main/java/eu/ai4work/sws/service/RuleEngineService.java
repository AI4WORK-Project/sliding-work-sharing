package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.InvalidFclFileException;
import eu.ai4work.sws.model.SlidingDecisionResult;
import eu.ai4work.sws.exception.UnknownInputParameterException;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private static final String SUGGESTED_WORK_SHARING_APPROACH = "suggestedWorkSharingApproach";
    private final Logger logger = LogManager.getLogger(RuleEngineService.class);
    private final ApplicationScenarioConfiguration applicationScenarioConfiguration;

    /**
     * Evaluates the rules based on the provided inputs and returns the sliding decision result.
     *
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @return SlidingDecisionResult representing the outcome of the sliding decision.
     * @throws Exception if there is an issue in process of initializing the FCL file or an evaluating the FCL rules.
     */
    public SlidingDecisionResult applySlidingDecisionRules(Map<String, Object> slidingDecisionInputParameters) throws Exception {
        FIS fuzzyInferenceSystem = initializeFuzzyInferenceSystem(applicationScenarioConfiguration.getFclRulesFilePath());

        setInputParametersToFuzzyInterfaceSystem(fuzzyInferenceSystem, slidingDecisionInputParameters);

        fuzzyInferenceSystem.evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getVariable(SUGGESTED_WORK_SHARING_APPROACH));

        // log explanation of sliding decision
        String slidingDecisionExplanation = generateSlidingDecisionExplanation(fuzzyInferenceSystem);
        logger.debug(slidingDecisionExplanation);

        return SlidingDecisionResult.valueOf(resultAsLinguisticTerm);
    }

    /**
     * Initializes a Fuzzy Inference System (FIS) based on Fuzzy Control Language (FCL) rules file
     *
     * @param fclRulesFilePath The file path of the FCL rules file.
     * @return Initialized FIS object.
     * @throws FileNotFoundException If the FCL file is not found.
     * @throws InvalidFclFileException If the FCL file cannot be parsed.
     */
    private FIS initializeFuzzyInferenceSystem(String fclRulesFilePath) throws FileNotFoundException, InvalidFclFileException {
        URL fuzzyLogicRulesResourceUrl = getClass().getClassLoader().getResource(fclRulesFilePath);
        if (fuzzyLogicRulesResourceUrl == null) {
            throw new FileNotFoundException("Fuzzy Control Language (FCL) file not found: " + fclRulesFilePath);
        }

        FIS fuzzyInferenceSystem = FIS.load(fuzzyLogicRulesResourceUrl.getPath(), false); // verbose set to 'false' because to avoid GUI-related processing
        if (fuzzyInferenceSystem == null) {
            throw new InvalidFclFileException("Failed to parse Fuzzy Control Language (FCL) file: " + fclRulesFilePath);
        }

        logger.debug("Successfully initialized FIS from file: {}", fclRulesFilePath);
        return fuzzyInferenceSystem;
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
     * @throws UnknownInputParameterException if an input parameter is not recognized by the FIS.
     */
    private void setInputParametersToFuzzyInterfaceSystem(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters) throws UnknownInputParameterException {
        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            Variable fuzzyVariableForParameter = fuzzyInferenceSystem.getVariable(parameterName);
            if (fuzzyVariableForParameter != null) {
                fuzzyVariableForParameter.setValue(((Number) parameterValue).doubleValue());
            } else {
                throw new UnknownInputParameterException("The following sliding decision input parameter is unknown: " + parameterName);
            }
        });
    }

    /**
     * Provide a basic explanation of the sliding decision by showing membership values and rules that were applied.
     *
     * @param fuzzyInferenceSystem the fuzzy inference system to obtain the explanation.
     * @return a formatted String containing the overall explanation.
     */
    private String generateSlidingDecisionExplanation(FIS fuzzyInferenceSystem) {
        var functionBlock = fuzzyInferenceSystem.getFunctionBlock(null);
        return "Sliding Decision Explanation\n" +
               "Sliding Decision Input Parameters:\n" + generateSlidingDecisionFuzzyVariableExplanation(functionBlock, Variable::isInput) + "\n" +
               "Suggested Work Sharing Parameters:\n" + generateSlidingDecisionFuzzyVariableExplanation(functionBlock, Variable::isOutput) + "\n" +
               "Applied Rules:\n" + getAppliedRules(functionBlock);
    }

    // Generates an explanation for all sliding decision fuzzy variable.
    private String generateSlidingDecisionFuzzyVariableExplanation(FunctionBlock functionBlock, Predicate<Variable> variableFilter) {
        return functionBlock.getVariables().values().stream()
             // filter variables could be Input or Output variables.
             .filter(variableFilter)
             // map variable details (name, value, linguistic terms and membership values) based on a predicate.
             .map(fuzzyVariable -> fuzzyVariable.getName() + ":\n\tValue " + fuzzyVariable.getValue() + "\n" + extractLinguisticTermNameAndMembershipValue(fuzzyVariable))
             .collect(Collectors.joining("\n"));
    }

    private String extractLinguisticTermNameAndMembershipValue(Variable fuzzyVariable) {
        return fuzzyVariable.getLinguisticTerms().entrySet().stream()
            // map linguistic term details (name and membership value)
            .map(linguisticTerm -> "\tTerm " + linguisticTerm.getKey() + "\t" + linguisticTerm.getValue().getMembershipFunction().membership(fuzzyVariable.getValue()) + "\n")
            .collect(Collectors.joining());
    }

    // get the all applied rules.
    private String getAppliedRules(FunctionBlock functionBlock) {
        // It filters the rules with a degree of support greater than zero.
        return functionBlock.getRuleBlocks().values().stream()
                .flatMap(ruleBlock -> ruleBlock.getRules().stream())
                .filter(rule -> rule.getDegreeOfSupport() > 0)
                .map(String::valueOf)
                .collect(Collectors.joining("\n"));
    }

}
