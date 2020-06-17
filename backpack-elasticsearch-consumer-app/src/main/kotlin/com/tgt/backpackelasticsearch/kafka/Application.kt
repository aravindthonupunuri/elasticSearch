package com.tgt.backpackelasticsearch.kafka

import com.target.platform.connector.micronaut.PlatformPropertySource
import com.tgt.lists.common.components.tap.TAPEnvironmentLoader
import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {

        // TAP deployment specific
        TAPEnvironmentLoader().setupTAPSpecificEnvironment()

        Micronaut.build()
            .propertySources(PlatformPropertySource.connect())
            .packages("com.tgt.backpackelasticsearch.kafka")
            .mainClass(Application.javaClass)
            .start()
    }
}
