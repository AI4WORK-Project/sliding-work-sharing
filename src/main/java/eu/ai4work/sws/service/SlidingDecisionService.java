package eu.ai4work.sws.service;

import eu.ai4work.sws.model.SlidingDecisionExplanation;
import eu.ai4work.sws.model.SlidingDecisionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlidingDecisionService {
    private final RuleEngineService ruleEngineService;

    public SlidingDecisionResult getSlidingDecision(Map<String, Object> slidingDecisionInputParameters) throws Exception {
        return ruleEngineService.applySlidingDecisionRules(slidingDecisionInputParameters);
    }

    public SlidingDecisionExplanation getSlidingDecisionExplanation() {
        return ruleEngineService.buildSlidingDecisionExplanation();
    }
}
