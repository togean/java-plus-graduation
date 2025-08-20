package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserDtoMapper;
import ru.practicum.model.NewUserRequest;
import ru.practicum.model.User;
import ru.practicum.model.UserDto;
import ru.practicum.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service("userServiceImpl")
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ValidationException("UserServiceImpl: Ошибка: такой пользователь уже существует") {
            };
        }
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
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public UserDto findById(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("UserServiceImpl: Ошибка: пользователь с Id" + userId + " не найден")
        );
        return userDtoMapper.mapToDto(user);
    }

    @Override
    public void delete(Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("UserServiceImpl: Ошибка: пользователь с Id" + userId + " не найден")
        );
        userRepository.delete(user);
    }
}
