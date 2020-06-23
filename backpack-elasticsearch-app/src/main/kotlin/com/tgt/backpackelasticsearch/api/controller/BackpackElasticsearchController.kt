package com.tgt.backpackelasticsearch.api.controller

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.transport.RegistrySearchSortFieldGroup
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants.PROFILE_ID
import com.tgt.backpackregistry.util.RegistryChannel
import com.tgt.backpackregistry.util.RegistrySortOrderGroup
import com.tgt.backpackregistry.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.lib.api.util.AppErrorCodes
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
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
        @Header(PROFILE_ID) guestId: String,
        @QueryValue("first_name") firstName: String?,
        @QueryValue("last_name") lastName: String?,
        @QueryValue("channel") registryChannel: RegistryChannel?,
        @QueryValue("sub_channel") registrySubChannel: RegistrySubChannel?,
        @QueryValue("sort_field") sortFieldBy: RegistrySearchSortFieldGroup? = RegistrySearchSortFieldGroup.NAME,
        @QueryValue("sort_order") sortOrderBy: RegistrySortOrderGroup? = RegistrySortOrderGroup.ASCENDING
    ): Mono<List<RegistryData>> {
        // TODO Check if null check is either of them OR both
        if (firstName.isNullOrEmpty()) {
            throw BadRequestException(AppErrorCodes.BAD_REQUEST_ERROR_CODE(listOf("firstName is incorrect, can’t be null")))
        }
        if (lastName.isNullOrEmpty()) {
            throw BadRequestException(AppErrorCodes.BAD_REQUEST_ERROR_CODE(listOf("lastName is incorrect, can’t be null")))
        }
        if (registryChannel == null) {
            throw BadRequestException(AppErrorCodes.BAD_REQUEST_ERROR_CODE(listOf("channel is incorrect, can’t be null")))
        }
        if (registrySubChannel == null) {
            throw BadRequestException(AppErrorCodes.BAD_REQUEST_ERROR_CODE(listOf("sub_channel is incorrect, can’t be null")))
        }
        // TODO Sort field and order are yet to be implemented
        return getRegistryService.findByRecipientName(firstName, lastName)
    }
}
