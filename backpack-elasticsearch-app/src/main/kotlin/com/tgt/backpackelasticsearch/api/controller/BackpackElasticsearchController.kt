package com.tgt.backpackelasticsearch.api.controller

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistrySearchSortFieldGroup
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySortOrderGroup
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.core.convert.format.Format
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
import java.time.LocalDate
import javax.inject.Inject

@Controller(ELASTIC_SEARCH_BASEPATH)
class BackpackElasticsearchController(
    @Inject val getRegistryService: GetRegistryService
) {

    @Get("/")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = RegistryData::class)))]
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
        @QueryValue("sort_order") sortOrderBy: RegistrySortOrderGroup? = RegistrySortOrderGroup.ASCENDING,
        @QueryValue("page") page: Int?,
        @QueryValue("page_size") pageSize: Int?
    ): Mono<List<RegistryData>> {
        // TODO Sort field and order are yet to be implemented
        return getRegistryService.findRegistry(firstName, lastName, organizationName, registryType, state, minDate, maxDate, page, pageSize)
    }
}
