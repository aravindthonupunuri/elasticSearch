package com.tgt.backpackelasticsearch.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.tgt.lists.cart.transport.CartContentsFieldGroup
import com.tgt.lists.cart.transport.CartState
import com.tgt.lists.cart.transport.CartType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DataProvider {

    static Logger LOG = LoggerFactory.getLogger(DataProvider)

    static ObjectMapper objectMapper = new ObjectMapper()

    static checkHeaders = { headers ->
        def headerOk = !headers.getAuthorization().get().isEmpty() && headers.get('X-B3-TraceId') != null &&
            headers.get('X-B3-SpanId') != null && headers.get('X-B3-ParentSpanId') != null &&
            headers.get('X-B3-Sampled') != null
        if (!headerOk) {
            LOG.error("checkHeaders failed: " +
                "authHeader: ${headers.getAuthorization().get()} X-B3-TraceId: ${headers.get('X-B3-TraceId')} X-B3-SpanId: ${headers.get('X-B3-SpanId')} X-B3-ParentSpanId: ${headers.get('X-B3-ParentSpanId')} X-B3-Sampled: ${headers.get('X-B3-Sampled')}"
            )
        }
        headerOk
    }

    static getTokenResponse() {
        return [
            "access_token" : "test-token",
            "token_type" : "Bearer",
            "expires_in" : "259200",
            "scope" : "openid"
        ]
    }

    static getHeaders(profileId, includeDebug = true) {
        def headers = ["X-Tgt-Auth-Source": "gsp", "profile_id": profileId, "x-api-id": UUID.randomUUID().toString(), "Authorization" : UUID.randomUUID().toString()]
        if (includeDebug) {
            headers.put("x-lists-debug", "true")
        }
        return headers
    }

    static getCartContentURI(cartId) {
        return "/carts/v4/cart_contents/" + cartId
    }

    static getCartURI(guestId) {
        String cartState = CartState.PENDING.name()
        String cartType = CartType.LIST.name()
        String fieldGroups =  CartContentsFieldGroup.CART.value
        return String.format('/carts/v4/?guest_id=%1$s&cart_state=%2$s&field_groups=%3$s&cart_type=%4$s', guestId, cartState, fieldGroups, cartType)
    }

    static getCartURI(guestId, cartNumber) {
        String cartState = CartState.PENDING.name()
        String cartType = CartType.LIST.name()
        String fieldGroups =  CartContentsFieldGroup.CART.value
        return String.format('/carts/v4/?guest_id=%1$s&cart_state=%2$s&cart_number=%3$s&field_groups=%4$s&cart_type=%5$s', guestId, cartState, cartNumber, fieldGroups, cartType)
    }
}
