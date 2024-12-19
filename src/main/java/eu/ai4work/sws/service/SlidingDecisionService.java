package eu.ai4work.sws.service;

import eu.ai4work.sws.model.specific.CongestionLevel;
import eu.ai4work.sws.model.specific.TruckPosition;
import eu.ai4work.sws.model.generic.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlidingDecisionService {
    private static final String NO_OF_TRUCKS_IN_QUEUE = "noOfTrucksInQueue";
    private static final String POSITION_OF_TRUCK_TO_BE_PRIORITIZED = "positionOfTruckToBePrioritized";
    private final RuleEngineService ruleEngineService;

    public SlidingDecisionResult getSlidingDecision(Map<String, Object> parameters) {
        int noOfTrucksInQueue = (int) parameters.getOrDefault(NO_OF_TRUCKS_IN_QUEUE, 0);
        int positionOfTruckToBePrioritized = (int) parameters.getOrDefault(POSITION_OF_TRUCK_TO_BE_PRIORITIZED, 0);

        CongestionLevel congestionLevel = determineCongestionLevel(noOfTrucksInQueue);
        TruckPosition truckPositionZone = determineTruckPositionZone(positionOfTruckToBePrioritized, noOfTrucksInQueue);

        return ruleEngineService.applySlidingDecisionRules(congestionLevel, truckPositionZone);
    }

    /**
     * Determines the congestion level based on the number of trucks in the queue.
     *
     * @param noOfTrucks The total number of trucks in the queue.
     * @return The congestion level as a CongestionLevel enum value.
     */
    private CongestionLevel determineCongestionLevel(int noOfTrucks) {
        if (noOfTrucks <= 10) {
            return CongestionLevel.LOW;
        } else if (noOfTrucks <= 20) {
            return CongestionLevel.MEDIUM;
        } else {
            return CongestionLevel.HIGH; // Applies to 21+ trucks
        }
    }

    /**
     * Determines the truck position in the queue relative to the midpoint of the queue.
     *
     * @param truckPosition The position of the truck in the queue.
     * @param noOfTrucks    The total number of trucks in the queue.
     * @return The truck position zone as a TruckPosition enum value.
     * @throws IllegalArgumentException if the total number of trucks is invalid.
     */
    private TruckPosition determineTruckPositionZone(int truckPosition, int noOfTrucks) {
        if (noOfTrucks <= 0) {
            throw new IllegalArgumentException("Invalid number of trucks in queue");
        }

        int midpoint = (noOfTrucks / 2) + (noOfTrucks % 2); // Calculate the midpoint of the queue

        return truckPosition <= midpoint ? TruckPosition.NEAR_THE_FRONT_OF_THE_QUEUE : TruckPosition.IN_THE_BACK_OF_THE_QUEUE;
    }
}
