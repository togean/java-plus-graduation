package ru.practicum.service;

import ru.practicum.dtomodels.EventRequestStatusUpdateRequest;
import ru.practicum.dtomodels.EventRequestStatusUpdateResult;
import ru.practicum.dtomodels.ParticipationRequestDto;

import java.util.Collection;

public interface EventRequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestsToUpdate);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    Collection<ParticipationRequestDto> getByRequesterId(Long requesterId);

    Collection<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId);
}
