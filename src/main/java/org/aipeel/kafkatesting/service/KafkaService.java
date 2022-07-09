package org.aipeel.kafkatesting.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Value("${kafka.topic.order}")
    String orderTopic;

    @Value("${kafka.topic.payment}")
    String paymentTopic;

    @KafkaListener(topics = "order", groupId = "cg1")
    public void payment(@Payload String product, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key){
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(paymentTopic, "id_"+key, product);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error publishing message: {}",ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("{}: Partition {} : Offset {} : Timestamp: {} : Message: {}",recordMetadata.topic(),recordMetadata.partition(),recordMetadata.offset(),recordMetadata.timestamp(),result.getProducerRecord().value());
            }
        });
    }

}
