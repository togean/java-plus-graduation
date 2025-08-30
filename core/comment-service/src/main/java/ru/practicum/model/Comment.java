package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "event_id")
    private Long eventId;

    @JoinColumn(name = "author_id")
    private Long authorId;

    @ManyToOne
    @JoinColumn(name = "reply_on_id")
    private Comment replyOn;

    @OneToMany(mappedBy = "replyOn")
    private List<Comment> replies;

    @Column(name = "text", length = 2000)
    private String text;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime updatedOn;
}
