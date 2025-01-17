package com.yatskevich.hs.spring.reactive.content_creation.service.impl;

import com.yatskevich.hs.spring.reactive.content_creation.dto.ContentStatusDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDto;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import com.yatskevich.hs.spring.reactive.content_creation.entity.ContentStatus;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import com.yatskevich.hs.spring.reactive.content_creation.repository.ContentRepository;
import com.yatskevich.hs.spring.reactive.content_creation.service.ContentService;
import com.yatskevich.hs.spring.reactive.content_creation.service.ContentVersionService;
import com.yatskevich.hs.spring.reactive.content_creation.service.DeltaService;
import com.yatskevich.hs.spring.reactive.content_creation.service.RevisionService;
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
public class ContentVersionServiceImpl implements ContentVersionService {

    private final ContentService contentService;
    private final ContentRepository contentRepository;
    private final RevisionService revisionService;
    private final DeltaService deltaService;

    @Override
    public Flux<RevisionDto> getAllByContentAndAuthor(UUID contentId, UUID authorId) {
        log.debug("Searching for all the revisions for the content {} of the author {} in the database.", contentId, authorId);

        return contentService.findByIdAndAuthorIdOrElseThrow(contentId, authorId)
            .flatMapMany(content -> revisionService.getAllByContentIdAndContentAuthorId(contentId, authorId)
                .flatMap(revision -> mapToRevisionDto(revision, content)));
    }

    private Mono<RevisionDto> mapToRevisionDto(Revision revision, Content content) {
        RevisionDto revisionDto = new RevisionDto();
        revisionDto.setContentId(revision.getContent().getId());
        revisionDto.setRevisionNumber(revision.getRevisionNumber());
        revisionDto.setDescription(revision.getDescription());
        revisionDto.setCreatedAt(revision.getCreatedAt());

        return Mono.zip(
            deltaService.getText2FromDelta(content.getTitle(), revision.getTitleDelta()),
            deltaService.getText2FromDelta(content.getDescription(), revision.getDescriptionDelta()),
            deltaService.getText2FromDelta(content.getBody(), revision.getBodyDelta())
        ).map(tuple -> {
            revisionDto.setContentTitle(tuple.getT1());
            revisionDto.setContentDescription(tuple.getT2());
            revisionDto.setContentBody(tuple.getT3());
            return revisionDto;
        });
    }

    @Override
    public Mono<Void> createRevision(RevisionDataDto revisionDataDto, UUID authorId) {
        UUID contentId = revisionDataDto.getContentId();

        return contentService.findByIdAndAuthorIdOrElseThrow(contentId, authorId)
            .flatMap(content -> revisionService.create(content, revisionDataDto))
            .then();
    }

    @Override
    public Mono<Void> updateStatus(ContentStatusDto contentStatusDto) {
        UUID contentId = contentStatusDto.getId();
        ContentStatus status = ContentStatus.valueOf(contentStatusDto.getStatus().toUpperCase());

        if (status.equals(ContentStatus.SUBMITTED)) {
            log.debug("Searching for the content {} in the database.", contentId);
            return Mono.fromCallable(() -> contentRepository.findById(contentId).orElseThrow(() -> {
                    log.error("The content {} is not found in the database.", contentId);
                    //FIXME create exception
                    return new RuntimeException("The content %s is not found in the database.".formatted(contentId));
                }))
                .flatMap(content -> applyLastRevisionToContent(content)
                    .then(Mono.fromRunnable(() -> content.setStatus(ContentStatus.SUBMITTED)))
                    .then(Mono.fromCallable(() -> contentRepository.save(content)).then())
                )
                .then();

        } else {
            log.debug("Changing the status of the content {} to {} in the database.", contentId, status);
            return Mono.fromCallable(() -> contentRepository.updateStatus(contentId, status)).then();
        }
    }

    private Mono<Void> applyLastRevisionToContent(Content content) {
        UUID contentId = content.getId();

        return revisionService.findLastByContentAndAuthor(contentId, content.getAuthorId())
            .flatMap(lastRevision -> {
                log.debug("Applying last revision {} for the content {}.", lastRevision.getId(), contentId);

                return Mono.zip(
                        deltaService.getText2FromDelta(content.getTitle(), lastRevision.getTitleDelta()),
                        deltaService.getText2FromDelta(content.getDescription(), lastRevision.getDescriptionDelta()),
                        deltaService.getText2FromDelta(content.getBody(), lastRevision.getBodyDelta())
                    )
                    .doOnNext(tuple -> {
                        content.setTitle(tuple.getT1());
                        content.setDescription(tuple.getT2());
                        content.setBody(tuple.getT3());
                    });
            }).then(revisionService.deleteById(contentId));
    }
}
