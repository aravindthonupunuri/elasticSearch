package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.tgt.backpackregistryclient.util.RegistrySearchVisibility
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistryType
import java.time.LocalDate
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
    @JsonProperty("registry_visibility")
    val searchVisibility: RegistrySearchVisibility?,
    @JsonProperty("registrant_first_name")
    val registrantFirstName: String?,
    @JsonProperty("registrant_last_name")
    val registrantLastName: String?,
    @JsonProperty("coregistrant_first_name")
    val coregistrantFirstName: String?,
    @JsonProperty("coregistrant_last_name")
    val coregistrantLastName: String?,
    @JsonProperty("organization_name")
    val organizationName: String?,
    @JsonProperty("event_city")
    val eventCity: String? = null,
    @JsonProperty("event_state")
    val eventState: String? = null,
    @JsonProperty("event_country")
    val eventCountry: String? = null,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("event_date")
    val eventDate: LocalDate? = null,
    @JsonProperty("image_url")
    val imageUrl: String? = null,
    @JsonProperty("image_id")
    val imageId: String? = null,
    @JsonProperty("image_dimension")
    val imageDimension: String? = null,
    @JsonProperty("image_url_params")
    val imageUrlParams: String? = null
)
