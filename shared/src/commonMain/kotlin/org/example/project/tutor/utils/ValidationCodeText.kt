package org.example.project.tutor.utils

sealed interface MsgSegment {
    data class Text(val value: String) : MsgSegment
    data class Code(val language: String?, val value: String) : MsgSegment
}

private val fencedCodeRegex =
    Regex("```([a-zA-Z0-9_+-]*)\\n([\\s\\S]*?)\\n```", RegexOption.MULTILINE)

fun parseFencedCodeBlocks(input: String): List<MsgSegment> {
    val segments = mutableListOf<MsgSegment>()
    var lastIndex = 0

    fencedCodeRegex.findAll(input).forEach { match ->
        val start = match.range.first
        val endExclusive = match.range.last + 1

        val before = input.substring(lastIndex, start)
        if (before.isNotBlank()) segments += MsgSegment.Text(before)

        val lang = match.groupValues[1].trim().ifBlank { null }
        val code = match.groupValues[2]
        segments += MsgSegment.Code(language = lang, value = code)

        lastIndex = endExclusive
    }

    val tail = input.substring(lastIndex)
    if (tail.isNotBlank()) segments += MsgSegment.Text(tail)

    return segments
}
