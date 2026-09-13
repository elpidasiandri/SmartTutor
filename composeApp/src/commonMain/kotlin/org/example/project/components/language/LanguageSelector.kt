package org.example.project.components.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "Language"
        )

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

