package com.tgt.shoppinglist.api.auth

import com.tgt.lists.cart.CartClient
import com.tgt.lists.common.components.filters.auth.permissions.CartPermissionManager
import com.tgt.lists.common.components.filters.auth.permissions.DefaultListPermissionManager
import com.tgt.lists.common.components.filters.auth.permissions.ListPermissionManager
import com.tgt.listspermissions.api.client.ListPermissionsClient
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory

@Factory
class BackpackElasticsearchPermissionManagerFactory(private val cartClient: CartClient, private val permissionsClient: ListPermissionsClient) {

    val listPermissionClientManager = BackpackElasticsearchPermissionClientManager(permissionsClient)
    val cartPermissionManager = CartPermissionManager(cartClient)

    @Bean
    fun newListPermissionManager(): ListPermissionManager {
        return DefaultListPermissionManager(listPermissionClientManager, cartPermissionManager)
    }
}
