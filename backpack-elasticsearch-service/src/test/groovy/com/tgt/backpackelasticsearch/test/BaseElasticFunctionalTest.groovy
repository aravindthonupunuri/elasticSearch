package com.tgt.backpackelasticsearch.test

import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

class BaseElasticFunctionalTest extends Specification implements TestPropertyProvider {

    @Inject
    @Client("/")
    RxHttpClient client

    static final String ELASTICSEARCH_USERNAME = "elastic"  // this is default user for ES don't change to any other thing
    static final String ELASTICSEARCH_PASSWORD = "testpass"

    @Shared
    ElasticsearchContainer elasticsearchContainer

    /*
    These properties will override application.yml defined properties
    */
    @Override
    Map<String, String> getProperties() {

        String elasticUrl = System.getenv("ELASTIC_URL")

        if(elasticUrl == null) {
            elasticsearchContainer =
                new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.6.2")
                    .withEnv("ELASTIC_PASSWORD", ELASTICSEARCH_PASSWORD)
                    .withEnv("xpack.security.enabled", "true")
            elasticsearchContainer.start()
            def mappedPort = elasticsearchContainer.getMappedPort(9200)
            elasticUrl = "http://localhost:${mappedPort}"
        }

        Map<String, String> properties = [
            "elasticsearch.httpHosts" : elasticUrl,
            "esaas.primary.httpHosts" : elasticUrl,
            "esaas.backup.httpHosts" : elasticUrl,
            "esaas.nuidUser" : ELASTICSEARCH_USERNAME,
            "esaas.nuidPassword" : ELASTICSEARCH_PASSWORD,
            "esaas.client-switch.failure-window-secs": 2,
            "esaas.client-switch.failure-count-threshold": 10,
            "esaas.client-switch.max-backup-mode-secs": 1
        ]

        return properties
    }
}
