package ru.practicum.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.kafkaconfig.ConsumerType;
import ru.practicum.kafkaconfig.KafkaConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.RecommendationService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionRunner implements Runnable {

    private final KafkaConfig kafkaConfig;
    private final RecommendationService recommendationService;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        KafkaConsumer<String, UserActionAvro> consumer = kafkaConfig.createConsumer(ConsumerType.USER_ACTION.getKey());
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic(ConsumerType.USER_ACTION)));
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfig.getAttemptTimeout(ConsumerType.USER_ACTION)));
                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    analyzeRecord(record);
                    checkOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.info("Ошибка обработки события ", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консюмер");
                consumer.close();
            }
        }
    }

    private void analyzeRecord(ConsumerRecord<String, UserActionAvro> consumerRecord) {
        recommendationService.saveAction(consumerRecord.value());
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