package com.yatskevich.hs.spring.reactive.content_creation.repository;

import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RevisionRepository extends ReactiveCrudRepository<Revision, UUID> {

    Flux<Revision> findAllByContentIdAndContentAuthorId(@Param("contentId") UUID contentId,
                                                        @Param("contentAuthorId") UUID authorId);

    @Query(value = """
        FROM Revision r
        LEFT JOIN r.content c
        WHERE c.id = :contentId
        AND c.authorId = :contentAuthorId
        ORDER BY r.revisionNumber DESC
        LIMIT 1
        """)
    Mono<Revision> findLastByContentIdAndContentAuthorId(@Param("contentId") UUID contentId,
                                                         @Param("contentAuthorId") UUID authorId);

    Mono<Void> deleteAllByContentId(@Param("contentId") UUID contentId);
}
