package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.UnIdentifiedTermException;
import eu.ai4work.sws.exception.UnValidFCLFileException;
import eu.ai4work.sws.model.SlidingDecisionResult;
import eu.ai4work.sws.exception.UnknownInputParameterException;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

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

        fuzzyInferenceSystem.getFuzzyRuleSet().evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getFuzzyRuleSet().getVariable(SUGGESTED_WORK_SHARING_APPROACH));

        return SlidingDecisionResult.valueOf(resultAsLinguisticTerm);
    }

    /**
     * Initializes a Fuzzy Inference System (FIS) based on Fuzzy Control Language (FCL) rules file
     *
     * @param fclRulesFilePath The file path of the FCL rules file.
     * @return Initialized FIS object.
     * @throws FileNotFoundException If the FCL file is not found.
     * @throws UnValidFCLFileException If the FCL file cannot be parsed.
     */
    private FIS initializeFuzzyInferenceSystem(String fclRulesFilePath) throws FileNotFoundException, UnValidFCLFileException {
        URL fuzzyLogicRulesResourceUrl = getClass().getClassLoader().getResource(fclRulesFilePath);
        if (fuzzyLogicRulesResourceUrl == null) {
            throw new FileNotFoundException("Fuzzy Control Language (FCL) file not found: " + fclRulesFilePath);
        }

        FIS fuzzyInferenceSystem = FIS.load(fuzzyLogicRulesResourceUrl.getPath(), false); // verbose set to 'false' because to avoid GUI-related processing
        if (fuzzyInferenceSystem == null) {
            throw new UnValidFCLFileException("Failed to parse Fuzzy Control Language (FCL) file: " + fclRulesFilePath);
        }

        logger.debug("Successfully initialized FIS from file: {}", fclRulesFilePath);
        return fuzzyInferenceSystem;
    }

    /**
     * Maps a fuzzy inference result to its corresponding linguistic term based on the highest membership degree.
     *
     * @param suggestedWorkSharingApproachAsFuzzyVariable The fuzzy output variable to evaluate.
     * @return The output as a linguistic term, i.e., the name of the membership function with the highest membership degree.
     * @throws UnIdentifiedTermException if no linguistic term can be identified.
     */
    private String mapFuzzyInferenceResultToLinguisticTerm(Variable suggestedWorkSharingApproachAsFuzzyVariable) throws UnIdentifiedTermException {
        return suggestedWorkSharingApproachAsFuzzyVariable.getLinguisticTerms().entrySet().stream()
                // Map each linguistic term to its corresponding membership degree
                // The key is the linguistic term name
                // The value is membership degree for the latest defuzzified value
                .map(linguisticTermWithMembershipDegree -> Map.entry(
                        linguisticTermWithMembershipDegree.getKey(),
                        linguisticTermWithMembershipDegree.getValue().getMembershipFunction()
                                .membership(suggestedWorkSharingApproachAsFuzzyVariable.getLatestDefuzzifiedValue())
                ))
                // Identify the linguistic term with the highest membership degree
                .max(Map.Entry.comparingByValue())
                // Retrieve the linguistic term with the highest membership degree, if available
                .map(Map.Entry::getKey)
                // If no valid term is found, throw an exception
                .orElseThrow(() -> new UnIdentifiedTermException(
                        "Unable to identify the linguistic term for value: "
                                + suggestedWorkSharingApproachAsFuzzyVariable.getLatestDefuzzifiedValue()
                ));
    }

    /**
     * Sets input parameters to the Fuzzy Inference System (FIS).
     *
     * @param fuzzyInferenceSystem           The FIS instance where input parameters will be set.
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @throws UnknownInputParameterException if an input parameter is not recognized by the FIS.
     * @throws IllegalArgumentException       if the input parameter map is null or empty.
     */
    private void setInputParametersToFuzzyInterfaceSystem(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters) throws UnknownInputParameterException {
        if (slidingDecisionInputParameters == null || slidingDecisionInputParameters.isEmpty()) {
            throw new IllegalArgumentException("Input parameters must not be null or empty.");
        }

        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            Variable fuzzyVariableForParameter = fuzzyInferenceSystem.getFuzzyRuleSet().getVariable(parameterName);
            if (fuzzyVariableForParameter != null) {
                fuzzyVariableForParameter.setValue(((Number) parameterValue).doubleValue());
            } else {
                throw new UnknownInputParameterException("The following input parameter is unknown: " + parameterName);
            }
        });
    }
}
