package com.yatskevich.hs.spring.reactive.content_creation.repository;

import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;

public interface RevisionRepository extends JpaRepository<Revision, UUID> {

    @Async
    CompletableFuture<List<Revision>> findAllByContentIdAndContentAuthorId(@Param("contentId") UUID contentId,
                                                                           @Param("contentAuthorId") UUID authorId);

    @Async
    @Query(value = """
        FROM Revision r
        LEFT JOIN r.content c
        WHERE c.id = :contentId
        AND c.authorId = :contentAuthorId
        ORDER BY r.revisionNumber DESC
        LIMIT 1
        """)
    CompletableFuture<Revision> findLastByContentIdAndContentAuthorId(@Param("contentId") UUID contentId,
                                                                      @Param("contentAuthorId") UUID authorId);

    @Async
    CompletableFuture<Void> deleteAllByContentId(@Param("contentId") UUID contentId);
}
