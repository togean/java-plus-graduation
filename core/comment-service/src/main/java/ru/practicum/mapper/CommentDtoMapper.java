package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.model.CommentDto;
import ru.practicum.model.NewCommentDto;
import ru.practicum.model.UpdateCommentDto;
import ru.practicum.model.UserShortDto;
import ru.practicum.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper {

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "eventId", source = "comment.eventId")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "replyOn", source = "comment.replyOn.id")
    @Mapping(target = "replies", source = "comment.replies")
    @Mapping(target = "text", source = "comment.text")
    @Mapping(target = "createdOn", source = "comment.createdOn")
    @Mapping(target = "updatedOn", source = "comment.updatedOn")
    CommentDto mapToDto(Comment comment, UserShortDto author);

    List<CommentDto> mapReplies(List<Comment> replies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "replyOn", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    Comment mapFromDto(NewCommentDto commentDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "replyOn", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", expression = "java(java.time.LocalDateTime.now())")
    Comment updateFromDto(@MappingTarget Comment comment, UpdateCommentDto commentDto);

    default Long map(Comment replyOn) {
        return replyOn != null ? replyOn.getId() : null;
    }
}
