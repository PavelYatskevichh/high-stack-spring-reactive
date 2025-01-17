package com.yatskevich.hs.spring.reactive.content_creation.service;

import com.yatskevich.hs.spring.reactive.content_creation.dto.ContentDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.ContentDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.ContentTagsDto;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContentService {

    Flux<ContentDto> getAll();

    Mono<ContentDto> getById(UUID contentId);

    Mono<Content> findByIdAndAuthorIdOrElseThrow(UUID contentId, UUID authorId);

    Mono<Void> create(ContentDataDto contentDataDto, UUID authorId);

    Mono<Void> addTags(ContentTagsDto contentTagsDto, UUID authorId);

    Mono<Void> deleteTags(ContentTagsDto contentTagsDto, UUID authorId);
}
