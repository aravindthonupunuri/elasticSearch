package com.tgt.grafana_builder

import spock.lang.Specification

class GrafanaBuilderElasticSearchConsumerUnitTest extends Specification {

    def "build backpack-elasticsearch-consumer-app grafana dashboard"() {
        given:
        def moduleDir = System.getProperty("user.dir")

        def elasticSearchPanels = [
            new GrafanaBuilderConfig.ElasticSearchPanel(method: "saveRegistry"),
            new GrafanaBuilderConfig.ElasticSearchPanel(method: "deleteRegistry"),
            new GrafanaBuilderConfig.ElasticSearchPanel(method: "updateRegistry")
        ]

        def metricsAlert = new GrafanaBuilderConfig.MetricsAlert(
            prodTapApplication: "backpackelasticsearchconsumer",
            prodTapCluster: "backpackelasticsearchconsumer",
            notificationUids: [ "FQW_lvBZk", "roC6asiMz", "7GGtGwmMz" ],
            cpuUsageThreshold: "75",
            memUsageThreshold: "75",
            server500countThreshold: 25
        )

        def kafkaConsumers = [
            new GrafanaBuilderConfig.KafkaConsumer(
                title: "Elastic Search Consumer",
                metricsName: "msgbus_consumer_event",
                isDlqConsumer: false,
                devEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-dev",
                    consumerGroup: "backpack-elasticsearch-data-bus-dev-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                stageEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-stage",
                    consumerGroup: "backpack-elasticsearch-data-bus-stage-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                prodEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-prod",
                    consumerGroup: "backpack-elasticsearch-data-bus-prod-consumer",
                    ttcCluster: "ost-ttc-prod-app",
                    tteCluster: "ost-ttce-prod-app"
                )
            ),
            new GrafanaBuilderConfig.KafkaConsumer(
                title: "Elastic Search DLQ Consumer",
                metricsName: "msgbus_consumer_event",
                isDlqConsumer: true,
                devEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-dev-dlq",
                    consumerGroup: "backpack-elasticsearch-data-bus-dev-dlq-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                stageEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-stage-dlq",
                    consumerGroup: "backpack-elasticsearch-data-bus-stage-dlq-consumer",
                    ttcCluster: "ost-ttc-test-app",
                    tteCluster: "ost-ttce-test-app"
                ),
                prodEnvironment: new GrafanaBuilderConfig.KafkaConsumerEnvironment(
                    topic: "registry-internal-data-bus-prod-dlq",
                    consumerGroup: "backpack-elasticsearch-data-bus-prod-dlq-consumer",
                    ttcCluster: "ost-ttc-prod-app",
                    tteCluster: "ost-ttce-prod-app"
                )
            )
        ]

        def kafkaProducers = [
            new GrafanaBuilderConfig.KafkaProducer(
                title: "Elastic Search msgbus DLQ Producer",
                metricsName: "msgbus_producer_event",
                isDlqProducer: true
            )
        ]

        GrafanaBuilderConfig grafanaBuilderConfig = new GrafanaBuilderConfig(
            tapDashboardJsonFile: "${moduleDir}/src/test/unit/resources/tap-dashboard.json",
            kafkaConsumers: kafkaConsumers,
            kafkaProducers: kafkaProducers,
            metricsAlert: metricsAlert,
            elasticSearchPanels: elasticSearchPanels
        )

        GrafanaBuilder grafanaBuilder = new GrafanaBuilder(grafanaBuilderConfig)

        when:
        def success = grafanaBuilder.buildPanels()

        then:
        success
    }
}
