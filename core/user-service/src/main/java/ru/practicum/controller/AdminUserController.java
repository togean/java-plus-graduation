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
import ru.practicum.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Пришел запрос на создание пользователя");
        final UserDto userDto = userService.create(newUserRequest);
        return userDto;
    }

    @GetMapping
    public Collection<UserDto> get(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Пришел запрос /admin/users?from={}&size={} по клиентам {}", from, size, ids);
        final Collection<UserDto> users = userService.findAll(ids, from, size);
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Пришел запрос на удаление пользователя с Id={}", userId);
        userService.delete(userId);
    }
}
