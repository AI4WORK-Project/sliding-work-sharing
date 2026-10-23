package eu.ai4work.sws.service;

import eu.ai4work.sws.config.ApplicationScenarioConfiguration;
import eu.ai4work.sws.config.FuzzyInferenceSystemInitializer;
import eu.ai4work.sws.config.InitializeFuzzyIOParameterLists;
import eu.ai4work.sws.exception.InvalidFclFileException;
import jakarta.annotation.PostConstruct;
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

    private FISRecord currentFISRecord;

    // this record encapsulates the Fuzzy Inference System (FIS) and its associated input/output parameters.
    public record FISRecord(
            FIS fuzzyInferenceSystem,
            List<String> requiredFuzzyInputParameters,
            List<String> outputVariableNamesFromFIS) {
    }

    @PostConstruct
    public void initializeFuzzyInferenceSystem() {
        currentFISRecord = loadFISRecord();
    }

    /**
     * Returns a consistent set of FIS for one sliding decision.
     * Runtime reload keeps the singleton FISService and replaces its record.
     */
    public FISRecord getFISRecord() {
        if (applicationScenarioConfiguration.isReloadFclAtRuntime()) {
            FISRecord reloadedRecord = loadFISRecord();
            currentFISRecord = reloadedRecord;
            return reloadedRecord;
        }
        return currentFISRecord;
    }

    private FISRecord loadFISRecord() {
        try {
            FIS fuzzyInferenceSystem = fuzzyInferenceSystemInitializer.initializeFuzzyInferenceSystem();
            return new FISRecord(fuzzyInferenceSystem,
                    initializeFuzzyIOParameterLists.getRequiredInputParametersFromFIS(fuzzyInferenceSystem),
                    initializeFuzzyIOParameterLists.getOutputVariableNamesFromFIS(fuzzyInferenceSystem));
        } catch (FileNotFoundException exception) {
            throw new InvalidFclFileException("Failed to load Fuzzy Control Language (FCL) file: ", exception);
        }
    }
}
