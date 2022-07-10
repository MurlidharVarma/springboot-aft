package org.aipeel.kafkatesting.controller;

import lombok.extern.slf4j.Slf4j;
import org.aipeel.kafkatesting.model.Product;
import org.aipeel.kafkatesting.service.KafkaService;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kafka")
@Slf4j
public class KafkaController {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Value("${kafka.topic.order}")
    String orderTopic;

    @Value("${kafka.topic.payment}")
    String paymentTopic;

    @Value("${product.url.inquiry}")
    String productInquiryUrl;

    @Value("${product.host}")
    String productHost;

    @GetMapping("/order/{product}")
    public List<Map> getOrder(@PathVariable("product") String product){
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(orderTopic, product, product);
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

        ResponseEntity<Map> response = restTemplate.exchange(productHost+productInquiryUrl+product, HttpMethod.GET,null, Map.class);
        Map resMap = response.getBody();

        return (List<Map>) resMap.get("products");
    }

}
