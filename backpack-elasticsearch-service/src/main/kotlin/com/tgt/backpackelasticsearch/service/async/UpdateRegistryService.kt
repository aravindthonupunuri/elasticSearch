package com.tgt.backpackelasticsearch.service.async

import com.fasterxml.jackson.databind.ObjectMapper
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import mu.KotlinLogging
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import reactor.core.publisher.Mono
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor
) {

    private val logger = KotlinLogging.logger { CreateRegistryService::class.java.name }

    val mapper = ObjectMapper()

    val ES_LIST_INDEX = "backpackregistry" // TODO Pick this from config

    fun updateRegistry(registryData: RegistryData): Mono<UpdateResponse> {

        val json = mapper.writeValueAsString("Registry doc that is being updated")

        val indexRequest = UpdateRequest(ES_LIST_INDEX, registryData.registryId.toString())
            .timeout("1s")
            .doc(json)

        return elasticCallExecutor.executeWithFallback(executionId = "updateRegistry",
            stmtBlock = { client: RestHighLevelClient, listenerArgs: ListenerArgs<UpdateResponse> ->
                client.updateAsync(indexRequest, RequestOptions.DEFAULT,
                    ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "updateRegistry", "/$ES_LIST_INDEX/_doc")) })
    }
}
