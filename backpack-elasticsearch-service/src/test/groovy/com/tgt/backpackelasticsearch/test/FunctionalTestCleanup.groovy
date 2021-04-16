package com.tgt.backpackelasticsearch.test

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.AbstractGlobalExtension

/*
Spock AbstractGlobalExtension mechanism allows to execute cleanup code right after spock executes all the specs.

This capability provides a Suite functionality for local testing where heavy resources, e.g. Kafka container, can be created as static variable within BaseFunctionalTest
and started there. Once all spock tests are completed, this hook will be called to cleanup (stop) those resources.

You need to provide a special file under src/test/resources/META-INF/services/org.spockframework.runtime.extension.IGlobalExtension that references this extension.
That file should contain a single line pointing to this extension as com.tgt.registriesexternalpartners.test.FunctionalTestCleanup
 */
class FunctionalTestCleanup extends AbstractGlobalExtension {
    static Logger LOG = LoggerFactory.getLogger(FunctionalTestCleanup)

    @Override
    void stop() {
        LOG.info("Running FunctionalTestCleanup.stop")
        if (BaseKafkaFunctionalTest.kafkaContainer != null) {
            LOG.info("Stopping kafkaContainer...")
            BaseKafkaFunctionalTest.kafkaContainer.stop()
        }
        if (BaseElasticFunctionalTest.elasticsearchContainer != null) {
            LOG.info("Stopping elasticsearchContainer...")
            BaseElasticFunctionalTest.elasticsearchContainer.stop()
        }
    }
}

