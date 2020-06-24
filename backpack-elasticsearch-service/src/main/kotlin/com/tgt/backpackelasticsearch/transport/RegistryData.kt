package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.tgt.backpackregistry.util.RegistryStatus
import com.tgt.backpackregistry.util.RegistryType
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryData(
    @JsonProperty("registry_id")
    val registryId: UUID?,
    @JsonProperty("registry_title")
    val registryTitle: String?,
    @JsonProperty("registry_type")
    val registryType: RegistryType?,
    @JsonProperty("registry_status")
    val registryStatus: RegistryStatus?,
    @JsonProperty("registrant_first_name")
    val registrantFirstName: String?,
    @JsonProperty("registrant_last_name")
    val registrantLastName: String?,
    @JsonProperty("coregistrant_first_name")
    val coregistrantFirstName: String?,
    @JsonProperty("coregistrant_last_name")
    val coregistrantLastName: String?,
    @JsonProperty("event_city")
    val eventCity: String? = null,
    @JsonProperty("event_state")
    val eventState: String? = null,
    @JsonProperty("event_country")
    val eventCountry: String? = null,
    @JsonProperty("event_date_ts")
    val eventDateTs: String? = null,
    @JsonProperty("number_of_guests")
    val numberOfGuests: Int? = 0
)
