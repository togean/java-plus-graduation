package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.kafkaconfig.KafkaTopicSelector;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProducer {

    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaTopicSelector kafkaTopicSelector;

    public void send(EventSimilarityAvro eventSimilarityAvro) {
        String topic = kafkaTopicSelector.getEventSimilarityTopic();
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                null,
                eventSimilarityAvro.getTimestamp().toEpochMilli(),
                String.valueOf(eventSimilarityAvro.getEventA()),
                eventSimilarityAvro
        );
        producer.send(record, (data, exception) -> {
            if (exception != null) {
                log.info("Ошибка при отправке сообщения в шину", exception);
            } else {
                log.info("Сообщение отправлено: topic={}, partition={}, offset={}",
                        data.topic(), data.partition(), data.offset());
            }
        });
    }
}