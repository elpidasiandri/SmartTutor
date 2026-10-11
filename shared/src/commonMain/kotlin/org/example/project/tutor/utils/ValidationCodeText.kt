package org.example.project.tutor.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

sealed class RichBlock {
    data class Text(val text: String) : RichBlock()
    data class Code(val code: String, val language: String?) : RichBlock()
}


fun splitIntoCodeAndTextBlocks(input: String): List<RichBlock> {
    val normalizedInput = input.replace("\r\n", "\n").replace('\r', '\n')

    val regex = Regex("""(?m)^```([A-Za-z0-9_+-]*)[ \t]*\n([\s\S]*?)^```[ \t]*$""")

    val blocks = mutableListOf<RichBlock>()
    var lastIndex = 0

    regex.findAll(normalizedInput).forEach { match ->
        val textBefore = normalizedInput.substring(
            lastIndex,
            match.range.first
        )

        if (textBefore.isNotBlank()) {
            blocks += RichBlock.Text(textBefore.trimEnd())
        }

        val language = match.groupValues[1]
            .takeIf { it.isNotBlank() }

        val code = match.groupValues[2]
            .trimEnd('\n')

        if (code.isNotBlank()) {
            blocks += RichBlock.Code(
                code = code,
                language = language
            )
        }

        lastIndex = match.range.last + 1
    }

    val remainingText = normalizedInput.substring(lastIndex)

    if (remainingText.isNotBlank()) {
        blocks += RichBlock.Text(remainingText.trim())
    }

    return blocks
}


fun buildStyledLearningText(raw: String): AnnotatedString {
    val lines = raw.split("\n")

    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            val trimmed = line.trimEnd()

            when {
                trimmed.trimStart().startsWith("###") -> {
                    val title = trimmed.trimStart().removePrefix("###").trimStart()
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendMarkdownStyles(title)
                    }
                }

                else -> appendMarkdownStyles(trimmed)
            }

            if (index != lines.lastIndex) append("\n")
        }
    }
}
private fun AnnotatedString.Builder.appendMarkdownStyles(line: String) {
    val regex = Regex(
        "(\\*\\*([^*]+)\\*\\*)|(`([^`]+)`)|(\\\\\\(([^)]+)\\\\\\))|('([^']+)')|(‘([^’]+)’)"
    )

    var last = 0

    regex.findAll(line).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1

        if (start > last) append(line.substring(last, start))

        when {
            m.groupValues[2].isNotBlank() -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(m.groupValues[2])
                }
            }

            m.groupValues[4].isNotBlank() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(m.groupValues[4])
                }
            }

            m.groupValues[6].isNotBlank() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(m.groupValues[6])
                }
            }

            m.groupValues[8].isNotBlank() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(m.groupValues[8])
                }
            }

            m.groupValues[10].isNotBlank() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(m.groupValues[10])
                }
            }
        }

        last = end
    }

    if (last < line.length) append(line.substring(last))
}

