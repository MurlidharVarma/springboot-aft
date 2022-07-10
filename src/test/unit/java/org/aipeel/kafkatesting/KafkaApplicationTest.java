package org.aipeel.kafkatesting;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"order","payment"}, partitions = 1)
@TestPropertySource(properties = {"kafka.bootstrap-server=${spring.embedded.kafka.brokers}"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("aft")
public class KafkaApplicationTest {

    private WireMockServer productServer = new WireMockServer(wireMockConfig()
                                                                    .port(8021)
                                                                    .usingFilesUnderDirectory("src/test/unit/resources"));

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ConsumerFactory<String, String> consumerFactory;

    @Autowired
    ProducerFactory<String, String> producerFactory;

    Consumer<String, String> consumer;

    Producer<String, String> producer;

    /* Dynamically override spring application properties value */
    @DynamicPropertySource
    private static void setDynamicProperties(DynamicPropertyRegistry registry){
        registry.add("product.host", () ->"http://127.0.0.1:8021");
    }

    @BeforeAll
    private void setup() throws Exception {
        // Setup for Kafka Producer / Consumer from EmbeddedKafka
        consumer = consumerFactory.createConsumer();
        producer = producerFactory.createProducer();

        // Setup WireMock for API calls
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
        configureFor("127.0.0.1", 8021);
        productServer.start();
    }

    @AfterAll
    private void tearDown(){
        // Stop Kafka Producer / Consumer from EmbeddedKafka
        consumer.close();
        producer.close();

        // Stop WireMock for API calls
        productServer.stop();
    }

    @Test
    @DisplayName("Test if Order received is published to Payments")
    @Timeout(10)
    public void testOrderTopic(@Value("${product.url.inquiry}") String inquiryPath, @Value("${product.host}") String host) {
        String product = "Phone";
        ResponseEntity<String> response = restTemplate.getForEntity("/kafka/order/"+product, String.class);

        log.info(response.getBody());
        Assertions.assertEquals("[{\"product\":\"Apple\"},{\"product\":\"OnePlus\"}]", response.getBody());

        ConsumerRecord<String, String> paymentRecord = KafkaTestUtils.getSingleRecord(consumer, "payment");
        String paymentRecordMessage = paymentRecord.value();
        Assertions.assertEquals(product, paymentRecordMessage);

    }

    @Test
    @DisplayName("Test if Order is posted to Order topic is passed to Payments topic")
    @Timeout(10)
    public void testPaymentTopic() throws ExecutionException, InterruptedException {
        String product = "Laptop";

        ProducerRecord<String,String> pr = new ProducerRecord<>("order",product,product);
        RecordMetadata recordMetadata = producer.send(pr).get();

        System.out.println(recordMetadata.topic() +" " + recordMetadata.offset());

        ConsumerRecord<String, String> paymentRecord = KafkaTestUtils.getSingleRecord(consumer,"payment");

        String paymentRecordMessage = paymentRecord.value();
        Assertions.assertEquals(product, paymentRecordMessage);

    }
}
