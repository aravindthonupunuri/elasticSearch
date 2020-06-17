package com.tgt.backpackelasticsearch.util

import com.tgt.lists.lib.api.util.ListSortFieldGroup
import com.tgt.lists.lib.api.util.ListSortOrderGroup

enum class RecipientType {
    REGISTRANT,
    COREGISTRANT
}

enum class RegistryType {
    BABY,
    WEDDING
}

enum class RegistryChannel(val value: String) {
    WEB("web"),
    MOBILE("mobile");

    companion object {
        fun toRegistryChannel(channel: String): RegistryChannel {
            return values().firstOrNull { it.toString().toLowerCase() == channel.toLowerCase() }
                ?: WEB
        }
    }
}

enum class RegistrySubChannel(val value: String) {
    KIOSK("kiosk")
}

enum class RegistrySearchSortFieldGroup(val value: String) {
    NAME("name"),
    EVENT_DATE("event_date"),
    LOCATION("location")
}

enum class RegistrySortFieldGroup(val value: String) {
    REGISTRY_TITLE("registry_title") {
        override fun toListSortField() = ListSortFieldGroup.LIST_TITLE
    },
    ADDED_DATE("added_date") {
        override fun toListSortField() = ListSortFieldGroup.ADDED_DATE
    },
    LAST_MODIFIED_DATE("last_modified_date") {
        override fun toListSortField() = ListSortFieldGroup.LAST_MODIFIED_DATE
    };
    abstract fun toListSortField(): ListSortFieldGroup
}

enum class RegistrySortOrderGroup(val value: String) {
    ASCENDING("asc") {
        override fun toListSortOrder() = ListSortOrderGroup.ASCENDING
    },
    DESCENDING("desc") {
        override fun toListSortOrder() = ListSortOrderGroup.DESCENDING
    };
    abstract fun toListSortOrder(): ListSortOrderGroup
}

enum class RegistryFlow(val value: String) {
    REGISTRANT("registrant"),
    GIFT_GIVER("gift_giver");
}

enum class RegistryContentsFieldGroup(val value: String) {
    REGISTRY("registry"),
    REGISTRY_ITEMS("registry_items")
}
