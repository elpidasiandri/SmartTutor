package org.example.project.tutor.excercise

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.tutor.learningList.LearningRichMessage

@Composable
fun ExerciseRichItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    LearningRichMessage(
        text = normalizeRichText(text),
        modifier = modifier
    )
}

fun normalizeRichText(text: String): String {
    return text
        .replace("\\n", "\n")
        .replace("\r\n", "\n")
        .trim()
}