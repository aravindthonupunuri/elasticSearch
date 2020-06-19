package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.UpdateRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistryMetaDataTO
import com.tgt.backpackelasticsearch.util.RecipientType
import com.tgt.lists.lib.kafka.model.UpdateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventProcessingResult
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRegistryNotifyEventHandler(
    @Inject val updateRegistryService: UpdateRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory,
    @Value("\${msgbus.dlq-source}") val dlqSource: String
) {
    private val logger = KotlinLogging.logger { UpdateRegistryNotifyEventHandler::class.java.name }

    fun handleUpdateRegistryNotifyEvent(
        updateRegistryNotifyEvent: UpdateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<EventProcessingResult> {
        val registryMetaData = RegistryMetaDataTO.getRegistryMetadata(updateRegistryNotifyEvent.userMetaData)
        return updateRegistryService.updateRegistry(RegistryData(registryId = updateRegistryNotifyEvent.listId,
            registryTitle = updateRegistryNotifyEvent.listTitle,
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
            .map { EventProcessingResult(true, eventHeaders, updateRegistryNotifyEvent) }
            .onErrorResume {
                val message = "Exception while updating registry data into elastic search from handleCreateRegistryNotifyEvent: $it"
                logger.error(message, it)
                Mono.just(EventProcessingResult(false,
                    eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = dlqSource),
                    updateRegistryNotifyEvent))
            }
    }
}
