package com.yatskevich.hs.spring.reactive.content_creation.service;

import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RevisionService {

    Flux<Revision> getAllByContentIdAndContentAuthorId(UUID contentId, UUID authorId);

    Mono<Void> deleteById(UUID contentId);

    Mono<Revision> findLastByContentAndAuthor(UUID contentId, UUID authorId);

    Mono<Revision> create(Content content, RevisionDataDto revisionDataDto);
}
