package ru.practicum.kafkaconfig;

import lombok.Data;

import java.util.Map;

@Data
public class ConsumerProperties {
    private String clientId;
    private String groupId;
    private String keyDeserializer;
    private String valueDeserializer;
    private long attemptTimeout;
    private String enableAutoCommit;
    private Map<String, String> topics;
}