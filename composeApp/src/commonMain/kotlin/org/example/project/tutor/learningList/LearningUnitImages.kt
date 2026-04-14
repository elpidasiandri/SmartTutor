package org.example.project.tutor.learningList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.tutor.models.domainModels.learningUnits.UnitImageDomain

@Composable
fun LearningUnitImages(
    images: List<UnitImageDomain>,
    resolveUrl: (String) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        images.forEach { img ->
            val path = img.relativePath.trim()
            if (path.isBlank()) return@forEach
            val fullUrl = resolveUrl(path)

            RemoteImage(
                url = fullUrl,
                contentDescription = img.caption,
                modifier = Modifier
                    .fillMaxWidth()
            )

            if (img.relativePath.isNotBlank() && img.caption.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(img.caption, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
expect fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
