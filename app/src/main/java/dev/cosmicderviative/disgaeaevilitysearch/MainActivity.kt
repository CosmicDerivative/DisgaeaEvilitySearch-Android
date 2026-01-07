package dev.cosmicderviative.disgaeaevilitysearch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    private val vm: EvilityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle optional deep link "build" query param if present
        val buildFromIntent = intent?.data?.getQueryParameter("build")
        if (!buildFromIntent.isNullOrBlank()) {
            vm.loadBuildFromString(buildFromIntent)
        }

        setContent {
            MaterialTheme {
                Surface {
                    EvilitySearchScreen(
                        state = vm.uiState,
                        onSearchTextChange = vm::setSearchText,
                        onToggleBuilder = vm::setBuilderActive,
                        onToggleFilter = vm::toggleFilter,
                        onToggleCategory = vm::toggleCategory,
                        onClearCategories = vm::clearCategories,
                        onAddToBuild = vm::addToBuild,
                        onRemoveFromBuild = vm::removeFromBuild,
                        onClearBuild = vm::clearBuild,
                        onShareBuild = {
                            val shareText = vm.buildShareText()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share build"))
                        },
                        onImportBuild = vm::importBuildFromCode
                    )
                }
            }
        }
    }
}
