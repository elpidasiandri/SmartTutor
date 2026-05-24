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
    val normalizedInput = input
        .replace("\\n", "\n")
        .replace("```java ", "```java\n")
        .replace("```kotlin ", "```kotlin\n")
        .replace("```python ", "```python\n")
        .replace("```javascript ", "```javascript\n")

    val regex = Regex(
        "```\\s*([A-Za-z0-9_+-]*)?\\s*\\n?([\\s\\S]*?)\\s*```",
        RegexOption.MULTILINE
    )

    val out = mutableListOf<RichBlock>()
    var last = 0

    regex.findAll(normalizedInput).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1

        val before = normalizedInput.substring(last, start)
        if (before.isNotBlank()) {
            out += RichBlock.Text(before.trimEnd())
        }

        val lang = m.groupValues
            .getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val code = m.groupValues[2].trim()

        if (code.isNotBlank()) {
            out += RichBlock.Code(
                code = code,
                language = lang
            )
        }

        last = end
    }

    val tail = normalizedInput.substring(last)
    if (tail.isNotBlank()) {
        out += RichBlock.Text(tail.trim())
    }

    return out
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

/**
 * Converts inline `code` to italic WITHOUT backticks.
 * Applies ONLY to normal text blocks (never called inside code blocks).
 */
private fun AnnotatedString.Builder.appendInlineCodeAsItalic(line: String) {
    // catch:
    // 1) `token`
    // 2) 'token'
    // 3) ‘token’ (smart quotes)
    val regex = Regex(
        "(`([^`]+)`)|(\\\\\\(([^)]+)\\\\\\))|('([^']+)')|(‘([^’]+)’)"
    )
    var last = 0

    regex.findAll(line).forEach { m ->
        val start = m.range.first
        val end = m.range.last + 1

        if (start > last) append(line.substring(last, start))

        val token = listOf(
            m.groupValues[2],
            m.groupValues[4],
            m.groupValues[6],
            m.groupValues[8]
        ).firstOrNull { it.isNotBlank() }
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
