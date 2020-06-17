package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.UpdateRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistryMetaDataTO
import com.tgt.backpackelasticsearch.util.RecipientType
import com.tgt.lists.lib.kafka.model.UpdateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRegistryNotifyEventHandler(
    @Inject val updateRegistryService: UpdateRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {
    private val logger = KotlinLogging.logger { UpdateRegistryNotifyEventHandler::class.java.name }

    fun handleUpdateRegistryNotifyEvent(
        updateRegistryNotifyEvent: UpdateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<Triple<Boolean, EventHeaders, Any>> {
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
            .map {
            if (it != null && it.id == updateRegistryNotifyEvent.listId.toString()) {
                Triple(true, eventHeaders, updateRegistryNotifyEvent)
            } else {
                val message = "Exception while saving registry data into elastic search from handleCreateRegistryNotifyEvent: $it"
                logger.error(message, it)
                Triple(false, eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message), updateRegistryNotifyEvent)
            }
        }
    }
}
