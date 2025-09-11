package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityCalculationImpl implements SimilarityCalculation {

    private final EventSimilarityProducer similarityProducer;
    private final Map<Long, Double> eventSummaryWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();
    private final Map<Long, Double> minSum = new HashMap<>();

    private List<EventSimilarityAvro> updateSimilarityWithUser(Long eventId, Long userId, double oldWeight, double newWeight, Instant timestamp) {
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        Set<Long> userEventIds = eventWeights.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(userId))
                .map(Map.Entry::getKey)
                .filter(id -> !id.equals(eventId))
                .collect(Collectors.toSet());

        for (Long otherEventId : userEventIds) {
            if(eventId.equals(otherEventId)) continue;
            Map<Long, Double> otherUsers = eventWeights.get(otherEventId);
            if(!(otherUsers != null && otherUsers.containsKey(userId)))continue;
            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);
            long uniqueKey = genKey(eventA, eventB);
            double score = calculateNewScore(eventA, eventB, uniqueKey, userId, eventId, oldWeight);
            if (score < 0) continue;
            similarities.add(EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(score)
                    .setTimestamp(timestamp)
                    .build());
        }
        return similarities;
    }

    private double calculateNewScore(long eventA, long eventB, long key, Long userId, Long changedEventId, double oldWeight) {

        double oldWeightForA = eventA == changedEventId ? oldWeight : eventWeights.get(eventA).getOrDefault(userId, 0.0);
        double oldWeightForB = eventB == changedEventId ? oldWeight : eventWeights.get(eventB).getOrDefault(userId, 0.0);
        double oldMinimum = Math.min(oldWeightForA, oldWeightForB);
        double newWeightForA = eventWeights.get(eventA).getOrDefault(userId, 0.0);
        double newWeightForB = eventWeights.get(eventB).getOrDefault(userId, 0.0);
        double newMinimum = Math.min(newWeightForA, newWeightForB);
        double delta = newMinimum - oldMinimum;
        double updatedMinimumSum = minSum.getOrDefault(key, 0.0) + delta;

        minSum.put(key, updatedMinimumSum);

        double sumForA = eventSummaryWeights.getOrDefault(eventA, 0.0);
        double sumForB = eventSummaryWeights.getOrDefault(eventB, 0.0);

        if (sumForA <= 0 || sumForB <= 0) return -1;

        return updatedMinimumSum / (Math.sqrt(sumForA) * Math.sqrt(sumForB));
    }

    @Override
    public void collectEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        similarityProducer.send(eventSimilarityAvro);
    }

    @Override
    public List<EventSimilarityAvro> updateSimilarity(UserActionAvro userAction) {

        Long userId = userAction.getUserId();
        Long eventId = userAction.getEventId();
        Map<Long, Double> userWeights = eventWeights.computeIfAbsent(eventId, e -> new HashMap<>());

        double receivedWeight = 0.0;

        if(userAction.getActionType().equals(ActionTypeAvro.VIEW)){
            receivedWeight = 0.4;
        }
        if(userAction.getActionType().equals(ActionTypeAvro.REGISTER)){
            receivedWeight = 0.8;
        }
        if(userAction.getActionType().equals(ActionTypeAvro.LIKE)){
            receivedWeight = 1.0;
        }
        double oldWeight = userWeights.getOrDefault(userId, 0.0);
        double maxWeight = Math.max(oldWeight, receivedWeight);
        if (oldWeight != maxWeight) {
            userWeights.put(userId, maxWeight);
        }

        double newWeight = Math.max(oldWeight, receivedWeight);

        if (oldWeight == newWeight) {
            log.info("Вес не изменился, пересчёт не требуется");
            return List.of();
        }

        eventSummaryWeights.put(eventId, eventSummaryWeights.getOrDefault(eventId, 0.0) + newWeight - oldWeight);
        return updateSimilarityWithUser(eventId, userId, oldWeight, newWeight, userAction.getTimestamp());
    }

    private long genKey(long eventA, long eventB) {
        return eventA * 1_000_000_000L + eventB;
    }

}