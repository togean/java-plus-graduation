package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {

    private Collection<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

    private Collection<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}
