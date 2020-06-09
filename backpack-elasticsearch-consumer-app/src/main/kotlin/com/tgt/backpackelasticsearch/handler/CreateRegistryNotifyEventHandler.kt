package com.tgt.backpackregistry.kafka.handler

import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.lists.lib.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaders
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRegistryNotifyEventHandler(
    @Inject val createRegistryService: CreateRegistryService
) {

    fun handleCreateRegistryNotifyEvent(
        createRegistryNotifyEvent: CreateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<Triple<Boolean, EventHeaders, Any>> {
        return createRegistryService.saveRegistry(createRegistryNotifyEvent, eventHeaders).map { it }
    }
}
