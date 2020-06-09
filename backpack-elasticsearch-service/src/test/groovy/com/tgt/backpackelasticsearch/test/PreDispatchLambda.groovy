package com.tgt.backpackelasticsearch.test

import com.tgt.lists.msgbus.event.EventHeaders
import org.jetbrains.annotations.NotNull


interface PreDispatchLambda {
    boolean onPreDispatchConsumerEvent(String topic, @NotNull EventHeaders eventHeaders, @NotNull byte[] data, boolean isPoisonEvent)
}
