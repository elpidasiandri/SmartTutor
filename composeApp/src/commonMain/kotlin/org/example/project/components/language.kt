package org.example.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.strings.AppLanguage

@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        FilterChip(
            selected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = {
                onLanguageSelected(AppLanguage.ENGLISH)
            },
            label = {
                Text("English")
            }
        )

        FilterChip(
            selected = selectedLanguage == AppLanguage.GREEK,
            onClick = {
                onLanguageSelected(AppLanguage.GREEK)
            },
            label = {
                Text("Ελληνικά")
            }
        )
    }
}
