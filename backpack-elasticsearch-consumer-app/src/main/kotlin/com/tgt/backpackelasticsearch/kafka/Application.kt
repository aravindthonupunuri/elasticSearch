package com.tgt.backpackelasticsearch.kafka

import com.target.platform.connector.micronaut.config.PlatformPropertySource
import com.tgt.lists.common.components.tap.TAPEnvironmentLoader
import io.micronaut.runtime.Micronaut
import java.util.*

object Application {
    @JvmStatic
    fun main(args: Array<String>) {

        // TAP deployment specific
        TAPEnvironmentLoader().setupTAPSpecificEnvironment()
        System.setProperty("APP_UUID", UUID.randomUUID().toString())
        Micronaut.build()
            .propertySources(PlatformPropertySource.connect())
            .packages("com.tgt.backpackelasticsearch.kafka")
            .mainClass(Application.javaClass)
            .start()
    }
}
