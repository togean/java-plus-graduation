package ru.practicum.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface SimilarityCalculation {
    List<EventSimilarityAvro> updateSimilarity(UserActionAvro userAction);

    void collectEventSimilarity(EventSimilarityAvro eventSimilarityAvro);

    default void close() {
    }
}