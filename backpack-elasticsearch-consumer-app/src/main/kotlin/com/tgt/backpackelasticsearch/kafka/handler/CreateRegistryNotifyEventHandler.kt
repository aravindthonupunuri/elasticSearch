package com.tgt.backpackelasticsearch.kafka.handler

import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistryMetaDataTO
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH
import com.tgt.backpackelasticsearch.util.RecipientType
import com.tgt.lists.lib.kafka.model.CreateListNotifyEvent
import com.tgt.lists.msgbus.event.EventHeaderFactory
import com.tgt.lists.msgbus.event.EventHeaders
import mu.KotlinLogging
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRegistryNotifyEventHandler(
    @Inject val createRegistryService: CreateRegistryService,
    @Inject private val eventHeaderFactory: EventHeaderFactory
) {
    private val logger = KotlinLogging.logger { CreateRegistryNotifyEventHandler::class.java.name }

    fun handleCreateRegistryNotifyEvent(
        createRegistryNotifyEvent: CreateListNotifyEvent,
        eventHeaders: EventHeaders,
        isPoisonEvent: Boolean
    ): Mono<Triple<Boolean, EventHeaders, Any>> {
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
            .map {
                if (it.v1() != null && it.v1().id == createRegistryNotifyEvent.listId.toString() &&
                    it.v2() != null && it.v2().id == createRegistryNotifyEvent.listId.toString()) {
                    Triple(true, eventHeaders, createRegistryNotifyEvent)
                } else {
                    val message = "Exception while saving registry data into elastic search from handleCreateRegistryNotifyEvent: $it"
                    logger.error(message, it)
                    Triple(false,
                        eventHeaderFactory.nextRetryHeaders(eventHeaders = eventHeaders, errorCode = 500, errorMsg = message).copy(source = ELASTIC_SEARCH_BASEPATH),
                        createRegistryNotifyEvent)
                }
            }
    }
}
