package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.NewUserRequest;
import ru.practicum.model.UserDto;
import ru.practicum.model.UserShortDto;
import ru.practicum.feign.client.UserClient;
import ru.practicum.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController implements UserClient {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Пришел POST запрос /admin/users с телом {}", newUserRequest);
        final UserDto userDto = userService.create(newUserRequest);
        log.info("Отправлен ответ POST /admin/users с телом {}", userDto);
        return userDto;
    }

    @GetMapping
    public Collection<UserDto> get(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Пришел GET запрос /admin/users?from={}&size={} по клиентам {}", from, size, ids);
        final Collection<UserDto> users = userService.findAll(ids, from, size);
        log.info("Отправлен ответ GET /admin/users?from={}&size={} с телом {}", from, size, users);
        return users;
    }

    @Override
    public Collection<UserShortDto> getShort(List<Long> ids, Integer from, Integer size) {
        log.info("Пришел GET запрос /admin/users/short?from={}&size={} по клиентам {}", from, size, ids);
        final Collection<UserShortDto> users = userService.findAllInShort(ids, from, size);
        log.info("Отправлен ответ GET /admin/users/short?from={}&size={} с телом {}", from, size, users);
        return users;
    }

    @Override
    public UserShortDto findById(Long userId) {
        log.info("Пришел GET запрос /admin/users?id={}", userId);
        final UserShortDto user = userService.findById(userId);
        log.info("Отправлен ответ GET /admin/users?id={} с телом {}", userId, user);
        return user;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Пришел DELETE запрос /admin/users/{}", userId);
        userService.delete(userId);
        log.info("Отправлен ответ DELETE /admin/users/{}", userId);
    }
}
