package com.tgt.grafana_builder

import spock.lang.Specification

class GrafanaBuilderElasticSearchAppUnitTest extends Specification {

    def "build backpack-elasticsearch-app grafana dashboard"() {
        given:
        def moduleDir = System.getProperty("user.dir")

        def elasticSearchPanels = [
            new GrafanaBuilderConfig.ElasticSearchPanel(method: "searchRegistry")
        ]

        def metricsAlert = new GrafanaBuilderConfig.MetricsAlert(
            prodTapApplication: "backpackelasticsearch",
            prodTapCluster: "backpackelasticsearch",
            notificationUids: [ "FQW_lvBZk", "roC6asiMz", "7GGtGwmMz" ],
            cpuUsageThreshold: "75",
            memUsageThreshold: "75",
            server500countThreshold: 25
        )

        GrafanaBuilderConfig grafanaBuilderConfig = new GrafanaBuilderConfig(
            tapDashboardJsonFile: "${moduleDir}/src/test/unit/resources/tap-dashboard.json",
            apiServerSpecBasePath: "/registries_searches/v1",
            apiServerSpecPath: "${moduleDir}/api-specs/backpack-elasticsearch-v1.yml",
            httpClientRowTitle: "Outbound Http Clients",
            needResiliencePanel: true,
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
