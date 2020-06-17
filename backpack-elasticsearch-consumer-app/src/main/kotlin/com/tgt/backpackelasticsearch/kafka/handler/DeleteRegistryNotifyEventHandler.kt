package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.DeleteRegistryService
import com.tgt.lists.lib.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRegistryNotifyEventHandler(
    @Inject val deleteRegistryService: DeleteRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {
    private val logger = KotlinLogging.logger { DeleteRegistryNotifyEventHandler::class.java.name }

    fun handleDeleteRegistryNotifyEvent(
        deleteRegistryNotifyEvent: DeleteListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<Triple<Boolean, EventHeaders, Any>> {
        return deleteRegistryService.deleteRegistry(deleteRegistryNotifyEvent.listId)
            .map {
                if (it != null && it.id == deleteRegistryNotifyEvent.listId.toString()) {
                    Triple(true, eventHeaders, deleteRegistryNotifyEvent)
                } else {
                    val message = "Exception while updating registry data into elastic search from handleDeleteRegistryNotifyEvent: $it"
                    logger.error(message, it)
                    Triple(false, eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message), deleteRegistryNotifyEvent)
                }
            }
    }
}
