package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonProperty

data class PaginatedRegistryData(
    @JsonProperty("registry_data_list")
    val registryDataList: List<RegistryData>? = emptyList(),
    val totalRecords: Int,
    val currentPage: Int? = 0,
    val pageSize: Int? = 0
)
