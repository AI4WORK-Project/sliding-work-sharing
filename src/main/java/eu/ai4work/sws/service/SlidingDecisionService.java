package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlidingDecisionService {
    private final RuleEngineService ruleEngineService;

    public SlidingDecision getSlidingDecision(Map<String, Object> slidingDecisionInputParameters) {
        return ruleEngineService.applySlidingDecisionRules(slidingDecisionInputParameters);
    }
}
