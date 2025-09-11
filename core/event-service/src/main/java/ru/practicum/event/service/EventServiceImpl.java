package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.AnalyzerClient;
import ru.practicum.CollectorClient;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.*;
import ru.practicum.event.mapper.EventDtoMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.RequestClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.location.mapper.LocationDtoMapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.service.LocationService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("eventServiceImpl")
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final UserClient userClient;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final CategoryDtoMapper categoryDtoMapper;
    private final LocationDtoMapper locationDtoMapper;
    private final CollectorClient collectorClient;
    private final AnalyzerClient  analyzerClient;
    private final RequestClient requestClient;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto eventDto) {
        final UserShortDto user = userClient.findById(userId);

        checkEventDate(eventDto.getEventDate());
        final Event event = eventDtoMapper.mapFromDto(eventDto);
        event.setInitiatorId(user.getId());
        final Event createdEvent = eventRepository.save(event);

        return eventDtoMapper.mapToFullDto(createdEvent, user);
    }

    @Override
    public Collection<EventShortDto> findAllByPublic(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && LocalDateTime.parse(rangeStart, formatter).isAfter(LocalDateTime.parse(rangeEnd, formatter))) {
            throw new IncorrectRequestException("RangeStart is after Range End");
        }
        if (sort != null && !sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new IncorrectRequestException("Unknown sort type");
        }
        final Collection<Event> events = eventRepository.findAllByPublic(text, categories, paid, rangeStart == null ? null : LocalDateTime.parse(rangeStart, formatter), rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, formatter), onlyAvailable, (Pageable) PageRequest.of(from, size));
        final Collection<UserShortDto> usersDto = userClient.getShort(events.stream().map(Event::getInitiatorId).toList(), 0, 10);
        final Map<Long, UserShortDto> usersInfo = usersDto.stream().collect(Collectors.toMap(UserShortDto::getId, u -> u));
        return events.stream()
                .map(event -> {
                    final EventShortDto eventDto = eventDtoMapper.mapToShortDto(event, usersInfo.get(event.getInitiatorId()));
                    eventDto.setRating(getEventRating(event.getId()));
                    return eventDto;
                })
                .sorted((e1, e2) -> sort == null || sort.equals("EVENT_DATE") ? e1.getEventDate().compareTo(e2.getEventDate()) : e1.getRating().compareTo(e2.getRating()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<EventShortDto> findAllByPrivate(Long userId, Integer from, Integer size) {
        final UserShortDto user = userClient.findById(userId);
        final Collection<Event> events = eventRepository.findAllByInitiatorId(user.getId(), PageRequest.of(from, size));
        return events.stream()
                .map(event -> {
                    final EventShortDto eventDto = eventDtoMapper.mapToShortDto(event, user);
                    eventDto.setRating(getEventRating(event.getId()));
                    return eventDto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<EventFullDto> findAllByAdmin(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {
        final Collection<Event> events = eventRepository.findAllByAdmin(users, states, categories, rangeStart == null ? null : LocalDateTime.parse(rangeStart, formatter), rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, formatter), (Pageable) PageRequest.of(from, size));
        final Collection<UserShortDto> usersDto = userClient.getShort(users, 0, 10);
        final Map<Long, UserShortDto> usersInfo = usersDto.stream().collect(Collectors.toMap(UserShortDto::getId, u -> u));
        return events.stream()
                .map(event -> {
                    final EventFullDto eventDto = eventDtoMapper.mapToFullDto(event, usersInfo.get(event.getInitiatorId()));
                    eventDto.setRating(getEventRating(event.getId()));
                    return eventDto;
                })
                .collect(Collectors.toCollection(ArrayList::new));

    }

    @Override
    public List<EventShortDto> findAllById(List<Long> eventIds) {
        final Collection<Event> events = eventRepository.findAllById(eventIds);
        final Collection<UserShortDto> usersDto = userClient.getShort(events.stream().map(Event::getInitiatorId).toList(), 0, 10);
        final Map<Long, UserShortDto> usersInfo = usersDto.stream().collect(Collectors.toMap(UserShortDto::getId, u -> u));
        return events.stream()
                .map(event -> {
                    final EventShortDto eventDto = eventDtoMapper.mapToShortDto(event, usersInfo.get(event.getInitiatorId()));
                    eventDto.setRating(getEventRating(event.getId()));
                    return eventDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findById(Long userId, Long eventId, Boolean isPublic, HttpServletRequest request) {
        final Event event = findEventById(eventId);

        if (isPublic && !event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        } else if (isPublic) {
            collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW, Instant.now());
        } else if (userId != null) {
            userClient.findById(userId);
        }

        final UserShortDto user = userClient.findById(event.getInitiatorId());
        final EventFullDto eventDto = eventDtoMapper.mapToFullDto(event, user);
        eventDto.setRating(getEventRating(event.getId()));
        return eventDto;
    }

    @Override
    public EventFullDto findById(Long eventId) {
        final Event event = findEventById(eventId);

        final UserShortDto user = userClient.findById(event.getInitiatorId());
        final EventFullDto eventDto = eventDtoMapper.mapToFullDto(event, user);
        eventDto.setRating(getEventRating(event.getId()));
        return eventDto;
    }

    @Override
    public EventFullDto updateByPrivate(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        final UserShortDto user = userClient.findById(userId);
        final Event event = findEventById(eventId);

        checkUser(event.getInitiatorId(), user);
        checkEventDate(eventDto.getEventDate());
        checkStatusForPrivate(event.getState(), eventDto.getStateAction());

        final Category category = findCategoryById(eventDto.getCategory());
        final Location location = saveLocation(eventDto.getLocation());
        eventDtoMapper.updateFromDto(event, eventDto);

        final Event updatedEvent = eventRepository.save(event);

        final EventFullDto updatedEventDto = eventDtoMapper.mapToFullDto(updatedEvent, user);
        updatedEventDto.setRating(getEventRating(event.getId()));
        return updatedEventDto;
    }

    @Override
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest eventDto) {
        final Event event = findEventById(eventId);

        checkEventDateForAdmin(eventDto.getEventDate() == null ? event.getEventDate() : LocalDateTime.parse(eventDto.getEventDate(), formatter), eventDto.getStateAction());
        checkStatusForAdmin(event.getState(), eventDto.getStateAction());

        final Category category = findCategoryById(eventDto.getCategory());
        final Location location = saveLocation(eventDto.getLocation());
        eventDtoMapper.updateFromDto(event, eventDto);
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
            event.setPublishedOn(LocalDateTime.now());
        }

        final Event updatedEvent = eventRepository.save(event);
        final UserShortDto user = userClient.findById(updatedEvent.getInitiatorId());

        final EventFullDto updatedEventDto = eventDtoMapper.mapToFullDto(updatedEvent, user);
        updatedEventDto.setRating(getEventRating(event.getId()));
        return updatedEventDto;
    }

    @Override
    public void updateEventConfirmedRequests(Long eventId, Long confirmedRequests) {
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " нет в базе данных"));
        event.setConfirmedRequests(confirmedRequests);
        eventRepository.save(event);
    }

    private void checkEventDate(String eventDate) {
        if (eventDate != null && LocalDateTime.parse(eventDate, formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Event date should be early than 2 hours than current moment " + eventDate + " " + LocalDateTime.parse(eventDate, formatter));
        }
    }

    private Event findEventById(Long eventId) {
        final Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        return event;
    }

    private void checkUser(Long userId, UserShortDto initiator) {
        if (!initiator.getId().equals(userId)) {
            throw new NotFoundException("Trying to change information not from initiator of event");
        }
    }

    private void checkStatusForPrivate(EventState state, EventStateAction stateAction) {
        if (state.equals(EventState.PUBLISHED)) {
            throw new ConflictException("Can't change event not cancelled or in moderation");
        }
        switch (stateAction) {
            case null:
            case EventStateAction.CANCEL_REVIEW:
            case EventStateAction.SEND_TO_REVIEW:
                return;
            default:
                throw new ForbiddenException("Unknown state action");
        }
    }

    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        final CategoryDto categoryDto = categoryService.findById(categoryId);
        return categoryDtoMapper.mapFromDto(categoryDto);
    }

    private Location saveLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }
        final LocationDto createdLocationDto = locationService.create(locationDto);
        return locationDtoMapper.mapFromDto(createdLocationDto);
    }

    private void checkEventDateForAdmin(LocalDateTime eventDate, EventStateAction stateAction) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Event date should be early than 2 hours than current moment");
        }
        if (stateAction != null && stateAction.equals(EventStateAction.PUBLISH_EVENT) && eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ForbiddenException("Event date should be early than 1 hours than publish moment");
        }
    }

    private void checkStatusForAdmin(EventState state, EventStateAction stateAction) {
        if (!state.equals(EventState.PENDING) && stateAction.equals(EventStateAction.PUBLISH_EVENT)) {
            throw new ConflictException("Can't publish not pending event");
        }
        if (state.equals(EventState.PUBLISHED) && stateAction.equals(EventStateAction.REJECT_EVENT)) {
            throw new ConflictException("Can't reject already published event");
        }
        if (stateAction != null && !stateAction.equals(EventStateAction.REJECT_EVENT) && !stateAction.equals(EventStateAction.PUBLISH_EVENT)) {
            throw new ForbiddenException("Unknown state action");
        }
    }

    @Override
    public Collection<EventShortDto> getRecommendedEvents(Long userId, Integer maxResults) {
        List<Long> eventsIds = analyzerClient.getRecommendation(userId, maxResults)
                .map(RecommendedEventProto::getEventId)
                .collect(Collectors.toCollection(ArrayList::new));
        return findAllById(eventsIds);
    }

    @Override
    public void addLikeToEvent(Long eventId, Long userId){
        if (!requestClient.isUserRegisteredOnEvent(userId, eventId)) {
            throw new IncorrectRequestException("Пользователь не участвует в этом событии.");
        }
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }

    private Double getEventRating(Long eventId){
        return analyzerClient.getInteractionsCount(List.of(eventId))
                .map(RecommendedEventProto::getScore)
                .findFirst()
                .orElse(0.0);
    }
}
