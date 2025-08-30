package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.dto.User;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    UserDto mapToDto(User user);

    UserShortDto mapToShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User mapFromDto(NewUserRequest newUserRequest);

    User mapFromDto(UserDto userDto);

    @Mapping(target = "email", ignore = true)
    User mapFromDto(UserShortDto userDto);
}
