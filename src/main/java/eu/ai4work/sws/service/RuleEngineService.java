package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.Console;
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
     * @throws Exception if the FIS cannot be initialized (e.g. because the FCL input cannot be loaded or parsed).
     */
    public SlidingDecisionResult applySlidingDecisionRules(Map<String, Object> slidingDecisionInputParameters) throws Exception {
        FIS fuzzyInferenceSystem = initializeFuzzyInferenceSystem(applicationScenarioConfiguration.getFclRulesFilePath());

        // Set input parameters in the FIS
        slidingDecisionInputParameters.forEach((parameterName, parameterValue) -> {
            Variable fuzzyVariableForParameter = fuzzyInferenceSystem.getFuzzyRuleSet().getVariable(parameterName);
            if (fuzzyVariableForParameter != null) {
                fuzzyVariableForParameter.setValue(((Number) parameterValue).doubleValue());
            } else {
                logger.warn("Input variable {} not found in FIS", parameterName);
            }
        });

        fuzzyInferenceSystem.getFuzzyRuleSet().evaluate();

        String resultAsLinguisticTerm = mapFuzzyInferenceResultToLinguisticTerm(fuzzyInferenceSystem.getFuzzyRuleSet().getVariable(SUGGESTED_WORK_SHARING_APPROACH));

        return SlidingDecisionResult.valueOf(resultAsLinguisticTerm);
    }

    /**
     * Initializes a FIS based on an FCL rules file
     *
     * @return FIS object.
     * @throws Exception if the FCL file cannot be found or parsed.
     */
    private FIS initializeFuzzyInferenceSystem(String fclRulesFilePath) throws Exception {
        try {
            URL fuzzyLogicRulesResourceUrl = getClass().getClassLoader().getResource(fclRulesFilePath);
            if (fuzzyLogicRulesResourceUrl == null) {
                throw new Exception("Fuzzy Control Language (FCL) file not found: " + fclRulesFilePath);
            }
            FIS fuzzyInferenceSystem = FIS.load(fuzzyLogicRulesResourceUrl.getPath(), false); // verbose set to 'false' because to avoid GUI-related processing
            if (fuzzyInferenceSystem == null) {
                throw new Exception("Failed to initialize Fuzzy Control Language (FCL) file: " + fclRulesFilePath);
            }
            logger.debug("Successfully initialized FIS from file: {}", fclRulesFilePath);
            return fuzzyInferenceSystem;
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }

    /**
     * Determines the linguistic term with the highest membership degree for the given variable.
     *
     * @param suggestedWorkSharingApproachAsFuzzyVariable The fuzzy output variable to evaluate.
     * @return The output as linguistic term, i.e. the name of the membership function with the highest membership degree.
     */
    private String mapFuzzyInferenceResultToLinguisticTerm(Variable suggestedWorkSharingApproachAsFuzzyVariable) {
        String linguisticTerm = null;
        // The variable is initialized to -1.0. This value is chosen because membership degrees in fuzzy logic
        // are typically between 0 and 1. Initializing to -1.0 ensures that any valid membership degree
        // (which will be greater than -1.0) will replace this initial value.
        double highestMembershipDegree = -1.0;

        for (Map.Entry<String, LinguisticTerm> termEntry : suggestedWorkSharingApproachAsFuzzyVariable.getLinguisticTerms().entrySet()) {
            double membershipDegree = termEntry.getValue().getMembershipFunction().membership(suggestedWorkSharingApproachAsFuzzyVariable.getLatestDefuzzifiedValue());
            if (membershipDegree > highestMembershipDegree) {
                highestMembershipDegree = membershipDegree;
                linguisticTerm = termEntry.getKey();
            }
        }

        if (linguisticTerm == null) {
            throw new IllegalStateException("Unable to identify the linguistic term.");
        }

        return linguisticTerm;
    }
}
