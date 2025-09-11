package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.SimilarEvent;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;
import ru.practicum.storage.EventSimilarityRepository;
import ru.practicum.storage.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public List<RecommendedEventProto> getRecommendationForUser(UserPredictionsRequestProto request) {
        log.info("RecommendationService: генерация рекоммендаций для пользователя: {}", request.getUserId());

        List<UserAction> lastUserActions = userActionRepository.findByUserIdOrderByCreatedDescLimitedTo(request.getUserId(), request.getMaxResults());

        if (lastUserActions.isEmpty()) {
            log.warn("RecommendationService: для пользователя {} не обнаружены активности, нечего предлагать", request.getUserId());
            return Collections.emptyList();
        }

        Set<Long> userInteractedEventIds = userActionRepository.findAllByUserId(request.getUserId()).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Set<Long> interactedEventIdsOnly = lastUserActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<SimilarEvent> allSimilarEvents = eventSimilarityRepository.findAllByEventIn(interactedEventIdsOnly);

        Map<Long, Double> userEventsWeights = lastUserActions.stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));

        Map<Long, Double> totalScores = new HashMap<>();

        for (SimilarEvent similarity : allSimilarEvents) {
            Long a = similarity.getEventAId();
            Long b = similarity.getEventBId();
            Long mainEventId = interactedEventIdsOnly.contains(a) ? a : b;
            Long similarEventId = mainEventId.equals(a) ? b : a;

            if (userInteractedEventIds.contains(similarEventId)) continue;

            double userWeight = userEventsWeights.getOrDefault(mainEventId, 0.0);
            double weightedScore = similarity.getScore() * userWeight;

            totalScores.merge(similarEventId, weightedScore, Double::sum);
        }

        List<RecommendedEvent> recommendedEvents = totalScores.entrySet().stream()
                .map(entry -> RecommendedEvent.builder()
                        .eventId(entry.getKey())
                        .score(entry.getValue())
                        .build())
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();

        return recommendedEvents.stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("RecommendationService: Запрос на получение схожих событий для собятия с eventId: {}", request.getEventId());
        Set<Long> userInteractedEventIds = userActionRepository.findAllByUserId(request.getUserId()).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
        List<SimilarEvent> similaritiesList = eventSimilarityRepository.findAllByEvent(request.getEventId());

        return similaritiesList.stream()
                .map(similarity -> {
                    Long similarEventId = getRecommendedEventId(similarity, request.getEventId());
                    return new AbstractMap.SimpleEntry<>(similarEventId, similarity.getScore());
                })
                .filter(event -> !userInteractedEventIds.contains(event.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return userActionRepository.getSumWeightForEvents(request.getEventIdList()).stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public void saveAction(UserActionAvro userActionAvro) {
        UserAction userAction = Mapper.mapToUserAction(userActionAvro);
        Optional<UserAction> existingAction = userActionRepository.findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());

        existingAction.ifPresent(old -> {
            userAction.setId(old.getId());
            if (userAction.getWeight() < old.getWeight()) {
                userAction.setWeight(old.getWeight());
            }
        });
        userActionRepository.save(userAction);
    }

    private Long getRecommendedEventId(SimilarEvent similarity, Long referenceEventId) {
        return Objects.equals(similarity.getEventAId(), referenceEventId)
                ? similarity.getEventBId()
                : similarity.getEventAId();
    }
}