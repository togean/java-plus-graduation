package ru.practicum.feign.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dtomodels.NewUserRequest;
import ru.practicum.dtomodels.UserDto;
import ru.practicum.dtomodels.UserShortDto;

import java.util.Collection;
import java.util.List;

@FeignClient(name="user-service", path="/admin/users")
public interface UserClient {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto create(@RequestBody @Valid NewUserRequest newUserRequest);

    @GetMapping("/short")
    Collection<UserShortDto> getShort(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    );

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long userId);

    @GetMapping("/{userId}")
    UserShortDto findById(@PathVariable Long userId);
}
