package eu.ai4work.sws.config;

import eu.ai4work.sws.exception.InvalidFclFileException;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.net.URL;

@Configuration
@RequiredArgsConstructor
public class FuzzyInferenceSystemInitializer {
    private final static Logger logger = LogManager.getLogger(FuzzyInferenceSystemInitializer.class);
    private final ApplicationScenarioConfiguration applicationScenarioConfiguration;

    /**
     * Initializes a Fuzzy Inference System (FIS) based on the Fuzzy Control Language (FCL) rules file.
     * This process executes on application startup.
     *
     * @return fuzzyInferenceSystem    an initialized FIS object.
     * @throws FileNotFoundException   if the FCL file cannot be found at the specified path.
     * @throws InvalidFclFileException if the FCL file cannot be parsed.
     */
    @Bean(name = "fuzzyInferenceSystem")
    public FIS initializeFuzzyInferenceSystem() throws FileNotFoundException, InvalidFclFileException {
        String fclRulesFilePath = applicationScenarioConfiguration.getFclRulesFilePath();
        URL fuzzyLogicRulesResourceUrl = getClass().getClassLoader().getResource(fclRulesFilePath);

        if (fuzzyLogicRulesResourceUrl == null) {
            throw new FileNotFoundException("Fuzzy Control Language (FCL) file not found: " + fclRulesFilePath);
        }

        return parseFclFile(fuzzyLogicRulesResourceUrl.getPath());
    }

    private static FIS parseFclFile(String fclRulesFilePath) {
        try {
            logger.debug("Parsing a Fuzzy Inference System (FIS) using an FCL file: " + fclRulesFilePath);
            return FIS.load(fclRulesFilePath);
        } catch (Exception exception) {
            throw new InvalidFclFileException("Failed to parse Fuzzy Control Language (FCL) file: " + fclRulesFilePath, exception);
        }
    }
}
