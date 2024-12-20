package eu.ai4work.sws.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {
    private static final String RULES_PATH = "rules/TruckSchedulingSlidingDecisionRules.drl";

    @Bean
    public KieContainer kieContainer() {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH));

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                throw new IllegalStateException("Errors in DRL file: " + kieBuilder.getResults());
            }

            KieModule kieModule = kieBuilder.getKieModule();
            return kieServices.newKieContainer(kieModule.getReleaseId());
        } catch (Exception e) {
            System.err.println("Failed to initialize KieContainer: " + e);
            throw e;
        }
    }
}
