package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.tutor.utils.MsgSegment
import org.example.project.tutor.utils.parseFencedCodeBlocks

@Composable
fun CodeBlock(
    code: String,
    language: String? = null,
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111318))
            .border(1.dp, Color(0xFF2A2F3A), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (!language.isNullOrBlank()) {
            Text(
                text = language,
                color = Color(0xFFB5C0FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        SelectionContainer {
            Text(
                text = code,
                color = Color(0xFFE6E6E6),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.horizontalScroll(hScroll)
            )
        }
    }
}

@Composable
fun ExplanationBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8E8E8))
            .padding(12.dp)
    ) {
        Text(
            text = text.trim(),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color.Black
        )
    }
}
@Composable
fun AiRichMessageWithCode(
    text: String,
) {
    val segments = remember(text) { parseFencedCodeBlocks(text) }

    Column {
        segments.forEachIndexed { idx, seg ->
            when (seg) {
                is MsgSegment.Text -> {
                    val chunk = seg.value.trim()
                    if (chunk.isNotEmpty()) {
                        // Αν θες εδώ μπορείς να καλέσεις το παλιό σου AiRichMessage(chunk)
                        // για markers [image ...], [quote ...] κλπ.
                        ExplanationBlock(text = chunk)
                    }
                }

                is MsgSegment.Code -> {
                    CodeBlock(
                        code = seg.value,
                        language = seg.language
                    )
                }
            }

            if (idx != segments.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}