package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistryMetaDataTO
import com.tgt.backpackelasticsearch.util.RecipientType
import com.tgt.lists.lib.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRegistryNotifyEventHandler(
    @Inject val createRegistryService: CreateRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory,
    @Value("\${msgbus.dlq-source}") val dlqSource: String
) {
    private val logger = KotlinLogging.logger { CreateRegistryNotifyEventHandler::class.java.name }

    fun handleCreateRegistryNotifyEvent(
        createRegistryNotifyEvent: CreateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {
        val registryMetaData = RegistryMetaDataTO.getRegistryMetadata(createRegistryNotifyEvent.userMetaData)
        return createRegistryService.saveRegistry(RegistryData(registryId = createRegistryNotifyEvent.listId,
            registryTitle = createRegistryNotifyEvent.listTitle,
            registryType = registryMetaData?.registryType,
            registryStatus = "ACTIVE", // TODO pick this from registry response
            registrantFirstName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.REGISTRANT }?.firstName,
            registrantLastName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.REGISTRANT }?.lastName,
            coregistrantFirstName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.COREGISTRANT }?.firstName,
            coregistrantLastName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.COREGISTRANT }?.lastName,
            eventCity = registryMetaData?.event?.city,
            eventState = registryMetaData?.event?.state,
            eventCountry = registryMetaData?.event?.country,
            eventDateTs = registryMetaData?.event?.eventDateTs,
            numberOfGuests = registryMetaData?.event?.numberOfGuests
        ))
            .map { EventProcessingResult(true, eventHeaders, createRegistryNotifyEvent) }
            .onErrorResume {
                val message = "Exception while saving registry data into elastic search from handleCreateRegistryNotifyEvent: $it"
                logger.error(message, it)
                Mono.just(EventProcessingResult(false,
                    eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = dlqSource),
                    createRegistryNotifyEvent))
            }
    }
}
