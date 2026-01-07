package dev.cosmicderviative.disgaeaevilitysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvilitySearchScreen(
    state: UiState,
    onSearchTextChange: (String) -> Unit,
    onToggleBuilder: (Boolean) -> Unit,
    onToggleFilter: (FilterKey, Boolean) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit,
    onAddToBuild: (Evility) -> Unit,
    onRemoveFromBuild: (Evility) -> Unit,
    onClearBuild: () -> Unit,
    onShareBuild: () -> Unit,
    onImportBuild: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var showFiltersSheet by remember { mutableStateOf(false) }
    var showBuilderSheet by remember { mutableStateOf(false) }

    val filtersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val builderSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disgaea 7 Evility Search") },
                actions = {
                    IconButton(onClick = { showFiltersSheet = true }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filters")
                    }
                    IconButton(onClick = { showBuilderSheet = true }) {
                        Icon(Icons.Filled.Build, contentDescription = "Builder")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                singleLine = true,
                label = { Text("Search") }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.filteredEvilities, key = { it.id }) { ev ->
                    EvilityCard(
                        evility = ev,
                        showBuilder = state.builderActive,
                        onAdd = onAddToBuild
                    )
                }
            }
        }
    }

    if (showFiltersSheet) {
        FiltersBottomSheet(
            sheetState = filtersSheetState,
            state = state,
            onDismiss = {
                scope.launch {
                    filtersSheetState.hide()
                    showFiltersSheet = false
                }
            },
            onToggleBuilder = onToggleBuilder,
            onToggleFilter = onToggleFilter,
            onToggleCategory = onToggleCategory,
            onClearCategories = onClearCategories
        )
    }

    if (showBuilderSheet) {
        BuilderBottomSheet(
            sheetState = builderSheetState,
            state = state,
            onDismiss = {
                scope.launch {
                    builderSheetState.hide()
                    showBuilderSheet = false
                }
            },
            onRemoveFromBuild = onRemoveFromBuild,
            onClearBuild = onClearBuild,
            onShareBuild = onShareBuild,
            onImportBuild = onImportBuild
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersBottomSheet(
    sheetState: SheetState,
    state: UiState,
    onDismiss: () -> Unit,
    onToggleBuilder: (Boolean) -> Unit,
    onToggleFilter: (FilterKey, Boolean) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit
) {
    // Categories come from the data itself; selection keys must match recompute():
    // cat = (ev.category ?: "None"), plus special "All"
    val categoryKeys = remember(state.allEvilities) {
        buildList {
            add("All")
            addAll(
                state.allEvilities
                    .asSequence()
                    .map { it.category ?: "None" }
                    .distinct()
                    .sorted()
                    .toList()
            )
        }.distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)

            TwoColumnFilterRow(
                left = FilterKey.UNIQUE, leftLabel = "Unique",
                right = FilterKey.GENERIC, rightLabel = "Generic",
                filters = state.filters,
                onToggle = onToggleFilter
            )

            TwoColumnFilterRow(
                left = FilterKey.PLAYER, leftLabel = "Player",
                right = FilterKey.ENEMY, rightLabel = "Enemy",
                filters = state.filters,
                onToggle = onToggleFilter
            )

            // BASE_GAME is the real enum constant
            TwoColumnFilterRow(
                left = FilterKey.BASE_GAME, leftLabel = "Base",
                right = FilterKey.DLC, rightLabel = "DLC",
                filters = state.filters,
                onToggle = onToggleFilter
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Builder mode")
                Switch(
                    checked = state.builderActive,
                    onCheckedChange = onToggleBuilder
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Categories", style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = onClearCategories,
                    enabled = state.activeCategories.isNotEmpty()
                ) { Text("Clear") }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryKeys, key = { it }) { key ->
                    val display = state.categoryDisplayNames[key] ?: key
                    FilterChip(
                        selected = state.activeCategories.contains(key),
                        onClick = { onToggleCategory(key) },
                        label = { Text(display, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuilderBottomSheet(
    sheetState: SheetState,
    state: UiState,
    onDismiss: () -> Unit,
    onRemoveFromBuild: (Evility) -> Unit,
    onClearBuild: () -> Unit,
    onShareBuild: () -> Unit,
    onImportBuild: (String) -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Builder", style = MaterialTheme.typography.titleLarge)

            OutlinedButton(
                onClick = { showImportDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import build")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearBuild,
                    modifier = Modifier.weight(1f),
                    enabled = state.build.isNotEmpty()
                ) { Text("Clear") }

                Button(
                    onClick = onShareBuild,
                    modifier = Modifier.weight(1f),
                    enabled = state.build.isNotEmpty()
                ) { Text("Share") }
            }

            if (state.build.isEmpty()) {
                Text("No evilities added yet.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.build, key = { it.id }) { ev ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(ev.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Cost: ${ev.cost}", style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { onRemoveFromBuild(ev) }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import build") },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste share code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onImportBuild(importText)
                        importText = ""
                        showImportDialog = false
                    }
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TwoColumnFilterRow(
    left: FilterKey,
    leftLabel: String,
    right: FilterKey,
    rightLabel: String,
    filters: Map<FilterKey, Boolean>,
    onToggle: (FilterKey, Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        LabeledCheckbox(
            modifier = Modifier.weight(1f),
            label = leftLabel,
            checked = filters[left] == true,
            onCheckedChange = { checked -> onToggle(left, checked) }
        )
        LabeledCheckbox(
            modifier = Modifier.weight(1f),
            label = rightLabel,
            checked = filters[right] == true,
            onCheckedChange = { checked -> onToggle(right, checked) }
        )
    }
}

@Composable
private fun LabeledCheckbox(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
