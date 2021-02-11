package com.tgt.backpackelasticsearch.test.util

import com.tgt.backpackregistryclient.transport.*
import com.tgt.backpackregistryclient.util.RegistrySearchVisibility
import com.tgt.lists.atlas.api.type.UserMetaData.Companion.toUserMetaData
import java.util.*

@Suppress("UNCHECKED_CAST")
class RegistryDataProvider {

    fun getRegistryMetaDataMap(
        profileAddressId: UUID?,
        alternateRegistryId: String?,
        giftCardsEnabled: Boolean?,
        groupGiftEnabled: Boolean?,
        groupGiftAmount: String?,
        recipients: List<RegistryRecipientTO>?,
        event: RegistryEventTO?,
        babyRegistry: RegistryBabyTO?,
        guestRulesMetaData: RegistryGuestRuleMetaDataTO?,
        imageMetaData: RegistryImageMetaDataTO?,
        customUrl: String?,
        organizationName: String?,
        occasionName: String?,
        searchVisibility: RegistrySearchVisibility?
    ): Map<String, Any>? {
        return toUserMetaData(RegistryMetaDataTO.toStringRegistryMetadata(RegistryMetaDataTO(profileAddressId, alternateRegistryId,
            giftCardsEnabled, groupGiftEnabled, groupGiftAmount, recipients, event, babyRegistry, guestRulesMetaData,
            imageMetaData, customUrl, organizationName, occasionName, searchVisibility)))?.metadata
    }
}
