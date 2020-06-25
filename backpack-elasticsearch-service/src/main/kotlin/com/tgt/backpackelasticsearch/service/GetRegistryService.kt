package com.tgt.backpackelasticsearch.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import io.micronaut.context.annotation.Value
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.Operator
import org.elasticsearch.search.builder.SearchSourceBuilder
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Value("\${elasticsearch.index}") private val registryIndex: String
) {

    val mapper = ObjectMapper()

    fun findByRecipientName(recipientFirstName: String?, recipientLastName: String?): Mono<List<RegistryData>> {
        val searchRequest = SearchRequest(registryIndex)
        val searchSourceBuilder = SearchSourceBuilder()
        val fullName = "$recipientFirstName $recipientLastName"
        // Match first and last name in both registrant as well co-regisrants first, last names
        // Do note its AND condition, so both first as well last has to be in either of 4 names
        val matchQueryBuilder = MultiMatchQueryBuilder(
            fullName,
            "registrantFirstName", "registrantLastName", "coregistrantFirstName", "coregistrantLastName"
        )
            .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS).operator(Operator.AND)

        searchSourceBuilder.query(matchQueryBuilder)
            .from(0)
            .size(5) // default=10
            .timeout(TimeValue(10, TimeUnit.SECONDS))
        searchRequest.source(searchSourceBuilder)
        searchRequest.preference("_local")
        return elasticCallExecutor.executeWithFallback(executionId = "searchListByName", stmtBlock = queryElastic(searchRequest))
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

    private fun queryElastic(indexRequest: SearchRequest?): (RestHighLevelClient, ListenerArgs<SearchResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<SearchResponse> ->
            client.searchAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "searchListByName", "/$registryIndex/_doc"))
        }
    }
}
