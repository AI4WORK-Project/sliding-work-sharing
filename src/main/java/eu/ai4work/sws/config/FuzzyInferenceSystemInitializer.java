package eu.ai4work.sws.config;

import eu.ai4work.sws.exception.InvalidFclFileException;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
        logger.info("Initializing a Fuzzy Inference System (FIS) from FCL file: " + fclRulesFilePath);

        // Try external file system
        File externalFuzzyRuleFile = new File(fclRulesFilePath);
        if (externalFuzzyRuleFile.exists()) {
            return parseFclFile(new FileInputStream(externalFuzzyRuleFile));
        }

        // Then try classpath resources
        InputStream internalFuzzyRuleResourceFile = getClass().getClassLoader().getResourceAsStream(fclRulesFilePath);
        if (internalFuzzyRuleResourceFile != null) {
            return parseFclFile(internalFuzzyRuleResourceFile);
        }

        throw new FileNotFoundException("Fuzzy Control Language (FCL) file not found: " + fclRulesFilePath);
    }

    private static FIS parseFclFile(InputStream inputStream) {
        try {
            return FIS.load(inputStream, true);
        } catch (Exception exception) {
            throw new InvalidFclFileException("Failed to parse Fuzzy Control Language (FCL) file: ", exception);
        }
    }
}
