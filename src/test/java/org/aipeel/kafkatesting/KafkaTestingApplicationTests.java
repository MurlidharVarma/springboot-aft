package org.aipeel.kafkatesting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaTestingApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

	@Test
    @DisplayName("Message received via api")
	public void apiTest() {
        ResponseEntity<String> response = restTemplate.getForEntity("/kafka/order/lee", String.class);
        Assertions.assertEquals("Message posted", response.getBody());
	}

}
