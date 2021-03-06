package com.tgt.backpackelasticsearch.api.controller

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.transport.PaginatedRegistryData
import com.tgt.backpackelasticsearch.transport.RegistrySearchSortFieldGroup
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes
import com.tgt.lists.common.components.exception.ErrorCode
import io.micronaut.core.convert.format.Format
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.elasticsearch.search.sort.SortOrder
import reactor.core.publisher.Mono
import java.time.LocalDate
import javax.inject.Inject

@Controller(ELASTIC_SEARCH_BASEPATH)
class BackpackElasticsearchController(
    @Inject val getRegistryService: GetRegistryService
) {
    @Get("/")
    @Status(HttpStatus.OK)
    @ApiResponse(responseCode = "200", description = "Success",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = PaginatedRegistryData::class))]
    )
    fun searchRegistryByFirstSecondName(
        @QueryValue("first_name") firstName: String?,
        @QueryValue("last_name") lastName: String?,
        @QueryValue("organization_name") organizationName: String?,
        @QueryValue("channel") registryChannel: RegistryChannel,
        @QueryValue("sub_channel") registrySubChannel: RegistrySubChannel,
        @QueryValue("registry_type") registryType: RegistryType?,
        @QueryValue("state") state: String?,
        @QueryValue("min_date") @Format("yyyy-MM-dd") minDate: LocalDate?,
        @QueryValue("max_date") @Format("yyyy-MM-dd") maxDate: LocalDate?,
        @QueryValue("sort_field") sortFieldBy: RegistrySearchSortFieldGroup? = RegistrySearchSortFieldGroup.NAME,
        @QueryValue("sort_order") sortOrderBy: SortOrder? = SortOrder.ASC,
        @QueryValue("page", defaultValue = "1") page: Int? = 1,
        @QueryValue("page_size", defaultValue = "28") pageSize: Int? = 28
    ): Mono<PaginatedRegistryData> {
        if (page == 0) throw BadRequestException(ErrorCode(BaseErrorCodes.BAD_REQUEST_ERROR_CODE, listOf("page number cannot be 0")))
        return getRegistryService.findRegistry(
                recipientFirstName = firstName,
                recipientLastName = lastName,
                organizationName = organizationName,
                registryType = registryType,
                state = state,
                minimumDate = minDate,
                maximumDate = maxDate,
                sortFieldBy = sortFieldBy,
                sortOrderBy = sortOrderBy,
                page = page,
                pageSize = pageSize)
    }
}
