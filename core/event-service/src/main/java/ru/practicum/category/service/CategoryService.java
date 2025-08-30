package ru.practicum.category.service;

import ru.practicum.dtomodels.CategoryDto;
import ru.practicum.dtomodels.NewCategoryDto;

import java.util.Collection;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    Collection<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto findById(Long categoryId);

    CategoryDto update(Long categoryId, CategoryDto categoryDto);

    void delete(Long categoryId);
}
