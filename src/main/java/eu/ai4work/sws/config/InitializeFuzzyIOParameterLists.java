package eu.ai4work.sws.config;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.NoSuchElementException;

@Configuration
public class InitializeFuzzyIOParameterLists {
    public List<String> getRequiredInputParametersFromFIS(FIS fuzzyInferenceSystem) {
        return fuzzyInferenceSystem.getFunctionBlock(null)  // Get default function block
                .getVariables().values().stream()
                .filter(Variable::isInput)
                .map(Variable::getName)
                .toList();
    }

    public List<String> getOutputVariableNamesFromFIS(FIS fuzzyInferenceSystem) {
        List<String> outputVariablesFromFIS = fuzzyInferenceSystem.getFunctionBlock(null)
                .getVariables().values().stream()
                .filter(Variable::isOutput)
                .map(Variable::getName)
                .toList();

        if (outputVariablesFromFIS.isEmpty()) {
            throw new NoSuchElementException("Output variable(s) missing in the provided FCL file. Please define at least one output variable.");
        }
        return outputVariablesFromFIS;
    }
}
