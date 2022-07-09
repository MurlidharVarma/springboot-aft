package org.aipeel.kafkatesting.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile({"!aft"})
public class KafkaTopicConfig {

    @Value("${kafka.bootstrap-server}")
    String bootstrapServer;

    @Value("${kafka.topic.order}")
    String orderTopic;

    @Value("${kafka.topic.payment}")
    String paymentTopic;

    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String,String> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        KafkaAdmin kafkaAdmin = new KafkaAdmin(Collections.unmodifiableMap(config));
        return kafkaAdmin;
    }

    /* Create a new topic called order */
    @Bean
    public NewTopic order(){
        return TopicBuilder.name(orderTopic).partitions(2).build();
    }

    /* Create a new topic called payment */
    @Bean
    public NewTopic payment(){
        return TopicBuilder.name(paymentTopic).partitions(2).build();
    }
}
