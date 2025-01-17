package com.yatskevich.hs.spring.reactive.content_creation.service;

import com.yatskevich.hs.spring.reactive.content_creation.dto.ContentStatusDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDto;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContentVersionService {

    Flux<RevisionDto> getAllByContentAndAuthor(UUID contentId, UUID authorId);

    Mono<Void> createRevision(RevisionDataDto revisionDataDto, UUID authorId);

    Mono<Void> updateStatus(ContentStatusDto contentStatusDto);
}
