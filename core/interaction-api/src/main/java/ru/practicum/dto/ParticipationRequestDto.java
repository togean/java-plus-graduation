package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private String created;

    private Long event;

    private Long id;

    private Long requester;

    private EventRequestStatus status;
}
