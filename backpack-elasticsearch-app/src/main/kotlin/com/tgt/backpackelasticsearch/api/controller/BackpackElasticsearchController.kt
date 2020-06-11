package com.tgt.backpackelasticsearch.api.controller

import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.PROFILE_ID
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import reactor.core.publisher.Mono

@Controller(ELASTIC_SEARCH_BASEPATH)
class BackpackElasticsearchController() {

    @Get("/name/{name}")
    @Status(HttpStatus.OK)
    fun searchRegistryByFirstSecondName(
        @Header(PROFILE_ID) guestId: String,
        @PathVariable("name") name: String
    ): Mono<Void> {
        return Mono.just(true).then()
    }
}
