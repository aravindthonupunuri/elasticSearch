package com.tgt.backpackelasticsearch.service.async

import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ElasticClientManager
import com.tgt.lists.micronaut.elastic.ListenerArgs
import io.micronaut.context.annotation.Value
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.collect.Tuple
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Inject private val elasticClientManager: ElasticClientManager,
    @Value("\${elasticsearch.index}") private val registryIndex: String
) {

    fun deleteRegistry(registryId: UUID?): Mono<Tuple<DeleteResponse, DeleteResponse>> {
        val indexRequest = DeleteRequest(registryIndex)
            .timeout("1s")
            .id(registryId.toString())

        // Delete data from primary and backup client to ensure both are in sync
        return elasticCallExecutor.execute("deleteRegistry", elasticClientManager.primaryClient, deleteFromElasticsearch(indexRequest))
            .zipWith(elasticClientManager.backupClient?.let { elasticCallExecutor.execute("deleteRegistry-backup", it, deleteFromElasticsearch(indexRequest)) })
            .map { Tuple(it.t1, it.t2) }
    }

    private fun deleteFromElasticsearch(indexRequest: DeleteRequest?): (RestHighLevelClient, ListenerArgs<DeleteResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<DeleteResponse> ->
            client.deleteAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "deleteRegistry", "/$registryIndex/_doc"))
        }
    }
}
