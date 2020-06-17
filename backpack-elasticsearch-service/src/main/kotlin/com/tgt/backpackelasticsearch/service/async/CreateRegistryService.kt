package com.tgt.backpackelasticsearch.service.async

import com.fasterxml.jackson.databind.ObjectMapper
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import mu.KotlinLogging
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor
) {
    private val logger = KotlinLogging.logger { CreateRegistryService::class.java.name }

    val mapper = ObjectMapper()

    val ES_LIST_INDEX = "backpackregistry" // TODO Pick this from config

    fun saveRegistry(registryData: RegistryData): Mono<IndexResponse> {

        val json = mapper.writeValueAsString(registryData)

        val indexRequest = IndexRequest(ES_LIST_INDEX)
            .timeout("1s")
            .id(registryData.registryId.toString())
            .source(json, XContentType.JSON)

        return elasticCallExecutor.executeWithFallback(executionId = "saveRegistry", stmtBlock = saveElastic(indexRequest))
    }

    private fun saveElastic(indexRequest: IndexRequest?): (RestHighLevelClient, ListenerArgs<IndexResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<IndexResponse> ->
            client.indexAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "saveRegistry", "/$ES_LIST_INDEX/_doc"))
        }
    }
}
