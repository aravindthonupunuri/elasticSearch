package com.tgt.backpackelasticsearch.kafka.api

import com.tgt.backpackelasticsearch.test.BaseKafkaFunctionalTest
import com.tgt.backpackelasticsearch.test.PreDispatchLambda
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.atlas.api.type.LIST_STATE
import com.tgt.lists.atlas.kafka.model.CreateListNotifyEvent
import com.tgt.lists.atlas.kafka.model.DeleteListNotifyEvent
import com.tgt.lists.msgbus.ListsMessageBusProducer
import com.tgt.lists.msgbus.event.EventHeaders
import com.tgt.lists.msgbus.event.EventLifecycleNotificationProvider
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

    def setupSpec() {
        waitForKafkaReadiness()
        testEventListener = new TestEventListener()
        testEventListener.tracer = tracer
        eventNotificationsProvider.registerListener(testEventListener)
    }

    def setup() {
        testEventListener.reset()
    }

    def registryId = UUID.randomUUID()

    def "Guest creates registry - Consumer kicks in to consume the event and copies regisrtry data into elastic search"() {
        String guestId = "1236"

        def createRegistryEvent = new CreateListNotifyEvent(guestId, registryId, "REGISTRY", RegistryType.WEDDING.name(), "List title 1",
            RegistryChannel.WEB.name(), RegistrySubChannel.TGTWEB.name(), LIST_STATE.INACTIVE, null, LocalDate.now(),
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
                def completedEvents = producerEvents.stream().filter {
                    def result = (TestEventListener.Result)it
                    (!result.preDispatch)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }


    }

    def "Guest deletes registry - Consumer kicks in to consume the event and deletes registry data from elastic search"() {
        String guestId = "1236"
        def registryId = UUID.randomUUID()

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
                def completedEvents = producerEvents.stream().filter {
                    def result = (TestEventListener.Result)it
                    (!result.preDispatch)
                }.collect(Collectors.toList())
                assert completedEvents.size() == 1
            }
        }
    }
}
