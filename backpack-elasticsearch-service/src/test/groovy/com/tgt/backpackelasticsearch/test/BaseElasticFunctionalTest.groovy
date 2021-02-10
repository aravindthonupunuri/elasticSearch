package com.tgt.backpackelasticsearch.test

import groovy.json.JsonOutput
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.sql.DriverManager

class BaseElasticFunctionalTest extends BaseFunctionalTest implements TestPropertyProvider {

    @Inject
    @Client("/")
    RxHttpClient client

    static final String ELASTICSEARCH_USERNAME = "elastic"  // this is default user for ES don't change to any other thing
    static final String ELASTICSEARCH_PASSWORD = "testpass"

    @Shared
    ElasticsearchContainer elasticsearchContainer

    static String elasticUrl

    /*
    These properties will override application.yml defined properties
    */
    @Override
    Map<String, String> getProperties() {

        elasticUrl = System.getenv("ELASTIC_URL")

        if(elasticUrl == null) {
            elasticsearchContainer =
                new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.6.2")
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

    static HttpResponse refresh() {
        if (elasticUrl != null) {
            try(RxHttpClient client = RxHttpClient.create(new URL(elasticUrl))) {
                client.toBlocking().exchange(HttpRequest.POST("/backpackregistry/_refresh", JsonOutput.toJson([])))
            } catch (Throwable t) {
                t.printStackTrace()
            }
        }
    }
}
