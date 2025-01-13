package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlidingDecisionService {
    private static final String NO_OF_TRUCKS_IN_QUEUE = "noOfTrucksInQueue";
    private static final String POSITION_OF_TRUCK_TO_BE_PRIORITIZED = "positionOfTruckToBePrioritized";
    private final RuleEngineService ruleEngineService;

    public SlidingDecisionResult getSlidingDecision(Map<String, Object> slidingDecisionInputParameters) throws Exception {
        int noOfTrucksInQueue = (int) slidingDecisionInputParameters.getOrDefault(NO_OF_TRUCKS_IN_QUEUE, 0);
        int positionOfTruckToBePrioritized = (int) slidingDecisionInputParameters.getOrDefault(POSITION_OF_TRUCK_TO_BE_PRIORITIZED, 0);

        return ruleEngineService.applySlidingDecisionRules(noOfTrucksInQueue, positionOfTruckToBePrioritized);
    }
}
