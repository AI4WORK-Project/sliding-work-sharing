package eu.ai4work.sws.config;

import eu.ai4work.sws.model.SlidingDecisionStatus;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.RequiredArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class OpenApiDefinitionConfig {

    private final FIS fuzzyInferenceSystem;

    /**
     * Dynamically modifies the auto-generated default OpenAPI definition according
     * to the current configuration of the Fuzzy Inference System:
     * - sets sliding decision input parameters according to FCL input vars
     * - sets sliding decision output parameters according to FCL output vars
     * @return OpenApiCustomizer adjusted to the current FCL config
     */
    @Bean
    public OpenApiCustomizer dynamicPropertiesCustomiser() {
        return openApi -> {
            Schema<?> requestSchema = openApi.getComponents()
                    .getSchemas()
                    .get("SlidingDecisionRequest");

            if (requestSchema != null) {
                requestSchema.addProperty("slidingDecisionStatus", new StringSchema()._default("Sliding Decision Request"));

                Schema inputParamSchema = requestSchema.getProperties().get("slidingDecisionInputParameters");
                inputParamSchema.additionalProperties(false);

                Map<String, Schema> inputParams = new LinkedHashMap<>();
                fuzzyInferenceSystem.getFunctionBlock(null).getVariables().values().stream().filter(Variable::isInput).forEach(
                        fuzzyVariable -> inputParams.put(fuzzyVariable.getName(), new NumberSchema())
                );
                inputParamSchema.setProperties(inputParams);
            }

            Schema<?> responseSchema = openApi.getComponents()
                    .getSchemas()
                    .get("SlidingDecisionResponse");

            if (responseSchema != null) {
                if (responseSchema.getProperties().get("decisionStatus") != null) {

                    responseSchema.getProperties().get("decisionStatus").setDefault(SlidingDecisionStatus.RESPONSE);
                }
                Schema outputParamSchema = responseSchema.getProperties().get("slidingDecisionOutputParameters");

                Map<String, Schema> outputParams = new LinkedHashMap<>();

                outputParamSchema.additionalProperties(Boolean.FALSE);
                fuzzyInferenceSystem.getFunctionBlock(null).getVariables().values().stream().filter(Variable::isOutput).forEach(
                        fuzzyVariable -> outputParams.put(fuzzyVariable.getName(), new NumberSchema())
                );
                outputParamSchema.setProperties(outputParams);
            }
        };
    }
}
