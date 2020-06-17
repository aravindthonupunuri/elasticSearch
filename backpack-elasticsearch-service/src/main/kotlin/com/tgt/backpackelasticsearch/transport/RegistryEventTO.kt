package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryEventTO(
    @JsonProperty("city")
    val city: String? = null,
    @JsonProperty("state")
    val state: String? = null,
    @JsonProperty("country")
    val country: String? = null,
    @JsonProperty("event_date_ts")
    val eventDateTs: String? = null,
    @JsonProperty("number_of_guests")
    val numberOfGuests: Int? = 0
)
