package dev.cosmicderviative.disgaeaevilitysearch

import kotlinx.serialization.Serializable

@Serializable
data class Evility(
    val id: String,
    val name: String,
    val description: String,
    val notes: String = "",
    val unique: Boolean = false,
    val cost: Int = 0,
    val enemyOnly: Boolean = false,
    val unlock: String = "",
    val category: String? = null,
    val dlc: Boolean = false,
    val fixed: String? = null,

    // computed
    val number: Int = 0
)

@Serializable
data class CategoryMap(
    val map: Map<String, String> = emptyMap()
)
