package ru.practicum.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {
    LocationDto mapToDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location mapFromDto(LocationDto locationDto);
}
