package com.tgt.backpackelasticsearch.service.async

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ElasticClientManager
import com.tgt.lists.micronaut.elastic.ListenerArgs
import io.micronaut.context.annotation.Value
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.collect.Tuple
import org.elasticsearch.common.xcontent.XContentType
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Inject private val elasticClientManager: ElasticClientManager,
    @Value("\${elasticsearch.index}") private val registryIndex: String,
    @Value("\${elasticsearch.operation-timeout}") private val operationTimeout: String = "1s"
) {

    val mapper = jacksonObjectMapper()

    fun saveRegistry(registryData: RegistryData): Mono<Tuple<IndexResponse, IndexResponse>> {

        val json = mapper.writeValueAsString(registryData)

        val indexRequest = IndexRequest(registryIndex)
            .timeout(operationTimeout)
            .id(registryData.registryId.toString())
            .source(json, XContentType.JSON)

        // If backup client is null then zipWith is called with null that shall make everything fail at runtime, so null check
        return if (elasticClientManager.backupClient != null)
        // Copy data into primary and backup client to ensure both are in sync
            elasticCallExecutor.execute("saveRegistry", elasticClientManager.primaryClient, saveToElasticsearch(indexRequest))
                .zipWith(elasticClientManager.backupClient?.let { elasticCallExecutor.execute("saveRegistry-backup", it, saveToElasticsearch(indexRequest)) })
                .map { Tuple(it.t1, it.t2) }
        else
        // Copy data into primary
            elasticCallExecutor.execute("saveRegistry", elasticClientManager.primaryClient, saveToElasticsearch(indexRequest))
                .map { Tuple(it, it) }
    }

    private fun saveToElasticsearch(indexRequest: IndexRequest?): (RestHighLevelClient, ListenerArgs<IndexResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<IndexResponse> ->
            client.indexAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "saveRegistry", "/$registryIndex/_doc", client))
        }
    }
}
