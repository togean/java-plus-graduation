package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="request-service", path="/users/{userId}")
public interface RequestClient {
    @GetMapping("/events/{eventId}/isregistered")
    Boolean isUserRegisteredOnEvent(@RequestParam Long userId, @RequestParam Long eventId);
}
