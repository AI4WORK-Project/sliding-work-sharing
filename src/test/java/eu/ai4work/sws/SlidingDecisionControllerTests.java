package eu.ai4work.sws;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=logistics")
class SlidingDecisionControllerTests {

    public static final String DECISION_STATUS_ERROR_STRING = "\"decisionStatus\":\"Error - Sliding Decision not possible\"";

    @Autowired
    private TestRestTemplate testSlidingDecisionRestTemplate;

    @Test
    void testHappyFlowOfSlidingDecisionRequest() {
        String slidingDecisionRequestJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "numberOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80
                  }
                }
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithBody(slidingDecisionRequestJsonBody),
                HttpStatus.OK,
                "\"decisionStatus\":\"Sliding Decision Response\"",
                "informHuman");
    }

    @Test
    void testInvalidJsonSlidingDecisionRequest() {
        String slidingDecisionRequestInvalidJsonBody = """
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters":
                    "numberOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80
                  }
                }
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithBody(slidingDecisionRequestInvalidJsonBody),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DECISION_STATUS_ERROR_STRING,
                "JSON parse error");
    }

    @Test
    void testMissingParameterSlidingDecisionRequest() {
        String slidingDecisionInputParametersJson = """
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "numberOfTrucksInQueue");
    }

    @Test
    void testParameterNameTypoSlidingDecisionRequest() {
        String slidingDecisionInputParametersJson = """
                    "numberOfTrucksInQueuee": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "numberOfTrucksInQueue");
    }

    @Test
    void testAdditionalUnknownParameterSlidingDecisionRequest() {
        String slidingDecisionInputParametersJson = """
                    "numberOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80,
                    "additionalParameter": 42
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "additionalParameter");
    }


    @Test
    void testInvalidInputParameterValueSlidingDecisionRequest() {
        String slidingDecisionInputParametersJson = """
                    "numberOfTrucksInQueue": "seven",
                    "positionOfTruckToBePrioritized": 5,
                    "materialUrgency":30,
                    "operationalWorkload":80
                """;
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionRequestWithParameters(slidingDecisionInputParametersJson),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "numberOfTrucksInQueue");
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
            """, slidingDecisionInputParametersJson)
        );
    }

    private ResponseEntity<String> postSlidingDecisionRequestWithBody(String jsonEntityBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> slidingDecisionRequestHttpEntity = new HttpEntity<>(jsonEntityBody, httpHeaders);
        return testSlidingDecisionRestTemplate.postForEntity("/sliding-decision", slidingDecisionRequestHttpEntity, String.class);
    }

    private void assertSlidingDecisionResponseStatusAndContents(ResponseEntity<String> slidingDecisionResponse, HttpStatus expectedResponseStatus, String... expectedResponseContains) {
        assertThat(slidingDecisionResponse.getStatusCode()).isEqualTo(expectedResponseStatus);
        for (String expectedResponse : expectedResponseContains) {
            assertThat(slidingDecisionResponse.getBody()).contains(expectedResponse);
        }
    }

    @Test
    void testSuccessfulSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
            postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
            HttpStatus.OK,
            "\"decisionStatus\":\"Sliding Decision Multi-Response\"",
            "informHuman",
            "autonomousReprioritization"
        );
    }

    @Test
    void testInvalidJsonSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50
                        "operationalWorkload":20
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.INTERNAL_SERVER_ERROR,
                DECISION_STATUS_ERROR_STRING,
                "JSON parse error",
                "Unexpected character"
        );
    }

    @Test
    void testMissingParameterSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "positionOfTruckToBePrioritized",
                "abc-123"
        );
    }


    @Test
    void testParameterTypoSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgenzy":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioriticed": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        // typo in "positionOfTruckToBePrioriticed" is not recognized because evaluation stops after the first typo
        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "materialUrgenzy",
                "abc-123"
        );
    }

    @Test
    void testAdditionalUnknownParameterSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20,
                        "additionalUnknownParameter":3
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "additionalUnknownParameter",
                "abc-456"
        );
    }

    @Test
    void testInvalidInputParameterValueSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 3,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":"invalidValue"
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "operationalWorkload",
                "abc-456"
        );
    }

    @Test
    void testEmptyInputParameterValueSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": null,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "numberOfTrucksInQueue",
                "abc-456"
        );
    }

    @Test
    void testEmptyIdSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-456",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": null,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "The ID in sliding decision request must not be empty"
        );
    }

    @Test
    void testDuplicatedIdsSlidingDecisionMultiRequest() {
        List<String> slidingDecisionMultiRequests = new ArrayList<>();
        String slidingDecisionAtomicRequest1 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": 7,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":30,
                        "operationalWorkload":80
                    }
                }
                """;
        String slidingDecisionAtomicRequest2 = """
                {
                    "id": "abc-123",
                    "slidingDecisionInputParameters": {
                        "numberOfTrucksInQueue": null,
                        "positionOfTruckToBePrioritized": 5,
                        "materialUrgency":50,
                        "operationalWorkload":20
                    }
                }
                """;
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest1);
        slidingDecisionMultiRequests.add(slidingDecisionAtomicRequest2);

        assertSlidingDecisionResponseStatusAndContents(
                postSlidingDecisionMultiRequestWithParameters(slidingDecisionMultiRequests),
                HttpStatus.BAD_REQUEST,
                DECISION_STATUS_ERROR_STRING,
                "The IDs in sliding decision request must be unique"
        );
    }

    private ResponseEntity<String> postSlidingDecisionMultiRequestWithParameters(List<String> slidingDecisionRequests) {
        return  postSlidingDecisionMultiRequestWithBody(
                String.format("""
                {
                  "decisionStatus": "Sliding Decision Multi-Request",
                  "requests": %s
                }
                """, slidingDecisionRequests)
        );
    }

    private ResponseEntity<String> postSlidingDecisionMultiRequestWithBody(String jsonEntityBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        HttpEntity<String> slidingDecisionRequestHttpEntity = new HttpEntity<>(jsonEntityBody, httpHeaders);
        return testSlidingDecisionRestTemplate.postForEntity("/sliding-decision-multi-request", slidingDecisionRequestHttpEntity, String.class);
    }

}
