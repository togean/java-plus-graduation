package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.model.*;
import ru.practicum.event.model.Event;
import ru.practicum.location.mapper.LocationDtoMapper;

@Mapper(componentModel = "spring", uses = {
        CategoryDtoMapper.class,
        LocationDtoMapper.class
})
public interface EventDtoMapper {

    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "annotation", source = "event.annotation")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "confirmedRequests", source = "event.confirmedRequests")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "paid", source = "event.paid")
    @Mapping(target = "participantLimit", source = "event.participantLimit")
    @Mapping(target = "requestModeration", source = "event.requestModeration")
    @Mapping(target = "state", source = "event.state")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "views", source = "event.views")
    @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", source = "event.createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", source = "event.publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss", ignore = true)
    EventFullDto mapToFullDto(Event event, UserShortDto user);

    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "annotation", source = "event.annotation")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "confirmedRequests", source = "event.confirmedRequests")
    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "paid", source = "event.paid")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "views", source = "event.views")
    @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto mapToShortDto(Event event, UserShortDto user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "confirmedRequests", expression = "java(0L)")
    @Mapping(target = "state", expression = "java(ru.practicum.dto.EventState.PENDING)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", expression = "java(0L)")
    Event mapFromDto(NewEventDto newEventDto);

    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", source = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", source = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event mapFromDto(EventFullDto eventFullDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "state", expression = "java((eventDto.getStateAction() != null && eventDto.getStateAction().equals(ru.practicum.dto.EventStateAction.SEND_TO_REVIEW)) ? ru.practicum.dto.EventState.PENDING : ru.practicum.dto.EventState.CANCELED)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateFromDto(@MappingTarget Event event, UpdateEventUserRequest eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "state", expression = "java((eventDto.getStateAction() != null && eventDto.getStateAction().equals(ru.practicum.dto.EventStateAction.PUBLISH_EVENT)) ? ru.practicum.dto.EventState.PUBLISHED : ru.practicum.dto.EventState.CANCELED)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateFromDto(@MappingTarget Event event, UpdateEventAdminRequest eventDto);
}
