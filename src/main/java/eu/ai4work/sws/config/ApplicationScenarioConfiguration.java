package eu.ai4work.sws.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "application-scenario-config")
public class ApplicationScenarioConfiguration {
    private String fclRulesFilePath;
    private Map<String, String> decisionResultsDescription;
}
