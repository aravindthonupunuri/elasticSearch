package com.tgt.backpackelasticsearch.transport

import com.tgt.backpackelasticsearch.util.RegistryStatus
import com.tgt.backpackelasticsearch.util.RegistryType
import java.util.*

data class RegistryData(
    val registryId: UUID?,
    val registryTitle: String?,
    val registryType: RegistryType?,
    val registryStatus: RegistryStatus?,
    val registrantFirstName: String?,
    val registrantLastName: String?,
    val coregistrantFirstName: String?,
    val coregistrantLastName: String?,
    val eventCity: String? = null,
    val eventState: String? = null,
    val eventCountry: String? = null,
    val eventDateTs: String? = null,
    val numberOfGuests: Int? = 0
)
