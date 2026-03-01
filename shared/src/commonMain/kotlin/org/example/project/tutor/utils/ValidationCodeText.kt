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

fun normalizeForHeaderCompare(s: String): String {
    return s
        .trim()
        .replace(Regex("^\\s*\\d{1,12}\\s*[.)-]\\s*"), "")
        .replace(Regex("^\\s*[-•–—]+\\s*"), "")
        .trim()
}
fun splitIntoCodeAndTextBlocks(input: String): List<RichBlock> {
    // Matches ```lang\n ... \n```
    val regex = Regex("```(\\w+)?\\n([\\s\\S]*?)\\n```")
    val out = mutableListOf<RichBlock>()
    var last = 0

    regex.findAll(input).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1

        val before = input.substring(last, start)
        if (before.isNotBlank()) out += RichBlock.Text(before.trimEnd())

        val lang = m.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() }
        val code = m.groupValues[2]
        out += RichBlock.Code(code = code.trimEnd(), language = lang)

        last = end
    }

    val tail = input.substring(last)
    if (tail.isNotBlank()) out += RichBlock.Text(tail.trim())

    return out
}

fun buildStyledLearningText(raw: String): AnnotatedString {
    val lines = raw.split("\n")

    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            val trimmed = line.trimEnd()

            when {
                // headings ### ...
                trimmed.trimStart().startsWith("###") -> {
                    val title = trimmed.trimStart().removePrefix("###").trimStart()
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendInlineCodeAsItalic(title)
                    }
                }

                else -> appendInlineCodeAsItalic(trimmed)
            }

            if (index != lines.lastIndex) append("\n")
        }
    }
}

/**
 * Converts inline `code` to italic WITHOUT backticks.
 * Applies ONLY to normal text blocks (never called inside code blocks).
 */
private fun AnnotatedString.Builder.appendInlineCodeAsItalic(line: String) {
    // catch:
    // 1) `token`
    // 2) 'token'
    // 3) ‘token’ (smart quotes)
    val regex = Regex("(`([^`]+)`)|('([^']+)')|(‘([^’]+)’)")
    var last = 0

    regex.findAll(line).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1

        if (start > last) append(line.substring(last, start))

        val token = listOf(m.groupValues[2], m.groupValues[4], m.groupValues[6])
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

        val looksLikeCode =
            token.length in 1..40 && token.any { it.code in 33..126 }
        if (looksLikeCode) {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(token) }
        } else {
            append(line.substring(start, end))
        }

        last = end
    }

    if (last < line.length) append(line.substring(last))
}
