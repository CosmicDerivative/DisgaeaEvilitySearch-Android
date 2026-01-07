package dev.cosmicderviative.disgaeaevilitysearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EvilityCard(
    evility: Evility,
    showBuilder: Boolean,
    onAdd: (Evility) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("#${evility.number} • ${evility.name}", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Cost: ${evility.cost}  •  Category: ${evility.category ?: "None"}  •  ${if (evility.dlc) "DLC" else "Base"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (evility.enemyOnly) {
                        Text("Enemy-only", style = MaterialTheme.typography.bodySmall)
                    }
                    if (evility.unique) {
                        Text("Unique", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (showBuilder) {
                    Button(onClick = { onAdd(evility) }) {
                        Text("Add")
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(evility.description)
            if (evility.unlock.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Source: ${evility.unlock}", style = MaterialTheme.typography.bodySmall)
            }
            if (evility.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(evility.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
