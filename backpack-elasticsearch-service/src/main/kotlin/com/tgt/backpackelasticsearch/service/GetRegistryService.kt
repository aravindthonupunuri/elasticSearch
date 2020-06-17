package com.tgt.backpackelasticsearch.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import mu.KotlinLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor
) {
    private val logger = KotlinLogging.logger { GetRegistryService::class.java.name }

    val mapper = ObjectMapper()

    val ES_LIST_INDEX = "backpackregistry" // TODO Pick this from config

    fun findByRecipientName(recipientFirstName: String?, recipientLastName: String?): Mono<List<RegistryData>> {
        val searchRequest = SearchRequest(ES_LIST_INDEX)
        val searchSourceBuilder = SearchSourceBuilder()
        val fullName = recipientFirstName + recipientLastName
        val matchQueryBuilder = MultiMatchQueryBuilder(fullName, "*_name") // TODO Match against every column ending with name

        searchSourceBuilder.query(matchQueryBuilder)
            .from(0)
            .size(5) // default=10
            .timeout(TimeValue(10, TimeUnit.SECONDS))
        return elasticCallExecutor.executeWithFallback(executionId = "searchListByName", stmtBlock = saveElastic(searchRequest))
            .map {
                val searchResponse = it
                val hits = searchResponse.getHits()
                val registries = hits.getHits().map {
                    val registry = mapper.readValue<RegistryData>(it.sourceAsString)
                    registry
                }
                registries
            }
    }

    private fun saveElastic(indexRequest: SearchRequest?): (RestHighLevelClient, ListenerArgs<SearchResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<SearchResponse> ->
            client.searchAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "searchListByName", "/$ES_LIST_INDEX/_doc"))
        }
    }
}
