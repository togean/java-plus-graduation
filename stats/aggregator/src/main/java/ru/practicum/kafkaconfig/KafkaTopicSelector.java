package ru.practicum.kafkaconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaTopicSelector {
    private final KafkaConfiguration kafkaConfiguration;

    public String getUserActionsTopic() {
        return kafkaConfiguration.getKafkaProperties()
                .getConsumer()
                .getTopics()
                .get(Topics.USER_ACTIONS);
    }

    public String getEventSimilarityTopic() {
        return kafkaConfiguration.getKafkaProperties()
                .getProducer()
                .getTopics()
                .get(Topics.EVENT_SIMILARITY);
    }
}