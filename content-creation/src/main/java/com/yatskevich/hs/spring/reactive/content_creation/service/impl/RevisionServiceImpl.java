package com.yatskevich.hs.spring.reactive.content_creation.service.impl;

import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import com.yatskevich.hs.spring.reactive.content_creation.repository.RevisionRepository;
import com.yatskevich.hs.spring.reactive.content_creation.service.DeltaService;
import com.yatskevich.hs.spring.reactive.content_creation.service.RevisionService;
import java.util.Comparator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RevisionServiceImpl implements RevisionService {

    private final RevisionRepository revisionRepository;
    private final DeltaService deltaService;

    @Override
    public Flux<Revision> getAllByContentIdAndContentAuthorId(UUID contentId, UUID authorId) {
        log.debug("Searching for all the revisions for the content {} of the author {} in the database.",
            contentId, authorId);
        return Mono.fromFuture(revisionRepository.findAllByContentIdAndContentAuthorId(contentId, authorId))
            .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> deleteById(UUID contentId) {
        log.debug("Removing all the revisions for the content {} in the database.", contentId);
        return Mono.fromFuture(revisionRepository.deleteAllByContentId(contentId));
    }

    @Override
    public Mono<Revision> findLastByContentAndAuthor(UUID contentId, UUID authorId) {
        log.debug("Searching for the last revision for the content {} in the database.", contentId);
        return Mono.fromFuture(revisionRepository.findLastByContentIdAndContentAuthorId(contentId, authorId))
            .switchIfEmpty(Mono.error(() -> {
                log.error("There are no revisions of the content {}  in the database.", contentId);
                //FIXME create exception
                return new RuntimeException("There are no revisions of the content %s  in the database."
                    .formatted(contentId));
            }));
    }

    @Override
    public Mono<Revision> create(Content content, RevisionDataDto revisionDataDto) {
        Mono<Integer> revisionNumberMono = Mono.fromFuture(
            revisionRepository.findAllByContentIdAndContentAuthorId(content.getId(), content.getAuthorId())
        ).map(revisions -> revisions.stream()
            .max(Comparator.comparingInt(Revision::getRevisionNumber))
            .map(value -> value.getRevisionNumber() + 1)
            .orElse(1));

        return revisionNumberMono.flatMap(revisionNumber ->
            Mono.zip(
                    deltaService.getDelta(content.getTitle(), revisionDataDto.getContentTitle()),
                    deltaService.getDelta(content.getDescription(), revisionDataDto.getContentDescription()),
                    deltaService.getDelta(content.getBody(), revisionDataDto.getContentBody())
                )
                .flatMap(tuple -> {
                        Revision revision = new Revision();
                        revision.setContent(content);
                        revision.setRevisionNumber(revisionNumber);
                        revision.setDescription(revisionDataDto.getDescription());
                        revision.setTitleDelta(tuple.getT1());
                        revision.setDescriptionDelta(tuple.getT2());
                        revision.setBodyDelta(tuple.getT3());

                        return Mono.fromCallable(() -> revisionRepository.save(revision));
                    }
                )
        );
    }
}
