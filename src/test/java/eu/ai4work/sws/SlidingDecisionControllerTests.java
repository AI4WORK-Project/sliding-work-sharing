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
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.OK, "\"decisionStatus\":\"Sliding Decision Response\"", "HUMAN_ON_THE_LOOP");
    }

    @Test
    void testMissingParameter() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.BAD_REQUEST, "\"decisionStatus\":\"Error - Sliding Decision not possible\"", "noOfTrucksInQueue");
    }

    @Test
    void testParameterNameTypo() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "noOfTrucksInQueuee": 7,
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.BAD_REQUEST, "\"decisionStatus\":\"Error - Sliding Decision not possible\"", "noOfTrucksInQueue");
    }

    @Test
    void testAdditionalUnknownParameter() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "additionalParameter": 42
                  }
                }
                """;
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.BAD_REQUEST, "\"decisionStatus\":\"Error - Sliding Decision not possible\"", "additionalParameter");
    }

    @Test
    void testInvalidJson() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters":
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.INTERNAL_SERVER_ERROR, "\"decisionStatus\":\"Error - Sliding Decision not possible\"", "JSON parse error");
    }

    @Test
    void testInvalidInputParameterValue() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "noOfTrucksInQueue": "seven",
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """;
        assertSlidingDecisionResponse(postSlidingDecisionRequest(slidingDecisionRequestJsonBody),
                HttpStatus.BAD_REQUEST, "\"decisionStatus\":\"Error - Sliding Decision not possible\"", "noOfTrucksInQueue");
    }

    private ResponseEntity<String> postSlidingDecisionRequest(String jsonEntityBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> slidingDecisionRequestHttpEntity = new HttpEntity<>(jsonEntityBody, httpHeaders);
        return testSlidingDecisionRestTemplate.postForEntity("/sliding-decision", slidingDecisionRequestHttpEntity, String.class);
    }

    private void assertSlidingDecisionResponse(ResponseEntity<String> slidingDecisionResponse, HttpStatus expectedResponseStatus, String... expectedResponseContains) {
        assertThat(slidingDecisionResponse.getStatusCode()).isEqualTo(expectedResponseStatus);
        for (String expectedResponse : expectedResponseContains) {
            assertThat(slidingDecisionResponse.getBody()).contains(expectedResponse);
        }
    }
}
