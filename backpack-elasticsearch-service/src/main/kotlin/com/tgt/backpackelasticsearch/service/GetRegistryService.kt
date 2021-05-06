package com.tgt.backpackelasticsearch.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackelasticsearch.transport.PaginatedRegistryData
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistrySearchSortFieldGroup
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes
import com.tgt.lists.common.components.exception.ErrorCode
import com.tgt.lists.micronaut.elastic.ElasticCallExecutor
import com.tgt.lists.micronaut.elastic.ListenerArgs
import io.micronaut.context.annotation.Value
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.*
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRegistryService(
    @Inject private val elasticCallExecutor: ElasticCallExecutor,
    @Value("\${elasticsearch.index}") private val registryIndex: String
) {

    val mapper = jacksonObjectMapper()

    companion object {
        const val DEFAULT_PAGE_SIZE = 100
    }

    fun findRegistry(
        recipientFirstName: String?,
        recipientLastName: String?,
        organizationName: String?,
        registryType: RegistryType?,
        state: String?,
        minimumDate: LocalDate?,
        maximumDate: LocalDate?,
        sortFieldBy: RegistrySearchSortFieldGroup?,
        sortOrderBy: SortOrder?,
        page: Int?,
        pageSize: Int?
    ): Mono<PaginatedRegistryData> {
        if (organizationName.isNullOrEmpty() && (recipientFirstName.isNullOrEmpty() || recipientLastName.isNullOrEmpty())) {
            throw BadRequestException(ErrorCode(BaseErrorCodes.BAD_REQUEST_ERROR_CODE, listOf("Missing required field first name and last name or organization")))
        }
        val searchRequest = SearchRequest(registryIndex)
        val fullName = "$recipientFirstName $recipientLastName"

        val identifier =
            organizationName
            // Match first and last name in both registrant as well co-registrants first, last names
            // Do note its AND condition, so both first as well last has to be in either of 4 names
            ?: fullName

        val query = "$identifier AND (registry_status:ACTIVE) AND (registry_visibility:PUBLIC)"

        val registryQuery = if (registryType != null) "$query AND (registry_type:$registryType)" else query

        val stateQuery = if (state != null) "$registryQuery AND (event_state:\"$state\")" else registryQuery

        val queryStringQueryBuilder = QueryStringQueryBuilder(stateQuery).defaultField("*")

        val rangeQueryBuilder = RangeQueryBuilder("event_date").from(minimumDate).to(maximumDate)

        val boolQueryBuilder = BoolQueryBuilder().must(queryStringQueryBuilder).must(rangeQueryBuilder)

        val finalPageSize = pageSize ?: DEFAULT_PAGE_SIZE
        val from = if (page == null || page == 0) {
            0
        } else {
            if (page * finalPageSize > DEFAULT_PAGE_SIZE) {
                DEFAULT_PAGE_SIZE
            } else {
                page * finalPageSize
            }
        }

        val searchSourceBuilder = SearchSourceBuilder().query(boolQueryBuilder)
            .timeout(TimeValue(10, TimeUnit.SECONDS))
            .from(from)
            .size(finalPageSize)

        if (sortFieldBy != null) {
            searchSourceBuilder
                .sort(SortBuilders.fieldSort(sortFieldBy.value + ".keyword")
                    .order(sortOrderBy ?: SortOrder.ASC))
        }

        searchRequest.source(searchSourceBuilder)
        searchRequest.preference("_local")

        return elasticCallExecutor.executeWithFallback(executionId = "searchRegistry", stmtBlock = queryElastic(searchRequest))
            .map {
                val searchResponse = it
                val hits = searchResponse.getHits()
                val registries = hits.getHits().map {
                    val registry = mapper.readValue<RegistryData>(it.sourceAsString)
                    registry
                }

                PaginatedRegistryData(
                    registryDataList = registries,
                    totalRecords = hits.totalHits?.value,
                    currentPage = page,
                    pageSize = pageSize
                )
            }
    }

    private fun queryElastic(indexRequest: SearchRequest?): (RestHighLevelClient, ListenerArgs<SearchResponse>) -> Unit {
        return { client: RestHighLevelClient, listenerArgs: ListenerArgs<SearchResponse> ->
            client.searchAsync(indexRequest, RequestOptions.DEFAULT,
                ElasticCallExecutor.listenerToSink(elasticCallExecutor, listenerArgs, "searchRegistry", "/$registryIndex/_doc", client))
        }
    }
}
