package com.tgt.backpackelasticsearch.test

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DataProvider {

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
}
