package ru.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.kafkaconfig.KafkaClient;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserActionServiceImpl implements UserActionService {

    @Value("${spring.kafka.topics.actions-topic}")
    private String topic;

    private final KafkaClient kafkaClient;

    @Override
    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setEventId(userActionProto.getEventId())
                .setUserId(userActionProto.getUserId())
                .setActionType(getActionType(userActionProto.getActionType()))
                .setTimestamp(mapToInstant(userActionProto.getTimestamp()))
                .build();
        log.info("Отправляем сообщение {} в topic {}", userActionProto, topic);
        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                topic, null, userActionAvro.getTimestamp().toEpochMilli(), null, userActionAvro);
        kafkaClient.getProducer().send(producerRecord);
        log.info("Сообщение от пользователя с ID = {} отправлено в топик: {}", userActionProto.getUserId(), topic);
    }

    private Instant mapToInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private ActionTypeAvro getActionType(ActionTypeProto actionTypeProto) {
        switch (actionTypeProto) {
            case ACTION_VIEW -> {
                return ActionTypeAvro.VIEW;
            }
            case ACTION_REGISTER -> {
                return ActionTypeAvro.REGISTER;
            }
            case ACTION_LIKE -> {
                return ActionTypeAvro.LIKE;
            }
            default -> {
                throw new IncorrectRequestException("Неизвестный тип ActionTypeProto: " + actionTypeProto);
            }
        }
    }
}
