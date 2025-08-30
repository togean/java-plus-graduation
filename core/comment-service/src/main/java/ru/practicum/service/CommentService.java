package ru.practicum.service;

import ru.practicum.dtomodels.CommentDto;
import ru.practicum.dtomodels.NewCommentDto;
import ru.practicum.dtomodels.UpdateCommentDto;

import java.util.Collection;

public interface CommentService {

    CommentDto create(Long userId, NewCommentDto commentDto);

    CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto);

    Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size);

    Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size);

    void delete(Long userId, Long commentId);
}
