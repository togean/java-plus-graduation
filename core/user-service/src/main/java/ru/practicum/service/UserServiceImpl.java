package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserDtoMapper;
import ru.practicum.dto.User;
import ru.practicum.storage.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service("userServiceImpl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        final User user = userDtoMapper.mapFromDto(newUserRequest);
        final User createdUser = userRepository.save(user);
        return userDtoMapper.mapToDto(createdUser);
    }

    @Override
    public Collection<UserDto> findAll(List<Long> ids, Integer from, Integer size) {
        final Collection<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            users = userRepository.findAllById(ids);
        }
        return users.stream()
                .map(userDtoMapper::mapToDto)
                .sorted(Comparator.comparing(UserDto::getId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<UserShortDto> findAllInShort(List<Long> ids, Integer from, Integer size) {
        final Collection<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(PageRequest.of(from, size)).getContent();
        } else {
            users = userRepository.findAllById(ids);
        }
        return users.stream()
                .map(userDtoMapper::mapToShortDto)
                .sorted(Comparator.comparing(UserShortDto::getId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public UserShortDto findById(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " was not found")
        );
        return userDtoMapper.mapToShortDto(user);
    }

    @Override
    public void delete(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " was not found")
        );
        userRepository.delete(user);
    }
}
