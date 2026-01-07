package dev.cosmicderviative.disgaeaevilitysearch

import android.content.Context
import kotlinx.serialization.json.Json

class EvilityRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun loadAllEvilities(): List<Evility> {
        val base = readAsset("data/evilities.json")
        val dlc = readAsset("data/evilities_dlc.json")

        val baseList = json.decodeFromString<List<Evility>>(base)
        val dlcList = json.decodeFromString<List<Evility>>(dlc)

        val temp = mutableListOf<Evility>()
        for (ev in baseList) {
            temp += ev.copy(dlc = false)
            // Match the web app behavior: insert DLC list after this id
            if (ev.id == "EVILITY_ID_OPENER_GENERIC_5") {
                temp += dlcList.map { it.copy(dlc = true) }
            }
        }

        // Set computed fields: number + cost override for unique
        return temp.mapIndexed { idx, e ->
            e.copy(
                number = idx + 1,
                cost = if (e.unique) 1 else e.cost
            )
        }
    }

    fun loadCategoryDisplayNames(): Map<String, String> {
        val raw = readAsset("data/evility_categories.json")
        // JSON is a simple object map in the web app
        return json.decodeFromString<Map<String, String>>(raw)
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
