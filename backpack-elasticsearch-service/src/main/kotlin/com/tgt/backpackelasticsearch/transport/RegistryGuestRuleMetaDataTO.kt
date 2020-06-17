package com.tgt.backpackelasticsearch.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegistryGuestRuleMetaDataTO(
    @JsonProperty("guest_rules")
    val guestRules: Map<String, Any>? = null
) {
    companion object {
        const val REGISTRY_GUEST_RULES_METADATA = "registry-guest-rules-metadata"
        val mapper = ObjectMapper()

        @JvmStatic
        fun getRegistryGuestRulesMetadata(metadata: Map<String, Any>?): RegistryGuestRuleMetaDataTO? {
            return metadata?.takeIf { metadata.containsKey(REGISTRY_GUEST_RULES_METADATA) }
                ?.let {
                    mapper.readValue<RegistryGuestRuleMetaDataTO>(
                        mapper.writeValueAsString(metadata[REGISTRY_GUEST_RULES_METADATA]))
                }
        }

        @JvmStatic
        fun getRegistryGuestRulesMetadataMap(
            guestRules: Map<String, Any>?
        ): Map<String, Any>? {
            return mapOf(REGISTRY_GUEST_RULES_METADATA to
                RegistryGuestRuleMetaDataTO(guestRules = guestRules))
        }
    }
}
