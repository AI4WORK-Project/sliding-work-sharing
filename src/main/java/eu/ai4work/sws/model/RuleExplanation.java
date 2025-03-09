package eu.ai4work.sws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleExplanation {
    private String name;
    private String condition;
    private String consequence;
    private String weight;
    private String degreeOfSupport;
}
