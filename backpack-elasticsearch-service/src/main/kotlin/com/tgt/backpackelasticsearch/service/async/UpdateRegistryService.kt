package com.tgt.backpackelasticsearch.service.async

import com.fasterxml.jackson.databind.ObjectMapper
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
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Inject private val elasticClientManager: ElasticClientManager,
    @Value("\${elasticsearch.index}") private val registryIndex: String
) {

    val mapper = ObjectMapper()

    fun updateRegistry(registryData: RegistryData): Mono<Tuple<UpdateResponse, UpdateResponse>> {

        val json = mapper.writeValueAsString("Registry doc that is being updated")

        val indexRequest = UpdateRequest(registryIndex, registryData.registryId.toString())
            .timeout("1s")
            .doc(json)

        // Update data into primary and backup client to ensure both are in sync
        return elasticCallExecutor.execute("updateRegistry", elasticClientManager.primaryClient, updateToElasticsearch(indexRequest))
            .zipWith(elasticClientManager.backupClient?.let { elasticCallExecutor.execute("updateRegistry-backup", it, updateToElasticsearch(indexRequest)) })
            .map { Tuple(it.t1, it.t2) }
    }

    private fun updateToElasticsearch(indexRequest: UpdateRequest?): (RestHighLevelClient, ListenerArgs<UpdateResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<UpdateResponse> ->
            client.updateAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "updateRegistry", "/$registryIndex/_doc"))
        }
    }
}
