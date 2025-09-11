package ru.practicum.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.kafkaconfig.ConsumerType;
import ru.practicum.kafkaconfig.KafkaConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.SimilarEvent;
import ru.practicum.storage.EventSimilarityRepository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityRunner implements Runnable {

    private final KafkaConfig kafkaConfig;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        KafkaConsumer<String, EventSimilarityAvro> consumer = kafkaConfig.createConsumer(ConsumerType.EVENT_SIMILARITY.getKey());
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic(ConsumerType.EVENT_SIMILARITY)));
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfig.getAttemptTimeout(ConsumerType.EVENT_SIMILARITY)));
                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    analyzeRecord(record);
                    checkOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.info("Ошибка во время обработки события ", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консюмер");
                consumer.close();
            }
        }
    }

    private void analyzeRecord(ConsumerRecord<String, EventSimilarityAvro> consumerRecord) {
        SimilarEvent similarEvent = Mapper.mapToEventSimilarity(consumerRecord.value());
        eventSimilarityRepository.findByEventAIdAndEventBId(similarEvent.getEventAId(), similarEvent.getEventBId())
                .ifPresent(old -> similarEvent.setId(old.getId()));
        eventSimilarityRepository.save(similarEvent);
    }

    private void checkOffsets(ConsumerRecord<String, EventSimilarityAvro> consumerRecord, int count, Consumer<String, EventSimilarityAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                new OffsetAndMetadata(consumerRecord.offset() + 1)
        );
        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.info("Ошибка фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}