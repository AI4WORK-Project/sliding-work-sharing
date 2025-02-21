package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.exception.InvalidFclFileException;
import eu.ai4work.sws.exception.InvalidInputParameterException;
import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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

        verifySlidingDecisionInputParameters(fuzzyInferenceSystem, slidingDecisionInputParameters);

        setInputParametersToFuzzyInterfaceSystem(fuzzyInferenceSystem, slidingDecisionInputParameters);

        fuzzyInferenceSystem.evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getVariable(SUGGESTED_WORK_SHARING_APPROACH));

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
     * Checks if any required sliding decision input parameters are unknown or missing or extra.
     *
     * @param fuzzyInferenceSystem           The FIS instance to get the input parameters.
     * @param slidingDecisionInputParameters The input parameters from the sliding decision request.
     * @throws InvalidInputParameterException if an input parameter is unknown or missing or extra.
     */
    private void verifySlidingDecisionInputParameters(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters)
            throws InvalidInputParameterException {
        Collection<String> requiredInputParameter = getRequiredInputParameterFromFIS(fuzzyInferenceSystem);
        // Extra parameters provided
        if (slidingDecisionInputParameters.size() > requiredInputParameter.size()) {
            List<String> extraSlidingDecisionInputParameters = slidingDecisionInputParameters.keySet().stream()
                    .filter(param -> !requiredInputParameter.contains(param))
                    .toList();
            if (!extraSlidingDecisionInputParameters.isEmpty()) {
                throw new InvalidInputParameterException("The following sliding decision input parameter is extra: " + extraSlidingDecisionInputParameters);
            }
        } else if (slidingDecisionInputParameters.size() < requiredInputParameter.size()) {
            // Missing parameters
            List<String> missingSlidingDecisionInputParameters = requiredInputParameter.stream()
                    .filter(param -> !slidingDecisionInputParameters.containsKey(param))
                    .toList();
            if (!missingSlidingDecisionInputParameters.isEmpty()) {
                throw new InvalidInputParameterException("The following sliding decision input parameter is missing:" + missingSlidingDecisionInputParameters);
            }
        } else {
            // Same count: validate that every provided parameter is expected
            List<String> unknownSlidingDecisionInputParameters = slidingDecisionInputParameters.keySet().stream()
                    .filter(param -> !requiredInputParameter.contains(param))
                    .toList();
            if (!unknownSlidingDecisionInputParameters.isEmpty()) {
                throw new InvalidInputParameterException("The following sliding decision input parameter is unknown: " + unknownSlidingDecisionInputParameters);
            }
        }
    }

    // Retrieves the list of required sliding input parameter names from the Fuzzy Inference System
    public List<String> getRequiredInputParameterFromFIS(FIS fuzzyInferenceSystem) {
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
    private void setInputParametersToFuzzyInterfaceSystem(FIS fuzzyInferenceSystem, Map<String, Object> slidingDecisionInputParameters) {
        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            fuzzyInferenceSystem.getVariable(parameterName).setValue(((Number) parameterValue).doubleValue());
        });
    }
}
