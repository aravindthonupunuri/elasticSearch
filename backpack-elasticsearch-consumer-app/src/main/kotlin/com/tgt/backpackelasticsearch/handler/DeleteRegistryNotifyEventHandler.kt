package com.tgt.backpackregistry.kafka.handler

import com.tgt.backpackelasticsearch.service.async.DeleteRegistryService
import com.tgt.lists.lib.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaders
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRegistryNotifyEventHandler(
    @Inject val deleteRegistryService: DeleteRegistryService
) {

    fun handleDeleteRegistryNotifyEvent(
        deleteRegistryNotifyEvent: DeleteListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<Triple<Boolean, EventHeaders, Any>> {
        return deleteRegistryService.deleteRegistry(deleteRegistryNotifyEvent, eventHeaders).map { it }
    }
}
