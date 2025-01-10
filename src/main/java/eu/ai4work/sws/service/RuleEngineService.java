package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private static final String NO_OF_TRUCKS_IN_QUEUE = "no_of_trucks_in_queue";
    private static final String POSITION_OF_TRUCK = "position_of_truck";
    private static final String SUGGESTED_WORK_SHARING_APPROACH = "suggested_work_sharing_approach";
    private static final String TRUCK_SCHEDULING_RULES_FILE = "rules/TruckSchedulingSlidingDecisionRules.fcl";

    Logger logger = LogManager.getLogger(RuleEngineService.class);

    /**
     * Evaluates the rules based on the provided inputs and returns the sliding decision result.
     *
     * @param noOfTrucksInQueue              The number of trucks.
     * @param positionOfTruckToBePrioritized The position of the truck to be prioritized.
     * @return SlidingDecisionResult representing the outcome of the sliding decision.
     * @throws Exception if the FIS cannot be initialized (e.g. because the FCL input cannot be loaded or parsed).
     */
    public SlidingDecisionResult applySlidingDecisionRules(int noOfTrucksInQueue, int positionOfTruckToBePrioritized) throws Exception {
        FIS fuzzyInferenceSystem = initializeFuzzyInferenceSystem();

        // sets the input variables for the FIS
        fuzzyInferenceSystem.getFuzzyRuleSet().setVariable(NO_OF_TRUCKS_IN_QUEUE, noOfTrucksInQueue);
        fuzzyInferenceSystem.getFuzzyRuleSet().setVariable(POSITION_OF_TRUCK, positionOfTruckToBePrioritized);

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
    private FIS initializeFuzzyInferenceSystem() throws Exception {
        try {
            URL fuzzyLogicRulesResourceUrl = getClass().getClassLoader().getResource(TRUCK_SCHEDULING_RULES_FILE);
            if (fuzzyLogicRulesResourceUrl == null) {
                throw new Exception("Fuzzy Control Language (FCL) file not found: " + TRUCK_SCHEDULING_RULES_FILE);
            }
            FIS fuzzyInferenceSystem = FIS.load(fuzzyLogicRulesResourceUrl.getPath(), false); // verbose set to 'false' because to avoid GUI-related processing
            if (fuzzyInferenceSystem == null) {
                throw new Exception("Failed to initialize Fuzzy Control Language (FCL) file: " + TRUCK_SCHEDULING_RULES_FILE);
            }
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
