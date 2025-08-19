package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorIdOrderByCreatedOn(Long id, Pageable pageable);

    List<Comment> findAllByEventIdOrderByCreatedOn(Long id, Pageable pageable);
}
