package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.EventClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.CommentDtoMapper;
import ru.practicum.dto.Comment;
import ru.practicum.storage.CommentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service("commentServiceImpl")
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final EventClient eventClient;
    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final CommentDtoMapper commentDtoMapper;

    @Override
    public CommentDto create(Long userId, NewCommentDto commentDto) {
        final UserShortDto userDto = userClient.findById(userId);
        final EventFullDto eventDto = eventClient.findById(commentDto.getEvent());

        final Comment comment = commentDtoMapper.mapFromDto(commentDto);
        comment.setAuthorId(userDto.getId());
        comment.setEventId(eventDto.getId());
        if (commentDto.getReplyOn() != null) {
            final Comment replyOnComment = findById(commentDto.getReplyOn());
            comment.setReplyOn(replyOnComment);
        }
        final Comment createdComment = commentRepository.save(comment);
        return commentDtoMapper.mapToDto(createdComment, userDto);
    }

    @Override
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto) {
        final UserShortDto userDto = userClient.findById(userId);
        final Comment comment = findById(commentId);
        commentDtoMapper.updateFromDto(comment, commentDto);

        final Comment updatedComment = commentRepository.save(comment);
        return commentDtoMapper.mapToDto(updatedComment, userDto);
    }

    @Override
    public Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size) {
        final UserShortDto userDto = userClient.findById(userId);
        final Collection<Comment> comments = commentRepository.findAllByAuthorIdOrderByCreatedOn(userDto.getId(), (Pageable) PageRequest.of(from, size));
        return comments.stream()
                .map(c -> commentDtoMapper.mapToDto(c, userDto))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size) {
        final EventFullDto event = eventClient.findById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            return new ArrayList<>();
        }
        final Collection<Comment> comments = commentRepository.findAllByEventIdOrderByCreatedOn(event.getId(), (Pageable) PageRequest.of(from, size));
        final Collection<UserShortDto> usersDto = userClient.getShort(comments.stream().map(Comment::getAuthorId).toList(), 0, 10);
        final Map<Long, UserShortDto> usersInfo = usersDto.stream().collect(Collectors.toMap(UserShortDto::getId, user -> user));
        return comments.stream()
                .map(c -> commentDtoMapper.mapToDto(c, usersInfo.get(c.getAuthorId())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void delete(Long userId, Long commentId) {
        final Comment comment = findById(commentId);
        if (userId != null) {
            final UserShortDto userDto = userClient.findById(userId);
            if (!userDto.getId().equals(comment.getAuthorId())) {
                throw new IncorrectRequestException("Trying to delete comment not from author");
            }
        }
        commentRepository.delete(comment);
    }

    private Comment findById(Long commentId) {
        final Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment with id=" + commentId + " was not found")
        );
        return comment;
    }
}
