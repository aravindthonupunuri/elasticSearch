package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.DeleteRegistryService
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.lists.lib.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
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
    ): Mono<EventProcessingResult> {
        return deleteRegistryService.deleteRegistry(deleteRegistryNotifyEvent.listId)
            .map {
                if (it.v1() != null && it.v1().id == deleteRegistryNotifyEvent.listId.toString() &&
                    it.v2() != null && it.v2().id == deleteRegistryNotifyEvent.listId.toString()) {
                    EventProcessingResult(true, eventHeaders, deleteRegistryNotifyEvent)
                } else {
                    val message = "Exception while deleting registry data of elastic search from handleDeleteRegistryNotifyEvent: $it"
                    logger.error(message, it)
                    EventProcessingResult(false,
                        eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH),
                        deleteRegistryNotifyEvent)
                }
            }
    }
}
