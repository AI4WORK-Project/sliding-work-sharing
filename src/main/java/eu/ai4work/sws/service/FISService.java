package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.config.FuzzyInferenceSystemInitializer;
import eu.ai4work.sws.config.InitializeFuzzyIOParameterLists;
import eu.ai4work.sws.exception.InvalidFclFileException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FISService {
    private final ApplicationScenarioConfiguration applicationScenarioConfiguration;
    private final FuzzyInferenceSystemInitializer fuzzyInferenceSystemInitializer;
    private final InitializeFuzzyIOParameterLists initializeFuzzyIOParameterLists;

    @Getter
    private FIS fuzzyInferenceSystem;
    @Getter
    private List<String> requiredInputParameters;
    @Getter
    private List<String> outputVariableNames;

    // During the application startup time it to loads the FIS.
    @PostConstruct
    public void initializeFISonApplicationStartup() {
        loadFuzzyInferenceSystem();
    }

    public void initializeFIS() {
        if (applicationScenarioConfiguration.isReloadFclAtRuntime()) {
            loadFuzzyInferenceSystem();
        }
    }

    private void loadFuzzyInferenceSystem() {
        try {
            FIS initializeFuzzyInferenceSystem = fuzzyInferenceSystemInitializer.initializeFuzzyInferenceSystem();
            fuzzyInferenceSystem = initializeFuzzyInferenceSystem;
            requiredInputParameters = initializeFuzzyIOParameterLists.getRequiredInputParametersFromFIS(initializeFuzzyInferenceSystem);
            outputVariableNames = initializeFuzzyIOParameterLists.getOutputVariableNamesFromFIS(initializeFuzzyInferenceSystem);;
        } catch (FileNotFoundException exception) {
            throw new InvalidFclFileException("Failed to load Fuzzy Control Language (FCL) file: ", exception);
        }
    }
}
