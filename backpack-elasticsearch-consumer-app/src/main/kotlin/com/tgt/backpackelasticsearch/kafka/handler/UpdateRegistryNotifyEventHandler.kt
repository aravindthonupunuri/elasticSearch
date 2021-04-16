package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.UpdateRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackregistryclient.transport.RegistryMetaDataTO
import com.tgt.backpackregistryclient.util.RecipientType
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.atlas.kafka.model.UpdateListNotifyEvent
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
        val registryMetaData = RegistryMetaDataTO.toEntityRegistryMetadata(updateRegistryNotifyEvent.userMetaData)
        return updateRegistryService.updateRegistry(
            RegistryData(registryId = updateRegistryNotifyEvent.listId,
                registryTitle = updateRegistryNotifyEvent.listTitle,
                registryType = if (updateRegistryNotifyEvent.listSubType != null)
                    RegistryType.toRegistryType(updateRegistryNotifyEvent.listSubType!!)
                else null,
                registryStatus = if (updateRegistryNotifyEvent.listState != null)
                    RegistryStatus.toRegistryStatus(updateRegistryNotifyEvent.listState.toString())
                else null,
                searchVisibility = registryMetaData?.searchVisibility,
                registrantFirstName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.REGISTRANT }?.firstName,
                registrantLastName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.REGISTRANT }?.lastName,
                coregistrantFirstName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.COREGISTRANT }?.firstName,
                coregistrantLastName = registryMetaData?.recipients?.firstOrNull { it.recipientType == RecipientType.COREGISTRANT }?.lastName,
                organizationName = registryMetaData?.organizationName,
                eventCity = registryMetaData?.event?.city,
                eventState = registryMetaData?.event?.state,
                eventCountry = registryMetaData?.event?.country,
                eventDate = registryMetaData?.event?.eventDate,
                imageUrl = registryMetaData?.imageMetaData?.profileImage?.imageUrl,
                imageId = registryMetaData?.imageMetaData?.profileImage?.imageId,
                imageDimension = registryMetaData?.imageMetaData?.profileImage?.dimension,
                imageUrlParams = registryMetaData?.imageMetaData?.profileImage?.imageUrlParams
            ))
            .map { EventProcessingResult(true, eventHeaders, updateRegistryNotifyEvent) }
            .onErrorResume {
                val message = "Exception while updating registry data into elastic search from handleUpdateRegistryNotifyEvent: $it"
                logger.error(message, it)
                Mono.just(EventProcessingResult(false,
                    eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = dlqSource),
                    updateRegistryNotifyEvent))
            }
    }
}
