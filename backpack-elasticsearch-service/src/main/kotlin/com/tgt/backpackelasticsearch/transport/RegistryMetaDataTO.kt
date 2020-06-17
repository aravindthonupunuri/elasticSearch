package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackelasticsearch.util.RegistrySubChannel
import com.tgt.backpackelasticsearch.util.RegistryType

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryMetaDataTO(
    @JsonProperty("sub_channel")
    val subChannel: RegistrySubChannel? = null,
    @JsonProperty("profile_address_id")
    val profileAddressId: String? = null,
    @JsonProperty("registry_type")
    val registryType: RegistryType? = null,
    @JsonProperty("gift_cards_enabled")
    val giftCardsEnabled: Boolean? = false,
    @JsonProperty("group_gift_enabled")
    val groupGiftEnabled: Boolean? = false,
    @JsonProperty("group_gift_amount")
    val groupGiftAmount: String? = null,
    @JsonProperty("recipient_list")
    val recipients: List<RegistryRecipientTO>? = null,
    @JsonProperty("event")
    val event: RegistryEventTO? = null,
    @JsonProperty("baby_registry")
    val babyRegistry: RegistryBabyTO? = null,
    @JsonProperty("guest_rules_metadata")
    val guestRulesMetaData: RegistryGuestRuleMetaDataTO? = null
) {
    companion object {
        const val REGISTRY_METADATA = "registry-metadata"
        val mapper = ObjectMapper()

        @JvmStatic
        fun getRegistryMetadata(metadata: Map<String, Any>?): RegistryMetaDataTO? {
            return metadata?.takeIf { metadata.containsKey(REGISTRY_METADATA) }
                ?.let {
                    mapper.readValue<RegistryMetaDataTO>(
                        mapper.writeValueAsString(metadata[REGISTRY_METADATA]))
                }
        }

        @JvmStatic
        fun getRegistryMetadataMap(registryMetaData: RegistryMetaDataTO?): Map<String, Any>? {
            return mapOf(REGISTRY_METADATA to
                RegistryMetaDataTO(registryMetaData?.subChannel, registryMetaData?.profileAddressId, registryMetaData?.registryType,
                    registryMetaData?.giftCardsEnabled, registryMetaData?.groupGiftEnabled, registryMetaData?.groupGiftAmount,
                    registryMetaData?.recipients, registryMetaData?.event, registryMetaData?.babyRegistry, registryMetaData?.guestRulesMetaData))
        }

        // ["registry-metadata" : [sub_channel: "KIOSK"]
        @JvmStatic
        fun getCoreRegistryMetadataMap(
            subChannel: RegistrySubChannel?,
            profileAddressId: String?,
            registryType: RegistryType?,
            giftCardsEnabled: Boolean?,
            groupGiftEnabled: Boolean?,
            groupGiftAmount: String?,
            recipientList: List<RegistryRecipientTO>?,
            event: RegistryEventTO?,
            babyRegistry: RegistryBabyTO?
        ): Map<String, Any>? {
            return mapOf(REGISTRY_METADATA to
                RegistryMetaDataTO(subChannel = subChannel, profileAddressId = profileAddressId, registryType = registryType, giftCardsEnabled = giftCardsEnabled,
                    groupGiftEnabled = groupGiftEnabled, groupGiftAmount = groupGiftAmount, recipients = recipientList, event = event, babyRegistry = babyRegistry))
        }

        @JvmStatic
        fun getRegistryMetadataMap(
            subChannel: RegistrySubChannel?,
            profileAddressId: String?,
            registryType: RegistryType?,
            giftCardsEnabled: Boolean?,
            groupGiftEnabled: Boolean?,
            groupGiftAmount: String?,
            recipientList: List<RegistryRecipientTO>?,
            event: RegistryEventTO?,
            babyRegistry: RegistryBabyTO?,
            guestRulesMetaData: RegistryGuestRuleMetaDataTO?
        ): Map<String, Any>? {
            return mapOf(REGISTRY_METADATA to
                RegistryMetaDataTO(subChannel = subChannel, profileAddressId = profileAddressId, registryType = registryType, giftCardsEnabled = giftCardsEnabled,
                    groupGiftEnabled = groupGiftEnabled, groupGiftAmount = groupGiftAmount, recipients = recipientList, event = event, babyRegistry = babyRegistry,
                    guestRulesMetaData = guestRulesMetaData))
        }
    }
}
