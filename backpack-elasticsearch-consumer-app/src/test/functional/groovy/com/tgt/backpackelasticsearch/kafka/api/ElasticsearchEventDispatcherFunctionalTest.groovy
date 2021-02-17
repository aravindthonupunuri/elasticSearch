package com.tgt.backpackelasticsearch.kafka.api

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.test.BaseKafkaFunctionalTest
import com.tgt.backpackelasticsearch.test.PreDispatchLambda
import com.tgt.backpackelasticsearch.test.util.RegistryDataProvider
import com.tgt.backpackregistryclient.transport.RegistryEventTO
import com.tgt.backpackregistryclient.transport.RegistryRecipientTO
import com.tgt.backpackregistryclient.util.*
import com.tgt.lists.atlas.api.type.LIST_STATE
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
import com.tgt.lists.atlas.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.atlas.kafka.model.UpdateListNotifyEvent
import com.tgt.lists.msgbus.ListsMessageBusProducer
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventLifecycleNotificationProvider
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MicronautTest
import io.opentracing.Tracer
import org.jetbrains.annotations.NotNull
import spock.lang.Shared
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

@MicronautTest
@Stepwise
class ElasticsearchEventDispatcherFunctionalTest extends BaseKafkaFunctionalTest {

    PollingConditions conditions = new PollingConditions(timeout: 30, delay: 1)

    @Shared
    @Inject
    Tracer tracer

    @Shared
    @Inject
    EventLifecycleNotificationProvider eventNotificationsProvider

    @Shared
    TestEventListener testEventListener

    @Inject
    ListsMessageBusProducer listsMessageBusProducer

    @Shared
    RegistryDataProvider registryDataProvider

    @Inject
    GetRegistryService getRegistryService

    @Shared
    String guestId = "1234"

    @Shared
    UUID registryId = UUID.randomUUID()

    def setupSpec() {
        testEventListener = new TestEventListener()
        testEventListener.tracer = tracer
        eventNotificationsProvider.registerListener(testEventListener)
    }

    def setup() {
        registryDataProvider = new RegistryDataProvider()
        testEventListener.reset()
    }

    def "Guest creates registry - Consumer kicks in to consume the event and copies regisrtry data into elastic search"() {
        def registryMetaData = registryDataProvider.getRegistryMetaDataMap(UUID.randomUUID(), "alternate_id",false, false, null,
            [new RegistryRecipientTO(RecipientType.REGISTRANT, RecipientRole.GROOM, "1234First", "1234Last"),
             new RegistryRecipientTO(RecipientType.COREGISTRANT,RecipientRole.BRIDE, "1234First", "1234Last")],
            new RegistryEventTO("Minneapolis", "Minnesota", "USA", LocalDate.now()), null, null, null, null, "organizationName", null, RegistrySearchVisibility.PUBLIC)

        def createRegistryEvent = new CreateListNotifyEvent(guestId, registryId, "REGISTRY", RegistryType.WEDDING.name(), "List title 1",
            RegistryChannel.WEB.name(), RegistrySubChannel.TGTWEB.name(), LIST_STATE.ACTIVE, registryMetaData, LocalDate.now(),
            null, null, null, null, null)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == CreateListNotifyEvent.getEventType()) {
                    def createRegistry = CreateListNotifyEvent.deserialize(data)
                    if (createRegistry.listId == registryId) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(createRegistryEvent.getEventType(), createRegistryEvent, registryId ).block()

        then:
        testEventListener.verifyEvents {consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        and:

        //test if registry is persisted
        when:
        def response = getRegistryService.findRegistry("1234First", "1234Last", null, null, null, null, null, null, null, null, null).block()

        then:
        !response.isEmpty()

        assert response.size() == 1
        assert response.get(0).registryId == registryId
    }

    def "Guest updates registry - Consumer kicks in to consume the event and updates registry data in elastic search"() {
        def registryMetaData = registryDataProvider.getRegistryMetaDataMap(UUID.randomUUID(), "alternate_id",false, false, null,
                [new RegistryRecipientTO(RecipientType.REGISTRANT, RecipientRole.GROOM, "1234First", "1234Last"),
                 new RegistryRecipientTO(RecipientType.COREGISTRANT,RecipientRole.BRIDE, "1234First", "1234Last")],
                new RegistryEventTO("Minneapolis", "Minnesota", "USA", LocalDate.now()), null, null,
                null, null, "organizationName", null, RegistrySearchVisibility.PUBLIC)

        def updateRegistryEvent = new UpdateListNotifyEvent(guestId, registryId, "REGISTRY", RegistryType.WEDDING.name(), "List title - updated",
                RegistryChannel.WEB.name(), RegistrySubChannel.TGTWEB.name(), LIST_STATE.ACTIVE, registryMetaData, LocalDate.now(),
                "updated description", null, null, null, null)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == UpdateListNotifyEvent.getEventType()) {
                    def updateRegistry = UpdateListNotifyEvent.deserialize(data)
                    if (updateRegistry.listId == registryId) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(updateRegistryEvent.getEventType(), updateRegistryEvent, registryId).block()

        then:
        testEventListener.verifyEvents {consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }

        and:

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        //test if registry is persisted
        when:
        def response = getRegistryService.findRegistry("1234First", "1234Last", null, null, null, null, null, null, null, null, null).block()

        then:
        !response.isEmpty()

        assert response.size() == 1
        assert response.get(0).registryId == registryId
        assert response.get(0).registryTitle == "List title - updated"
    }

    def "Guest deletes registry - Consumer kicks in to consume the event and deletes registry data from elastic search"() {
        def deleteRegistryEvent = new DeleteListNotifyEvent(guestId, registryId, "REGISTRY", "Testing Registry Event", null, null)

        testEventListener.preDispatchLambda = new PreDispatchLambda() {
            @Override
            boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent) {
                if (eventHeaders.eventType == DeleteListNotifyEvent.getEventType()) {
                    def deleteRegistry = DeleteListNotifyEvent.deserialize(data)
                    if (deleteRegistry.listId == registryId) {
                        return true
                    }
                }
                return false
            }
        }

        when:
        listsMessageBusProducer.sendMessage(deleteRegistryEvent.getEventType(), deleteRegistryEvent, registryId ).block()

        then:
        testEventListener.verifyEvents {consumerEvents, producerEvents, consumerStatusEvents ->
            conditions.eventually {
                def completedEvents = consumerEvents.stream().filter {
                    def result = (TestEventListener.Result) it
                    (!result.preDispatch && result.success)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1 && completedEvents.any {
                    (it.eventHeaders.eventType == DeleteListNotifyEvent.getEventType()) && (it.data.listId == registryId)

                }
            }
        }

        and:

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        and:

        //test if registry is deleted
        when:
        def response = getRegistryService.findRegistry("1234First", "1234Last", null, null, null, null, null, null, null, null, null).block()

        then:
        response.isEmpty()
    }
}

