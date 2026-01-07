package dev.cosmicderviative.disgaeaevilitysearch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class FilterKey {
    UNIQUE, GENERIC, PLAYER, ENEMY, BASE_GAME, DLC
}

data class UiState(
    val loading: Boolean = true,
    val allEvilities: List<Evility> = emptyList(),
    val filteredEvilities: List<Evility> = emptyList(),
    val categoryDisplayNames: Map<String, String> = emptyMap(),

    val searchText: String = "",
    val builderActive: Boolean = false,

    val filters: Map<FilterKey, Boolean> = mapOf(
        FilterKey.UNIQUE to true,
        FilterKey.GENERIC to true,
        FilterKey.PLAYER to true,
        FilterKey.ENEMY to false,
        FilterKey.BASE_GAME to true,
        FilterKey.DLC to true,
    ),

    // Category filters are stored by display string value (matches web app semantics)
    val activeCategories: Set<String> = emptySet(),

    val build: List<Evility> = emptyList(),
    val error: String? = null
)

class EvilityViewModel(app: Application) : AndroidViewModel(app) {
    private var pendingBuild: String? = null

    private val repo = EvilityRepository(app.applicationContext)

    var uiState by mutableStateOf(UiState())
        private set

    init {
        viewModelScope.launch {
            try {
                val (evs, cats) = withContext(Dispatchers.IO) {
                    repo.loadAllEvilities() to repo.loadCategoryDisplayNames()
                }
                uiState = uiState.copy(
                    loading = false,
                    allEvilities = evs,
                    categoryDisplayNames = cats
                )
                recompute()
                pendingBuild?.let {
                    pendingBuild = null
                    loadBuildFromString(it)
                }
            } catch (t: Throwable) {
                uiState = uiState.copy(loading = false, error = t.message ?: "Failed to load data")
            }
        }
    }

    fun importBuildFromCode(codeRaw: String) {
        val raw = codeRaw.trim()
        if (raw.isBlank()) return

        // Allow pasting the full share message, e.g.:
        // "Disgaea 7 Evility build: 1_2_3"
        val cleaned = raw.substringAfter("build:", raw).trim()

        // Accept either "EV7:1,2,3" or plain numbers/underscores
        val payload = if (cleaned.startsWith("EV7:", ignoreCase = true)) {
            cleaned.substringAfter(":")
        } else cleaned

        val numbers = payload
            .split(",", " ", "\n", "\t", "_")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()

        if (numbers.isEmpty()) return

        val all = uiState.allEvilities
        if (all.isEmpty()) {
            // Data not loaded yet; queue it like loadBuildFromString does
            pendingBuild = numbers.joinToString("_")
            return
        }

        val newBuild = all.filter { it.number in numbers }

        uiState = uiState.copy(
            build = newBuild,
            builderActive = true
        )
    }

    fun setSearchText(text: String) {
        uiState = uiState.copy(searchText = text)
        recompute()
    }

    fun setBuilderActive(active: Boolean) {
        uiState = uiState.copy(builderActive = active)
    }

    fun toggleFilter(key: FilterKey, value: Boolean) {
        uiState = uiState.copy(filters = uiState.filters.toMutableMap().apply { put(key, value) })
        recompute()
    }

    fun toggleCategory(categoryDisplayName: String) {
        val s = uiState.activeCategories.toMutableSet()
        if (s.contains(categoryDisplayName)) s.remove(categoryDisplayName) else s.add(categoryDisplayName)
        uiState = uiState.copy(activeCategories = s)
        recompute()
    }

    fun clearCategories() {
        uiState = uiState.copy(activeCategories = emptySet())
        recompute()
    }

    fun addToBuild(ev: Evility) {
        if (!uiState.builderActive) return
        if (uiState.build.any { it.number == ev.number }) return
        uiState = uiState.copy(build = uiState.build + ev)
    }

    fun removeFromBuild(ev: Evility) {
        uiState = uiState.copy(build = uiState.build.filterNot { it.number == ev.number })
    }

    fun clearBuild() {
        uiState = uiState.copy(build = emptyList())
    }

    fun loadBuildFromString(build: String) {
        if (uiState.allEvilities.isEmpty()) {
            pendingBuild = build
            return
        }
        val nums = build.split("_").mapNotNull { it.toIntOrNull() }.toSet()
        val buildList = uiState.allEvilities.filter { nums.contains(it.number) }
        uiState = uiState.copy(build = buildList, builderActive = true)
    }

    fun buildShareText(): String {
        val buildStr = uiState.build.joinToString("_") { it.number.toString() }
        return if (buildStr.isBlank()) {
            "Disgaea 7 Evility build: (empty)"
        } else {
            "Disgaea 7 Evility build: $buildStr"
        }
    }

    private fun recompute() {
        val q = uiState.searchText.trim().lowercase()
        val f = uiState.filters
        val cats = uiState.activeCategories

        val filtered = uiState.allEvilities.asSequence()
            .filter { ev ->
                val isUnique = ev.unique
                val isEnemy = ev.enemyOnly
                val isDlc = ev.dlc

                val okUnique = if (isUnique) (f[FilterKey.UNIQUE] == true) else (f[FilterKey.GENERIC] == true)
                val okEnemy = if (isEnemy) (f[FilterKey.ENEMY] == true) else (f[FilterKey.PLAYER] == true)
                val okDlc = if (isDlc) (f[FilterKey.DLC] == true) else (f[FilterKey.BASE_GAME] == true)

                okUnique && okEnemy && okDlc
            }
            .filter { ev ->
                if (cats.isEmpty()) return@filter true
                val cat = (ev.category ?: "None")
                cats.contains(cat) || (cats.contains("All"))
            }
            .filter { ev ->
                if (q.isBlank()) return@filter true
                ev.name.lowercase().contains(q) ||
                        ev.description.lowercase().contains(q) ||
                        ev.unlock.lowercase().contains(q) ||
                        (ev.fixed ?: "").lowercase().contains(q)
            }
            .toList()

        uiState = uiState.copy(filteredEvilities = filtered)
    }
}
