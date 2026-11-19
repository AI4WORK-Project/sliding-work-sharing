package eu.ai4work.sws.config;

import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;
import java.util.NoSuchElementException;

@Configuration
@RequiredArgsConstructor
public class InitializeFuzzyIOParameterLists {
    private final FIS fuzzyInferenceSystem;

    @Bean("requiredFuzzyInputParameters")
    @DependsOn("fuzzyInferenceSystem")
    public List<String> getRequiredInputParametersFromFIS() {
        return fuzzyInferenceSystem.getFunctionBlock(null)  // Get default function block
                .getVariables().values().stream()
                .filter(Variable::isInput)
                .map(Variable::getName)
                .toList();
    }

    @Bean("outputVariableNamesFromFIS")
    @DependsOn("fuzzyInferenceSystem")
    public List<String> getOutputVariableNamesFromFIS() {
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
