package com.tgt.backpackelasticsearch.test

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
        def headers = ["X-Tgt-Auth-Source": "gsp", "profile_id": profileId, "x-api-id": UUID.randomUUID().toString()]
        if (includeDebug) {
            headers.put("x-lists-debug", "true")
        }
        return headers
    }
}
