package com.yatskevich.hs.spring.reactive.content_creation.repository;

import com.yatskevich.hs.spring.reactive.content_creation.entity.Tag;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TagRepository extends ReactiveCrudRepository<Tag, UUID> {
}
