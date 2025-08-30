package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;

@FeignClient(name="event-service", path="admin/events")
public interface EventClient {

    @GetMapping("/{eventId}")
    EventFullDto findById(@PathVariable Long eventId);

    @PostMapping("/confirmed")
    void updateEventConfirmedRequests(@RequestParam Long eventId, @RequestParam Long confirmedRequests);

}
