package com.tgt.backpackelasticsearch.service.async

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ElasticClientManager
import com.tgt.lists.micronaut.elastic.ListenerArgs
import io.micronaut.context.annotation.Value
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.collect.Tuple
import org.elasticsearch.common.xcontent.XContentType
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Inject private val elasticClientManager: ElasticClientManager,
    @Value("\${elasticsearch.index}") private val registryIndex: String,
    @Value("\${elasticsearch.operation-timeout}") private val operationTimeout: String = "1s"
) {

    val mapper = jacksonObjectMapper()
    val retryOnConflict = 5

    fun updateRegistry(registryData: RegistryData): Mono<Tuple<UpdateResponse, UpdateResponse>> {

        val json = mapper.writeValueAsString(registryData)

        val indexRequest = UpdateRequest(registryIndex, registryData.registryId.toString())
                .retryOnConflict(retryOnConflict)
                .timeout(operationTimeout)
                .doc(json, XContentType.JSON)

        // If backup client is null then zipWith is called with null that shall make everything fail at runtime, so null check
        return if (elasticClientManager.backupClient != null)
        // Update data into primary and backup client to ensure both are in sync
            elasticCallExecutor.execute("updateRegistry", elasticClientManager.primaryClient, updateToElasticsearch(indexRequest))
                .zipWith(elasticClientManager.backupClient?.let { elasticCallExecutor.execute("updateRegistry-backup", it, updateToElasticsearch(indexRequest)) })
                .map { Tuple(it.t1, it.t2) }
        else
        // Update data into primary
            elasticCallExecutor.execute("updateRegistry", elasticClientManager.primaryClient, updateToElasticsearch(indexRequest))
                .map { Tuple(it, it) }
    }

    private fun updateToElasticsearch(indexRequest: UpdateRequest?): (RestHighLevelClient, ListenerArgs<UpdateResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<UpdateResponse> ->
            client.updateAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "updateRegistry", "/$registryIndex/_doc"))
        }
    }
}
