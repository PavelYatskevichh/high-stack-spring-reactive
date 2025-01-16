package com.yatskevich.hs.spring.reactive.content_creation.repository;

import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import com.yatskevich.hs.spring.reactive.content_creation.entity.ContentStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContentRepository extends ReactiveCrudRepository<Content, UUID> {

    Flux<Content> findAllByAuthorId(@Param("authorId") UUID authorId);

    Mono<Content> findByIdAndAuthorId(@Param("id") UUID contentId,
                                      @Param("userId") UUID authorId);

    @Query(value = """
        UPDATE Content
        SET status = :status
        WHERE id = :id
        """)
    @Modifying
    Mono<Void> updateStatus(@Param("id") UUID id,
                            @Param("status") ContentStatus status);
}
