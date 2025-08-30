package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationDtoMapper {

    @Mapping(target = "events", source = "eventsDto")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto compilationDto);
}
