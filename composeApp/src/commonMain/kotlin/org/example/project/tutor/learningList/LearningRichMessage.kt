package org.example.project.tutor.learningList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.tutor.utils.RichBlock
import org.example.project.tutor.utils.buildStyledLearningText
import org.example.project.tutor.utils.splitIntoCodeAndTextBlocks

@Composable
fun LearningRichMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { splitIntoCodeAndTextBlocks(text) }

    Column(modifier = modifier) {
        blocks.forEach { block ->
            key(
                when (block) {
                    is RichBlock.Code -> "c:${block.code.hashCode()}:${block.language}"
                    is RichBlock.Text -> "t:${block.text.hashCode()}"
                }
            ) {
                when (block) {
                    is RichBlock.Code -> {
                        CodeBlockView(code = block.code, language = block.language)
                        Spacer(Modifier.height(8.dp))
                    }

                    is RichBlock.Text -> {
                        val annotated = remember(block.text) { buildStyledLearningText(block.text) }
                        Text(text = annotated, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun CodeBlockView(code: String, language: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        if (!language.isNullOrBlank()) {
            Text(
                text = language,
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE6E6E6)
        )
    }
}
