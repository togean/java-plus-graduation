package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.mapper.CompilationDtoMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationRepository;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("compilationServiceImpl")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;
    private final CompilationDtoMapper compilationDtoMapper;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Добавление подборки {}", newCompilationDto.toString());
        Compilation compilation = compilationDtoMapper.toCompilation(newCompilationDto);

        compilation.setPinned(Optional.ofNullable(compilation.getPinned()).orElse(false));

        final List<Long> compilationEventIds = Optional.ofNullable(newCompilationDto.getEvents())
                .orElse(Collections.emptySet()).stream().toList();
        final List<Long> eventIds = compilationEventIds.stream().toList();
        final List<EventShortDto> events = eventService.findAllById(eventIds);
        compilation.setEvents(eventIds);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка добавлена: {}", savedCompilation);

        return compilationDtoMapper.toCompilationDto(savedCompilation, events);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = validateCompilation(compId);
        log.info("Обновление подборки c {}, на {}", updateCompilationRequest.toString(), compilation.toString());
        List<EventShortDto> events = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Long> eventIds = new ArrayList<>(updateCompilationRequest.getEvents());
            events.addAll(eventService.findAllById(eventIds));
            compilation.setEvents(eventIds);
            log.trace("Events = {}", compilation.getEvents());
        }

        compilation.setPinned(Optional.ofNullable(updateCompilationRequest.getPinned()).orElse(false));
        log.trace("Pinned = {}", compilation.getPinned());

        compilation.setTitle(Optional.ofNullable(updateCompilationRequest.getTitle()).orElse(compilation.getTitle()));
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена: {}", compilation);

        return compilationDtoMapper.toCompilationDto(updatedCompilation, events);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long id) {
        log.info("Удаление подборки c {}", id);
        validateCompilation(id);
        compilationRepository.deleteById(id);
        log.info("Подборка удалена");
    }

    @Override
    public List<CompilationDto> getAllCompilations(Integer from, Integer size, Boolean pinned) {
        log.info("Получение всех подборок с from={}, size={}, pinned={}", from, size, pinned);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned != null) {
            log.info("Получение всех подборок с pinned: {}", pinned);
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
            log.info("Получены подборки с pinned={}: {}", pinned, compilations);
        } else {
            log.info("Получение всех подборок без фильтрации по pinned");
            compilations = compilationRepository.findAll(pageRequest).getContent();
            log.info("Получены все подборки: {}", compilations);

        }
        final List<EventShortDto> events = eventService.findAllById(
                compilations.stream()
                        .flatMap(c -> c.getEvents().stream())
                        .collect(Collectors.toSet()).stream().toList()
        );
        final Map<Long, EventShortDto> eventsInfo = events.stream().collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> eventsShortDto = compilation.getEvents().stream()
                            .map(eventsInfo::get)
                            .toList();

                    return compilationDtoMapper.toCompilationDto(compilation, eventsShortDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("Получение подборки с compId={}", compId);
        Compilation compilation = validateCompilation(compId);
        log.info("Подборка найдена: {}", compilation);
        List<EventShortDto> events = new ArrayList<>();
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            events = eventService.findAllById(compilation.getEvents());
        }

        return compilationDtoMapper.toCompilationDto(compilation, events);
    }

    private Compilation validateCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id = " + compId + " не найдена."));
    }
}
