package eu.ai4work.sws;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=logistics")
class SlidingDecisionControllerTests {

    public static final String DECISION_STATUS_ERROR_STRING = "\"decisionStatus\":\"Error - Sliding Decision not possible\"";

    @Autowired
    private TestRestTemplate testSlidingDecisionRestTemplate;

    @Test
    void testHappyFlowOfSlidingDecision() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithBody(slidingDecisionRequestJsonBody),
                HttpStatus.OK,
                "\"decisionStatus\":\"Sliding Decision Response\"",
                "HUMAN_ON_THE_LOOP");
    }

    @Test
    void testInvalidJson() {
        String slidingDecisionRequestInvalidJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters":
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithBody(slidingDecisionRequestInvalidJsonBody),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DECISION_STATUS_ERROR_STRING,
                "JSON parse error");
    }

    @Test
    void testMissingParameter() {
        String slidingDecisionInputParametersJson = """
                    "positionOfTruckToBePrioritized": 5
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "noOfTrucksInQueue");
    }

    @Test
    void testParameterNameTypo() {
        String slidingDecisionInputParametersJson = """
                    "noOfTrucksInQueuee": 7,
                    "positionOfTruckToBePrioritized": 5
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "noOfTrucksInQueue");
    }

    @Test
    void testAdditionalUnknownParameter() {
        String slidingDecisionInputParametersJson = """
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "additionalParameter": 42
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "additionalParameter");
    }


    @Test
    void testInvalidInputParameterValue() {
        String slidingDecisionInputParametersJson = """
                    "noOfTrucksInQueue": "seven",
                    "positionOfTruckToBePrioritized": 5
                """;
        assertSlidingDecisionResponsStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "noOfTrucksInQueue");
    }

    private ResponseEntity<String> postSlidingDecisionRequestWithParameters(String slidingDecisionInputParametersJson) {
        return postSlidingDecisionRequestWithBody(
                String.format("""
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    %s
                  }
                }
                """,
                slidingDecisionInputParametersJson));
    }

    private ResponseEntity<String> postSlidingDecisionRequestWithBody(String jsonEntityBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> slidingDecisionRequestHttpEntity = new HttpEntity<>(jsonEntityBody, httpHeaders);
        return testSlidingDecisionRestTemplate.postForEntity("/sliding-decision", slidingDecisionRequestHttpEntity, String.class);
    }

    private void assertSlidingDecisionResponsStatusAndContents(ResponseEntity<String> slidingDecisionResponse, HttpStatus expectedResponseStatus, String... expectedResponseContains) {
        assertThat(slidingDecisionResponse.getStatusCode()).isEqualTo(expectedResponseStatus);
        for (String expectedResponse : expectedResponseContains) {
            assertThat(slidingDecisionResponse.getBody()).contains(expectedResponse);
        }
    }
}
