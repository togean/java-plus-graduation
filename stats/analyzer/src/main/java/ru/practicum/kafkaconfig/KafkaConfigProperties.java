package ru.practicum.kafkaconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "analyzer.kafka.config")
public class KafkaConfigProperties {
    private String bootstrapServers;
    private String userActionTopic;
    private String eventsSimilarityTopic;
    private Map<String, ConsumerProperties> consumers = new HashMap<>();
}