package eu.ai4work.sws.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;
import java.util.NoSuchElementException;

@Configuration
@RequiredArgsConstructor
public class InitializeFuzzyIOParameters {
    private final FIS fuzzyInferenceSystem;

    @Bean
    @DependsOn("fuzzyInferenceSystem")
    public List<String> getRequiredInputParametersFromFIS() {
        return fuzzyInferenceSystem.getFunctionBlock(null)  // Get default function block
                // get all variables
                .getVariables().values().stream()
                // keep only input variables
                .filter(Variable::isInput)
                // extract variable names
                .map(Variable::getName)
                // convert to list
                .toList();
    }

    @Bean
    @DependsOn("fuzzyInferenceSystem")
    public List<String> getOutputVariableNamesFromFIS() {
        List<String> outputVariablesFromFIS = fuzzyInferenceSystem.getFunctionBlock(null)
                .getVariables().values().stream()
                .filter(Variable::isOutput)
                .map(Variable::getName)
                .toList();

        if (outputVariablesFromFIS.isEmpty()) {
            throw new NoSuchElementException("Output variable missing in the provided FCL file. Please define the output variable.");
        }
        return outputVariablesFromFIS;
    }
}
