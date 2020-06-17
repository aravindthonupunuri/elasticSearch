package com.tgt.backpackelasticsearch.service.async

import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import mu.KotlinLogging
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor
) {
    private val logger = KotlinLogging.logger { CreateRegistryService::class.java.name }

    val ES_LIST_INDEX = "backpackregistry" // TODO Pick this from config

    fun deleteRegistry(registryId: UUID?): Mono<DeleteResponse> {
        val indexRequest = DeleteRequest(ES_LIST_INDEX)
            .timeout("1s")
            .id(registryId.toString())

        return elasticCallExecutor.executeWithFallback(executionId = "deleteRegistry",
            stmtBlock = saveElastic(indexRequest))
    }

    private fun saveElastic(indexRequest: DeleteRequest?): (RestHighLevelClient, ListenerArgs<DeleteResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<DeleteResponse> ->
            client.deleteAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "deleteRegistry", "/$ES_LIST_INDEX/_doc"))
        }
    }
}
