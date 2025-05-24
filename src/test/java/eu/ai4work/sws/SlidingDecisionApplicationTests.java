package eu.ai4work.sws;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=logistics")
class SlidingDecisionApplicationTests {

    @Autowired
    private TestRestTemplate testSlidingDecisionRestTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testLogisticsScenario() {
        HttpEntity<String> slidingDecisionRequestHttpEntity = createSlidingDecisionRequestEntity("""
                {
                  "decisionStatus": "Sliding Decision Request",
                  "slidingDecisionInputParameters": {
                    "noOfTrucksInQueue": 7,
                    "positionOfTruckToBePrioritized": 5
                  }
                }
                """);

        ResponseEntity<String> slidingDecisionResponseEntityEntity = testSlidingDecisionRestTemplate.postForEntity("/sliding-decision", slidingDecisionRequestHttpEntity, String.class);

        assertThat(slidingDecisionResponseEntityEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(slidingDecisionResponseEntityEntity.getBody()).contains("\"decisionStatus\":\"Sliding Decision Response\"");
        assertThat(slidingDecisionResponseEntityEntity.getBody()).contains("\"slidingDecision\":\"HUMAN_ON_THE_LOOP\"");
        assertThat(slidingDecisionResponseEntityEntity.getBody()).contains("\"description\":\"Human has to be informed about AI's rescheduling\"");
    }

    private HttpEntity<String> createSlidingDecisionRequestEntity(String jsonEntityBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        return new HttpEntity<>(jsonEntityBody, httpHeaders);
    }
}
