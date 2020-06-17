package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryBabyTO(
    @JsonProperty("baby_name")
    val babyName: String? = null,
    @JsonProperty("baby_gender")
    val babyGender: String? = null,
    @JsonProperty("first_child")
    val firstChild: Boolean? = false,
    @JsonProperty("shower_date_ts")
    val showerDateTs: String? = null
)
