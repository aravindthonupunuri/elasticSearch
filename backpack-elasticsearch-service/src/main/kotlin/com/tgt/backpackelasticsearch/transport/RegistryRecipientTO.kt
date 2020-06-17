package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.tgt.backpackelasticsearch.util.RecipientType

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryRecipientTO(
    @JsonProperty("recipient_type")
    val recipientType: RecipientType,
    @JsonProperty("recipient_role")
    val recipientRole: String? = null, // TODO: Have enum values instead?
    @JsonProperty("first_name")
    val firstName: String? = null,
    @JsonProperty("middle_name")
    val middleName: String? = null,
    @JsonProperty("last_name")
    val lastName: String? = null
)
