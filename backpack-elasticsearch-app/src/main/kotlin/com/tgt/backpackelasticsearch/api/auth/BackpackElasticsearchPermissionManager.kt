package com.tgt.backpackelasticsearch.api.auth

import com.tgt.lists.cart.CartClient
import com.tgt.lists.common.components.filters.auth.DefaultListPermissionManager
import com.tgt.lists.common.components.filters.auth.ListPermissionManager
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Singleton

@Singleton
class BackpackElasticsearchPermissionManager() : ListPermissionManager {

    val defaultListPermissionManager: ListPermissionManager
    init {
        defaultListPermissionManager = object: ListPermissionManager {
            override fun authorize(userId: String, listId: UUID): Mono<Boolean> {
                return Mono.just(true)
            }
        }
    }

    override fun authorize(userId: String, listId: UUID): Mono<Boolean> {
        return defaultListPermissionManager.authorize(userId, listId)
    }
}
