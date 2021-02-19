package com.tgt.backpackelasticsearch.test


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.support.TestPropertyProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.elasticsearch.ElasticsearchContainer
import spock.lang.Shared

import javax.inject.Inject

class BaseElasticFunctionalTest extends BaseFunctionalTest implements TestPropertyProvider {

    static Logger LOG = LoggerFactory.getLogger(BaseElasticFunctionalTest)

    @Inject
    @Client("/")
    RxHttpClient client

    static final String ELASTICSEARCH_USERNAME = "elastic"  // this is default user for ES don't change to any other thing
    static final String ELASTICSEARCH_PASSWORD = "testpass"

    private static long elasticSearchCheckRetryMillis = 200
    private static long maxElasticSearchCheckCount = 300

    def setupSpec() {
        createIndex()
    }

    def cleanupSpec() {
        truncate()
    }

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

    static createIndex() {
        waitForElasticSearchReadiness()
        LOG.info("creating elastic search index")
        if (elasticUrl != null) {
            try(RxHttpClient client = RxHttpClient.create(new URL(elasticUrl))) {
                def moduleDir = System.getProperty("user.dir")
                String fileContents = new File("${moduleDir}/../backpack-elasticsearch-service/src/main/resources/db/index/index.json").text
                def jsonSlurper = new JsonSlurper()
                client.toBlocking().exchange(HttpRequest.PUT("/backpackregistry", JsonOutput.toJson(jsonSlurper.parseText(fileContents))).contentType(MediaType.APPLICATION_JSON))
                LOG.info("completed creating elastic search index")
            } catch (Throwable t) {
                t.printStackTrace()
            }
        }
    }

    static void waitForElasticSearchReadiness() {
        boolean elasticSearchReady = false
        LOG.info("Elastic search readiness check")
        int checkout = 0
        while (!elasticSearchReady && checkout < maxElasticSearchCheckCount) {
            LOG.info("Checking Elastic search readiness [$checkout]")
            checkout++
            try(RxHttpClient client = RxHttpClient.create(new URL(elasticUrl))) {
                HttpResponse response = client.toBlocking().exchange(HttpRequest.GET("/_cluster/health?wait_for_status=green"))
                if(response.status() == HttpStatus.OK){
                    elasticSearchReady = true
                }
            } catch (HttpClientResponseException ex) {
                LOG.error("Elastic search not ready", ex.message)
            }
            if (!elasticSearchReady) {
                LOG.info("Elastic search not ready yet, retry after ${elasticSearchCheckRetryMillis}ms")
                sleep(elasticSearchCheckRetryMillis)
            }
        }
        LOG.info("Elastic search is ready")
    }

    static HttpResponse refresh() {
        LOG.info("starting elastic search refresh")
        if (elasticUrl != null) {
            try(RxHttpClient client = RxHttpClient.create(new URL(elasticUrl))) {
                client.toBlocking().exchange(HttpRequest.POST("/backpackregistry/_refresh", JsonOutput.toJson([])))
            } catch (Throwable t) {
                t.printStackTrace()
            }
        }
    }

    static void truncate() {
        LOG.info("starting elastic search delete index")
        if (elasticUrl != null) {
            try(RxHttpClient client = RxHttpClient.create(new URL(elasticUrl))) {
                client.toBlocking().exchange(HttpRequest.DELETE("/backpackregistry"))
                LOG.info("completed elastic search delete index")
            } catch (Throwable t) {
                t.printStackTrace()
            }
        }
    }
}
