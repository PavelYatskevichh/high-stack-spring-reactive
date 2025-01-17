package com.yatskevich.hs.spring.reactive.content_creation.service;

import reactor.core.publisher.Mono;

public interface DeltaService {

    Mono<String> getText2FromDelta(String text1, String delta);

    Mono<String> getDelta(String text1, String text2);
}
