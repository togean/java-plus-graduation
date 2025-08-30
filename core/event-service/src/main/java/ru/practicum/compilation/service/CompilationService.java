package ru.practicum.compilation.service;

import ru.practicum.dtomodels.CompilationDto;
import ru.practicum.dtomodels.NewCompilationDto;
import ru.practicum.dtomodels.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(Long id);

    List<CompilationDto> getAllCompilations(Integer from, Integer size, Boolean pinned);

    CompilationDto findCompilationById(Long compId);
}
