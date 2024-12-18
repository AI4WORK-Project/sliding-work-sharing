package eu.ai4work.sws.service;

import eu.ai4work.sws.model.specific.CongestionLevel;
import eu.ai4work.sws.model.specific.TruckPosition;
import eu.ai4work.sws.model.generic.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleEngineService {
    private final KieContainer kieContainer;

    /**
     * Evaluates the rules based on the provided congestion level and truck position,
     * and returns the decision results.
     *
     * @param congestionLevel The congestion level to be evaluated.
     * @param truckPosition   The truck position to be evaluated.
     * @return SlidingDecisionResult.
     */
    public SlidingDecisionResult applySlidingDecisionRules(CongestionLevel congestionLevel, TruckPosition truckPosition) {
        KieSession kieSession = kieContainer.newKieSession();

        try {
            kieSession.insert(congestionLevel);
            kieSession.insert(truckPosition);
            kieSession.fireAllRules();

            return getSlidingDecisionResultFromKieSession(kieSession);
        } finally {
            // Ensure the KieSession is disposed of to release resources
            kieSession.dispose();
        }
    }

    /**
     * Extracts the decision results from the KieSession after rules have been fired.
     *
     * @param kieSession The active KieSession containing results.
     * @return SlidingDecisionResult.
     */
    private SlidingDecisionResult getSlidingDecisionResultFromKieSession(KieSession kieSession) {
        // Define an ObjectFilter to extract objects of type SlidingDecisionResult
        ObjectFilter filter = obj -> obj instanceof SlidingDecisionResult;

        // Retrieve and return the first SlidingDecisionResult object from the session
        return kieSession.getObjects(filter)
                .stream()
                .map(obj -> (SlidingDecisionResult) obj)
                .findFirst()
                .orElse(SlidingDecisionResult.HUMAN_MANUALLY);
    }
}
