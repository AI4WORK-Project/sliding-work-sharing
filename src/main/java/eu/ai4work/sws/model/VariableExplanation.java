package eu.ai4work.sws.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VariableExplanation {
    private double value;
    private Map<String, Double> membershipValues;

}
