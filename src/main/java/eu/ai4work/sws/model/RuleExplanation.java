package eu.ai4work.sws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleExplanation {
    private String name;
    private String ifClause;
    private String thenClause;
    private String weight;
    private String degreeOfSupport;
}
