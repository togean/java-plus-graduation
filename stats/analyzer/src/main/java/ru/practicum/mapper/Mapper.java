package ru.practicum.mapper;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.model.ActionType;
import ru.practicum.model.SimilarEvent;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;

public class Mapper {

    public static UserAction mapToUserAction(UserActionAvro userActionAvro) {
        return UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .actionType(mapToActionType(userActionAvro.getActionType()))
                .created(userActionAvro.getTimestamp())
                .weight(mapToActionType(userActionAvro.getActionType()).getWeight())
                .build();
    }

    public static ActionType mapToActionType(ActionTypeAvro actionTypeAvro) {
        return ActionType.valueOf(actionTypeAvro.name());
    }

    public static SimilarEvent mapToEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        return SimilarEvent.builder()
                .eventAId(eventSimilarityAvro.getEventA())
                .eventBId(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .build();
    }

    public static RecommendedEventProto mapToRecommendedEventProto(RecommendedEvent recommendedEvent) {
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedEvent.getEventId())
                .setScore(recommendedEvent.getScore())
                .build();
    }
}