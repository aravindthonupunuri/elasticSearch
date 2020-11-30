package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.DeleteRegistryService
import com.tgt.lists.atlas.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRegistryNotifyEventHandler(
    @Inject val deleteRegistryService: DeleteRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory,
    @Value("\${msgbus.dlq-source}") val dlqSource: String
) {
    private val logger = KotlinLogging.logger { DeleteRegistryNotifyEventHandler::class.java.name }

    fun handleDeleteRegistryNotifyEvent(
        deleteRegistryNotifyEvent: DeleteListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {
        return deleteRegistryService.deleteRegistry(deleteRegistryNotifyEvent.listId)
            .map { EventProcessingResult(true, eventHeaders, deleteRegistryNotifyEvent) }
            .onErrorResume {
                val message = "Exception while deleting registry data of elastic search from handleDeleteRegistryNotifyEvent: $it"
                logger.error(message, it)
                Mono.just(EventProcessingResult(false,
                    eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = dlqSource),
                    deleteRegistryNotifyEvent))
            }
    }
}
