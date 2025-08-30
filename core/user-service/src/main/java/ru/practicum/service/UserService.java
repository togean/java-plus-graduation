package ru.practicum.service;

import ru.practicum.dtomodels.NewUserRequest;
import ru.practicum.dtomodels.UserDto;
import ru.practicum.dtomodels.UserShortDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest newUserRequest);

    Collection<UserDto> findAll(List<Long> ids, Integer from, Integer size);

    Collection<UserShortDto> findAllInShort(List<Long> ids, Integer from, Integer size);

    UserShortDto findById(Long userId);

    void delete(Long userId);
}
