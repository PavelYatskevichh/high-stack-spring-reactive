package com.yatskevich.hs.spring.reactive.content_creation.service.impl;

import com.yatskevich.hs.spring.reactive.content_creation.dto.RevisionDataDto;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Content;
import com.yatskevich.hs.spring.reactive.content_creation.entity.Revision;
import com.yatskevich.hs.spring.reactive.content_creation.repository.RevisionRepository;
import com.yatskevich.hs.spring.reactive.content_creation.service.DeltaService;
import com.yatskevich.hs.spring.reactive.content_creation.service.RevisionService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RevisionServiceImpl implements RevisionService {

    private final RevisionRepository revisionRepository;
    private final DeltaService deltaService;

    @Override
    public List<Revision> getAllByContentIdAndContentAuthorId(UUID contentId, UUID authorId) {
        log.debug("Searching for all the revisions for the content {} of the author {} in the database.",
            contentId, authorId);
        return revisionRepository.findAllByContentIdAndContentAuthorId(contentId, authorId);
    }

    @Override
    public void deleteById(UUID contentId) {
        log.debug("Removing all the revisions for the content {} in the database.", contentId);
        revisionRepository.deleteAllByContentId(contentId);
    }

    @Override
    public Revision findLastByContentAndAuthor(UUID contentId, UUID authorId) {
        log.debug("Searching for the last revision for the content {} in the database.", contentId);
        return revisionRepository.findLastByContentIdAndContentAuthorId(contentId, authorId)
            .orElseThrow(() -> {
                log.error("There are no revisions of the content {}  in the database.", contentId);
                //FIXME create exception
                return new RuntimeException("There are no revisions of the content %s  in the database."
                    .formatted(contentId));
            });
    }

    @Override
    public void create(Content content, RevisionDataDto revisionDataDto) {
        Optional<Revision> revisionOptional = revisionRepository
            .findAllByContentIdAndContentAuthorId(content.getId(), content.getAuthorId()).stream()
            .max(Comparator.comparingInt(Revision::getRevisionNumber));

        Integer revisionNumber = revisionOptional.map(value -> value.getRevisionNumber() + 1).orElse(1);

        Revision revision = new Revision();
        revision.setContent(content);
        revision.setRevisionNumber(revisionNumber);
        revision.setDescription(revisionDataDto.getDescription());
        revision.setTitleDelta(deltaService.getDelta(content.getTitle(), revisionDataDto.getContentTitle()));
        revision.setDescriptionDelta(deltaService.getDelta(content.getDescription(), revisionDataDto.getContentDescription()));
        revision.setBodyDelta(deltaService.getDelta(content.getBody(), revisionDataDto.getContentBody()));

        revisionRepository.save(revision);
    }
}
