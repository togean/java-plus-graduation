package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import ru.practicum.category.model.Category;
import ru.practicum.dtomodels.EventState;
import ru.practicum.location.model.Location;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "annotation", length = 2000)
    private String annotation;

    @Column(name = "description", length = 7000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "paid")
    private Boolean paid;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column(name = "participant_limit")
    private Long participantLimit;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @Column(name = "state", length = 50)
    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Transient
    private Long views;
}
