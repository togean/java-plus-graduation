package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.kafkaconfig.KafkaConfiguration;
import ru.practicum.kafkaconfig.KafkaTopicSelector;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner {
    private final KafkaConfiguration kafkaConfiguration;
    private final KafkaTopicSelector kafkaTopicSelector;
    private final SimilarityCalculation similarityCalculation;
    private final Consumer<String, UserActionAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void run() {

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaTopicSelector.getUserActionsTopic()));
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfiguration.getKafkaProperties().getConsumer().getAttemptTimeout()));
                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    analyzeRecord(record);
                    checkOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignores) {
        } catch (Exception e) {
            log.error("Ошибка обработки событий", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                similarityCalculation.close();
            }
        }
    }

    private void analyzeRecord(ConsumerRecord<String, UserActionAvro> consumerRecord) throws InterruptedException {
        List<EventSimilarityAvro> eventSimilarityList = similarityCalculation.updateSimilarity(consumerRecord.value());
        for (EventSimilarityAvro eventSimilarity : eventSimilarityList) {
            similarityCalculation.collectEventSimilarity(eventSimilarity);
        }
    }

    private void checkOffsets(ConsumerRecord<String, UserActionAvro> consumerRecord, int count, Consumer<String, UserActionAvro> consumer) {
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