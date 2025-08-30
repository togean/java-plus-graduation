package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dtomodels.CompilationDto;
import ru.practicum.dtomodels.EventShortDto;
import ru.practicum.dtomodels.NewCompilationDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationDtoMapper {

    @Mapping(target = "events", source = "eventsDto")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto compilationDto);
}
