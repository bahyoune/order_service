package com.microtest.OrderService.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.order}")
    private String topics;
    @Value("${app.kafka.topic.partition}")
    private int partition_topic;
    @Value("${app.kafka.topic.replicate}")
    private short replicate_topic;

    @Value("${app.kafka.dlt.order}")
    private String topicsDLT;
    @Value("${app.kafka.dlt.partition}")
    private int partition_DLT;
    @Value("${app.kafka.dlt.replicate}")
    private short replicate_DLT;



    @Bean
    public NewTopic orderTopic() {
        return new NewTopic(topics,
                partition_topic,
                replicate_topic);
    }

    @Bean
    public NewTopic orderTopicDLT() {
        return new NewTopic(topicsDLT,
                partition_DLT,
                replicate_DLT);
    }


}
