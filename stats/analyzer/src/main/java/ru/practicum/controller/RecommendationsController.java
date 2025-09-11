package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController: Запрос рекомендаций для пользователя {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.getRecommendationForUser(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController: Запрос похожих событий {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.getSimilarEvents(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController: Запрос кол-ва взаимодействий {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.getInteractionsCount(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }
}