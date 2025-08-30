package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private Long eventId;

    private UserShortDto author;

    private Long replyOn;

    private Collection<CommentDto> replies;

    private String text;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
