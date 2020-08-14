package com.tgt.backpackelasticsearch.api.auth

import com.tgt.lists.common.components.exception.BaseErrorCodes.FORBIDDEN_ERROR_CODE
import com.tgt.lists.common.components.exception.ForbiddenException
import com.tgt.lists.common.components.filters.auth.permissions.ListPermissionManager
import com.tgt.listspermissions.api.client.ListPermissionsClient
import com.tgt.listspermissions.domain.model.PermissionType
import io.micronaut.http.HttpMethod
import reactor.core.publisher.Mono
import java.util.*

class BackpackElasticsearchPermissionClientManager(private val permissionsClient: ListPermissionsClient) : ListPermissionManager {

    override fun authorize(userId: String, listId: UUID, requestMethod: HttpMethod): Mono<Boolean> {
        val permissionType: PermissionType = when (requestMethod) {
            HttpMethod.GET -> PermissionType.READ
            HttpMethod.POST -> PermissionType.CREATE
            HttpMethod.PUT -> PermissionType.UPDATE
            HttpMethod.DELETE -> PermissionType.DELETE
            else -> throw ForbiddenException(FORBIDDEN_ERROR_CODE(listOf("User is not allowed to access Registry $listId")))
        }
        return permissionsClient.checkResourceSubResourcePermission(resourceId = listId, memberId = userId, permission = permissionType).map { true }
    }
}
