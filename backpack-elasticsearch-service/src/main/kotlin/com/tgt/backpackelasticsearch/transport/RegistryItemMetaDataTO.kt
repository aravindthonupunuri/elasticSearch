package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackelasticsearch.util.RegistrySubChannel

data class RegistryItemMetaDataTO(
    @JsonProperty("sub_channel")
    val subChannel: RegistrySubChannel? = null,
    @JsonProperty("generic_item_name")
    val genericItemName: String? = null,
    @JsonProperty("external_product_url")
    val externalProductUrl: String? = null,
    @JsonProperty("external_retailer")
    val externalRetailer: String? = null,
    @JsonProperty("external_product_price")
    val externalProductPrice: String? = null,
    @JsonProperty("added_by_recipient")
    val addedByRecipient: Boolean? = true,
    @JsonProperty("purchased_quantity")
    val purchasedQuantity: Int? = 0,
    @JsonProperty("agent_id")
    val agentId: String? = null
) {
    companion object {
        const val REGISTRY_ITEM_METADATA = "registry-item-metadata"
        val mapper = ObjectMapper()

        @JvmStatic
        fun getRegistryItemMetaData(metaData: Map<String, Any>?): RegistryItemMetaDataTO? {
            return metaData?.takeIf { metaData.containsKey(REGISTRY_ITEM_METADATA) }
                ?.let {
                    mapper.readValue<RegistryItemMetaDataTO>(
                        mapper.writeValueAsString(metaData[REGISTRY_ITEM_METADATA]))
                }
        }

        @JvmStatic
        fun getRegistryItemMetaDataMap(
            subChannel: RegistrySubChannel?,
            genericItemName: String?,
            externalProductUrl: String?,
            externalRetailer: String?,
            externalProductPrice: String?,
            addedByRecipient: Boolean?,
            purchasedQuantity: Int?,
            agentId: String?
        ): Map<String, Any>? {
            return mapOf(REGISTRY_ITEM_METADATA to
                RegistryItemMetaDataTO(subChannel = subChannel, genericItemName = genericItemName, externalProductUrl = externalProductUrl,
                    externalRetailer = externalRetailer, externalProductPrice = externalProductPrice, addedByRecipient = addedByRecipient,
                    purchasedQuantity = purchasedQuantity, agentId = agentId))
        }
    }
}
