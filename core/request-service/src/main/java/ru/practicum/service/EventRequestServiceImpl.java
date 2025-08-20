package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.client.EventClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.EventRequestDtoMapper;
import ru.practicum.model.*;
import ru.practicum.storage.EventRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("eventRequestServiceImpl")
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {
    private final EventRequestRepository eventRequestRepository;
    private final EventRequestDtoMapper eventRequestDtoMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        final UserDto user = userClient.findById(userId);
        final EventFullDto event = eventClient.findById(eventId);
        if (eventRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ValidationException("EventRequestServiceImpl: Ошибка: Такой запрос уже существует");
        }
        if (event.getInitiator().equals(user.getId())) {
            throw new ValidationException("EventRequestServiceImpl: Инициатор и пользователь являются одним и тем же лицом");
        }
        checkEventForRequest(event);
        EventRequestStatus status;
        if(event.getParticipantLimit().equals(0L) || !event.getRequestModeration()){
            status =EventRequestStatus.CONFIRMED;
        }else{
            status =EventRequestStatus.PENDING;
        }

        final EventRequest request = new EventRequest(
                null,
                eventId,
                userId,
                status,
                LocalDateTime.now()
        );
        final EventRequest createdRequest = eventRequestRepository.save(request);
        if (status.equals(EventRequestStatus.CONFIRMED)) {
            eventClient.updateConfirmedRequests(event.getId(), event.getConfirmedRequests() + 1);
        }

        return eventRequestDtoMapper.mapToResponseDto(createdRequest);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestsToUpdate) {
        final UserDto user = userClient.findById(userId);
        final EventFullDto event = eventClient.findById(eventId);
        if (eventRequestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ValidationException("EventRequestServiceImpl: Ошибка: Такой запрос уже существует");
        }
        if (event.getInitiator().equals(user.getId())) {
            throw new ValidationException("EventRequestServiceImpl: Инициатор и пользователь являются одним и тем же лицом");
        }

        checkEventForRequest(event);

        final Collection<EventRequest> requests = eventRequestRepository.findById(requestsToUpdate.getRequestIds());

        checkRequests(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        switch (requestsToUpdate.getStatus()) {
            case REJECTED -> rejectRequests(result, requests);
            case CONFIRMED -> confirmRequests(result, event, requests);
            default -> throw new ForbiddenException("EventRequestServiceImpl: Ошибка: указан неизвестный статус");
        }
        return result;
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        final EventRequest request = eventRequestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("EventRequestServiceImpl: Ошибка: Запрос с Id=" + requestId + " не найден")
        );
        if (!userId.equals(request.getRequesterId())) {
            throw new NotFoundException("EventRequestServiceImpl: Ошибка: Не владелец запроса не может отменить его");
        }
        request.setStatus(EventRequestStatus.CANCELED);
        final EventRequest updatedRequest = eventRequestRepository.save(request);
        return eventRequestDtoMapper.mapToResponseDto(updatedRequest);
    }

    @Override
    public Collection<ParticipationRequestDto> getByRequesterId(Long requesterId) {
        if (userClient.findById(requesterId) == null) {
            throw new NotFoundException("EventRequestServiceImpl: Ошибка: Пользователь id=" + requesterId + " не найден");
        }
        final Collection<EventRequest> requests = eventRequestRepository.findByRequesterId(requesterId);
        return requests.stream()
                .map(eventRequestDtoMapper::mapToResponseDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId) {
        if (userClient.findById(eventInitiatorId) == null) {
            throw new NotFoundException("EventRequestServiceImpl: Ошибка: Пользователь id=" + eventInitiatorId + " не найден");
        }
        if (eventClient.findById(eventId) == null) {
            throw new NotFoundException("EventRequestServiceImpl: Ошибка: Событий с id=" + eventId + " не найдено");
        }
        final Collection<EventRequest> requests = eventRequestRepository.findByEventId(eventId);
        return requests.stream()
                .map(eventRequestDtoMapper::mapToResponseDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkEventForRequest(EventFullDto event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ValidationException("EventRequestServiceImpl: Ошибка: Данное событие не опубликовано.");
        }
        if (event.getParticipantLimit().equals(0L)) {
            return;
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ValidationException("EventRequestServiceImpl: Ошибка: выход за установленные лимиты");
        }
    }

    private void checkRequests(Collection<EventRequest> requests) {
        for (EventRequest request : requests) {
            if (!request.getStatus().equals(EventRequestStatus.PENDING)) {
                throw new ValidationException("EventRequestServiceImpl: Попытка изменить статус запроса, который не в статусе PENDING");
            }
        }
    }

    private void rejectRequests(EventRequestStatusUpdateResult result, Collection<EventRequest> requests) {
        for (EventRequest request : requests) {
            request.setStatus(EventRequestStatus.REJECTED);
            final EventRequest updatedRequest = eventRequestRepository.save(request);
            result.getRejectedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
        }
    }

    private void confirmRequests(EventRequestStatusUpdateResult result, EventFullDto event, Collection<EventRequest> requests) {

        final Long limit = event.getParticipantLimit();
        Long currentConfirmed = event.getConfirmedRequests();

        for (EventRequest request : requests) {
            if (currentConfirmed >= limit) {
                request.setStatus(EventRequestStatus.REJECTED);
                final EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getRejectedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
            } else {
                request.setStatus(EventRequestStatus.CONFIRMED);
                final EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getConfirmedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
                currentConfirmed++;
            }
        }

        eventClient.updateConfirmedRequests(event.getId(), currentConfirmed);
    }
}
