package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dtomodels.NewUserRequest;
import ru.practicum.dtomodels.UserDto;
import ru.practicum.dtomodels.UserShortDto;
import ru.practicum.dtomodels.User;

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
