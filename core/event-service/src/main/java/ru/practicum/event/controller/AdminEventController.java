package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dtomodels.EventFullDto;
import ru.practicum.dtomodels.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.client.EventClient;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController implements EventClient {
    private final EventService eventService;

    @GetMapping
    public Collection<EventFullDto> get(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("AdminEventController: Пришел GET запрос /admin/events с параметрами: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        final Collection<EventFullDto> events = eventService.findAllByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("AdminEventController: Отправлен ответ GET /admin/events с телом: {}", events);
        return events;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest eventDto) {
        log.info("Пришел PATCH запрос /admin/events/{} с телом {}", eventId, eventDto);
        final EventFullDto event = eventService.updateByAdmin(eventId, eventDto);
        log.info("Отправлен ответ PATCH /admin/events/{} с телом: {}", eventId, event);
        return event;
    }

    @Override
    public EventFullDto findById(Long eventId) {
        log.info("Пришел GET запрос /admin/events/{}", eventId);
        final EventFullDto event = eventService.findById(eventId);
        log.info("Отправлен ответ GET /admin/events/{} с телом: {}", eventId, event);
        return event;
    }

    @Override
    public void updateEventConfirmedRequests(Long eventId, Long confirmedRequests) {
        log.info("Пришел PATCH запрос /admin/events/confirmed с параметрами eventId={} и confirmedRequests={}", eventId, confirmedRequests);
        eventService.updateEventConfirmedRequests(eventId, confirmedRequests);
        log.info("Отработан PATCH /admin/events/confirmed с параметрами eventId={} и confirmedRequests={}", eventId, confirmedRequests);
    }
}
