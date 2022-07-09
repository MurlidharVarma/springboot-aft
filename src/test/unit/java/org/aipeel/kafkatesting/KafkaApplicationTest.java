package org.aipeel.kafkatesting;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.ExecutionException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"order","payment"}, partitions = 1)
@TestPropertySource(properties = {"kafka.bootstrap-server=${spring.embedded.kafka.brokers}"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("aft")
public class KafkaApplicationTest {

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

    @BeforeAll
    private void setup() throws Exception {
        consumer = consumerFactory.createConsumer();
        producer = producerFactory.createProducer();

        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

    }

    @AfterAll
    private void tearDown(){
        consumer.close();
        producer.close();
    }

    @Test
    @DisplayName("Test if Order received is published to Payments")
    @Timeout(10)
    public void testOrderTopic() {
        String product = "Boombox";
        ResponseEntity<String> response = restTemplate.getForEntity("/kafka/order/"+product, String.class);
        Assertions.assertEquals("Message posted", response.getBody());

        ConsumerRecord<String, String> paymentRecord = KafkaTestUtils.getSingleRecord(consumer, "payment");
        String paymentRecordMessage = paymentRecord.value();
        Assertions.assertEquals(product, paymentRecordMessage);

    }

    @Test
    @DisplayName("Test if Order is posted to Order topic is passed to Payments topic")
    @Timeout(10)
    public void testPaymentTopic() throws ExecutionException, InterruptedException {
        String product = "Radio";

        ProducerRecord<String,String> pr = new ProducerRecord<>("order",product,product);
        RecordMetadata recordMetadata = producer.send(pr).get();

        System.out.println(recordMetadata.topic() +" " + recordMetadata.offset());

        ConsumerRecord<String, String> paymentRecord = KafkaTestUtils.getSingleRecord(consumer,"payment");

        String paymentRecordMessage = paymentRecord.value();
        Assertions.assertEquals(product, paymentRecordMessage);

    }
}
