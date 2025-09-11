package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.SimilarEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventSimilarityRepository extends JpaRepository<SimilarEvent, Long> {

    Optional<SimilarEvent> findByEventAIdAndEventBId(Long EventAId, Long EventBId);

    @Query("select evsim from SimilarEvent evsim where evsim.eventAId = :id or evsim.eventBId = :id")
    List<SimilarEvent> findAllByEvent(@Param("id") Long eventId);

    @Query("SELECT evsim FROM SimilarEvent evsim WHERE evsim.eventAId IN :eventIds OR evsim.eventBId IN :eventIds")
    List<SimilarEvent> findAllByEventIn(@Param("eventIds") Collection<Long> eventIds);
}