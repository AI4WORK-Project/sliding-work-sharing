package eu.ai4work.sws.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SlidingDecisionExplanation {
    private Map<String, VariableExplanation> inputVariables;
    private List<RuleExplanation> appliedRules;
    private Map<String, VariableExplanation> outputVariables;
}
