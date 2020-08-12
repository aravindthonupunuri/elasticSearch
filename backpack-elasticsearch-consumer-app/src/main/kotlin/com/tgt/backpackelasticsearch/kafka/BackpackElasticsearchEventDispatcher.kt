package com.tgt.backpackelasticsearch.kafka

import com.tgt.backpackelasticsearch.kafka.handler.CreateRegistryNotifyEventHandler
import com.tgt.backpackelasticsearch.kafka.handler.DeleteRegistryNotifyEventHandler
import com.tgt.backpackelasticsearch.kafka.handler.UpdateRegistryNotifyEventHandler
import com.tgt.lists.lib.kafka.model.CreateListNotifyEvent
import com.tgt.lists.lib.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.lib.kafka.model.UpdateListNotifyEvent
import com.tgt.lists.msgbus.EventDispatcher
import com.tgt.lists.msgbus.event.DeadEventTransformedValue
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import com.tgt.lists.msgbus.event.EventTransformedValue
import com.tgt.lists.msgbus.execution.ExecutionSerialization
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BackpackElasticsearchEventDispatcher(
    @Inject val createRegistryNotifyEventHandler: CreateRegistryNotifyEventHandler,
    @Inject val updateRegistryNotifyEventHandler: UpdateRegistryNotifyEventHandler,
    @Inject val deleteRegistryNotifyEventHandler: DeleteRegistryNotifyEventHandler,
    @Value("\${msgbus.source}") val source: String,
    @Value("\${msgbus.dlq-source}") val dlqSource: String,
    @Value("\${kafka-sources.allow}") val allowedSources: List<String>
) : EventDispatcher {

    private val logger = KotlinLogging.logger {}

    /**
     * Transform ByteArray data to a concrete type based on event type header
     * It is also used by msgbus framework during dql publish exception handling
     */
    override fun transformValue(eventHeaders: EventHeaders, data: ByteArray): EventTransformedValue? {
        if (eventHeaders.source == source || eventHeaders.source == dlqSource || allowedSources.contains(eventHeaders.source)) {
            return when (eventHeaders.eventType) {
                CreateListNotifyEvent.getEventType() -> {
                    logger.info { "TransformValue: Got CreateList Event" }
                    val createListNotifyEvent = CreateListNotifyEvent.deserialize(data)
                    EventTransformedValue("guest_${createListNotifyEvent.guestId}", ExecutionSerialization.ID_SERIALIZATION, createListNotifyEvent)
                }
                UpdateListNotifyEvent.getEventType() -> {
                    logger.info { "TransformValue: Got UpdateList Event" }
                    val updateListNotifyEvent = UpdateListNotifyEvent.deserialize(data)
                    EventTransformedValue("lists_${updateListNotifyEvent.listId}", ExecutionSerialization.ID_SERIALIZATION, updateListNotifyEvent)
                }
                DeleteListNotifyEvent.getEventType() -> {
                    logger.info { "TransformValue: Got DeleteList Event" }
                    val deleteListNotifyEvent = DeleteListNotifyEvent.deserialize(data)
                    EventTransformedValue("guest_${deleteListNotifyEvent.guestId}", ExecutionSerialization.ID_SERIALIZATION, deleteListNotifyEvent)
                }
                else -> null
            }
        }
        return null
    }

    override fun dispatchEvent(eventHeaders: EventHeaders, data: Any, isPoisonEvent: Boolean): Mono<EventProcessingResult> {
        // Check for both the source:
        // 1. Generic messages created for the common list bus
        // 2. Failed messages and are retried as part of DLQ process
        if (eventHeaders.source == source || eventHeaders.source == dlqSource || allowedSources.contains(eventHeaders.source)) {
            // handle following events only from configured source
            when (eventHeaders.eventType) {
                CreateListNotifyEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val createListNotifyEvent = data as CreateListNotifyEvent
                    logger.info { "Got CreateList Event: $createListNotifyEvent" }
                    return createRegistryNotifyEventHandler.handleCreateRegistryNotifyEvent(createListNotifyEvent, eventHeaders, isPoisonEvent)
                }
                UpdateListNotifyEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val updateListNotifyEvent = data as UpdateListNotifyEvent
                    logger.info { "Got UpdateList Event: $updateListNotifyEvent" }
                    return updateRegistryNotifyEventHandler.handleUpdateRegistryNotifyEvent(updateListNotifyEvent, eventHeaders, isPoisonEvent)
                }
                DeleteListNotifyEvent.getEventType() -> {
                    // always use transformValue to convert raw data to concrete type
                    val deleteListNotifyEvent = data as DeleteListNotifyEvent
                    logger.info { "Got DeleteList Event: $deleteListNotifyEvent" }
                    return deleteRegistryNotifyEventHandler.handleDeleteRegistryNotifyEvent(deleteListNotifyEvent, eventHeaders, isPoisonEvent)
                }
            }
        }

        logger.debug { "Unhandled eventType: ${eventHeaders.eventType}" }
        return Mono.just(EventProcessingResult(true, eventHeaders, data))
    }

    /**
     * Handle DLQ dead events here
     * @return Triple<ExecutionId?, ExecutionSerialization, Mono<Void>>
     *                          Possible values:
     *                          null - to discard this event as we don't want to handle this dead event
     *                          OR
     *                          Triple:
     *                          =======s
     *                          ExecutionId - used only for ID_SERIALIZATION to denote a unique string identifying the processing of this event (usually some kind of business id)
     *                          ExecutionSerialization - type of serialization processing required for this event
     *                          Mono<Void> - dead event processing lambda to be run
     */
    override fun handleDlqDeadEvent(eventHeaders: EventHeaders, data: ByteArray): DeadEventTransformedValue? {
        return null
    }
}
