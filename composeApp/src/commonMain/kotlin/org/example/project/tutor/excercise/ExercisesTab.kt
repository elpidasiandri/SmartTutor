package org.example.project.tutor.excercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing8
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import kotlin.time.TimeSource

@Composable
fun ExercisesTab(
    modifier: Modifier = Modifier,
    exercises: List<ExerciseDomain>,
    exerciseTutorAnswers: Map<Int, String>,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
    ) -> Unit,
) {
    if (exercises.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No exercises found.")
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = spacing16),
        verticalArrangement = Arrangement.spacedBy(spacing12)
    ) {
        itemsIndexed(
            exercises,
            key = { index, ex ->
                ex.question.ifBlank { index.toString() }
            }
        ) { index, ex ->
            ExerciseCard(
                index = index,
                exercise = ex,
                tutorAnswer = exerciseTutorAnswers[index],
                onExerciseAnswered = onExerciseAnswered,
                onAskTutorForOpenExercise = onAskTutorForOpenExercise
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    index: Int,
    exercise: ExerciseDomain,
    tutorAnswer: String?,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
    ) -> Unit,
) {
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }

    var selectedIndex by rememberSaveable(index) { mutableStateOf<Int?>(null) }
    var tfSelected by rememberSaveable(index) { mutableStateOf<Boolean?>(null) }
    var openAnswer by rememberSaveable(index) { mutableStateOf("") }

    val type = exercise.type

    val startedAt = remember(exercise.question) {
        TimeSource.Monotonic.markNow()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(spacing12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exercise ${index + 1}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            HintExplainSolutionRow(
                showHint = showHint,
                showExplanation = showExplanation,
                showSolution = showSolution,
                onToggleHint = { showHint = !showHint },
                onToggleExplanation = { showExplanation = !showExplanation },
                onToggleSolution = { showSolution = !showSolution },
                type = type
            )
        }

        Spacer(Modifier.height(spacing8))

        ExerciseRichItem(text = exercise.question)

        Spacer(Modifier.height(spacing8))

        when (type) {
            ExerciseType.MCQ -> {
                exercise.options.forEachIndexed { optIndex, opt ->
                    val letter = ('A' + optIndex).toString()
                    val isSelected = selectedIndex == optIndex
                    val clickedIsCorrect = isCorrectOption(exercise, optIndex, opt)

                    OptionRow(
                        label = "$letter)",
                        text = opt,
                        selected = isSelected,
                        isCorrect = isSelected && clickedIsCorrect,
                        isWrong = isSelected && !clickedIsCorrect,
                        onClick = {
                            selectedIndex = optIndex
                            onExerciseAnswered(
                                index,
                                ExerciseType.MCQ,
                                opt,
                                correctAnswerText(exercise),
                                clickedIsCorrect,
                                startedAt.elapsedNow().inWholeMilliseconds,
                                exercise.question
                            )
                        }
                    )
                }
            }

            ExerciseType.TRUE_FALSE -> {
                val trueLabel = exercise.options.getOrNull(0) ?: "True"
                val falseLabel = exercise.options.getOrNull(1) ?: "False"

                val correctAnswer = exercise.solution.trim()

                val trueCorrect =
                    correctAnswer.contains(trueLabel, ignoreCase = true) ||
                            correctAnswer.contains("true", ignoreCase = true)

                val falseCorrect =
                    correctAnswer.contains(falseLabel, ignoreCase = true) ||
                            correctAnswer.contains("false", ignoreCase = true)

                OptionRow(
                    label = "",
                    text = trueLabel,
                    selected = tfSelected == true,
                    isCorrect = tfSelected == true && trueCorrect,
                    isWrong = tfSelected == true && !trueCorrect,
                    onClick = {
                        tfSelected = true
                        onExerciseAnswered(
                            index,
                            ExerciseType.TRUE_FALSE,
                            trueLabel,
                            correctAnswer,
                            trueCorrect,
                            startedAt.elapsedNow().inWholeMilliseconds,
                            exercise.question
                        )
                    }
                )

                OptionRow(
                    label = "",
                    text = falseLabel,
                    selected = tfSelected == false,
                    isCorrect = tfSelected == false && falseCorrect,
                    isWrong = tfSelected == false && !falseCorrect,
                    onClick = {
                        tfSelected = false
                        onExerciseAnswered(
                            index,
                            ExerciseType.TRUE_FALSE,
                            falseLabel,
                            correctAnswer,
                            falseCorrect,
                            startedAt.elapsedNow().inWholeMilliseconds,
                            exercise.question
                        )
                    }
                )
            }

            ExerciseType.OPEN -> {
                OutlinedTextField(
                    value = openAnswer,
                    onValueChange = { openAnswer = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your answer...") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onAskTutorForOpenExercise(
                            index,
                            exercise.question,
                            openAnswer,
                            exercise.solution
                        )
                        openAnswer = ""
                    },
                    enabled = openAnswer.isNotBlank()
                ) {
                    Text("Ask tutor about my answer")
                }

                if (!tutorAnswer.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF3F3F3),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Tutor Answer",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        ExerciseRichItem(text = tutorAnswer)
                    }
                }
            }

            else -> {
                exercise.options.forEachIndexed { optIndex, opt ->
                    val letter = ('A' + optIndex).toString()
                    val isSelected = selectedIndex == optIndex
                    val clickedIsCorrect = isCorrectOption(exercise, optIndex, opt)

                    OptionRow(
                        label = "$letter)",
                        text = opt,
                        selected = isSelected,
                        isCorrect = isSelected && clickedIsCorrect,
                        isWrong = isSelected && !clickedIsCorrect,
                        onClick = {
                            selectedIndex = optIndex
                            onExerciseAnswered(
                                index,
                                ExerciseType.MCQ,
                                opt,
                                correctAnswerText(exercise),
                                clickedIsCorrect,
                                startedAt.elapsedNow().inWholeMilliseconds,
                                exercise.question
                            )
                        }
                    )
                }
            }
        }

        if (showHint && exercise.hint.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Hint", text = exercise.hint)
        }

        if (showExplanation && exercise.explanation.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Explanation", text = exercise.explanation)
        }

        if (showSolution && exercise.solution.isNotBlank() && exercise.type == ExerciseType.OPEN) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Solution", text = exercise.solution)
        }
    }
}

private fun isCorrectOption(
    exercise: ExerciseDomain,
    optIndex: Int,
    optText: String,
): Boolean {
    val answerIndex = exercise.answerIndex

    if (
        answerIndex != null &&
        answerIndex in exercise.options.indices
    ) {
        return optIndex == answerIndex
    }

    return exercise.solution
        .trim()
        .contains(optText.trim(), ignoreCase = true)
}

private fun correctAnswerText(
    exercise: ExerciseDomain,
): String {
    val answerIndex = exercise.answerIndex

    return if (
        answerIndex != null &&
        answerIndex in exercise.options.indices
    ) {
        exercise.options[answerIndex]
    } else {
        exercise.solution
    }
}

@Composable
private fun HintExplainSolutionRow(
    showHint: Boolean,
    showExplanation: Boolean,
    showSolution: Boolean,
    onToggleHint: () -> Unit,
    onToggleExplanation: () -> Unit,
    onToggleSolution: () -> Unit,
    type: ExerciseType,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SmallToggleChip(
            text = "Hint",
            selected = showHint,
            onClick = onToggleHint
        )

        SmallToggleChip(
            text = "Explain",
            selected = showExplanation,
            onClick = onToggleExplanation
        )

        if (type == ExerciseType.OPEN) {
            SmallToggleChip(
                text = "Solution",
                selected = showSolution,
                onClick = onToggleSolution
            )
        }
    }
}

@Composable
private fun SmallToggleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) AppColors.Purple else AppColors.GrayDark
        )
    ) {
        Text(text, fontSize = 13.sp)
    }
}

@Composable
private fun OptionRow(
    label: String,
    text: String,
    selected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        isCorrect -> Color(0xFFE6F4EA)
        isWrong -> Color(0xFFFFE8E8)
        selected -> Color(0xFFEDE7F6)
        else -> Color(0xFFF6F6F6)
    }

    val borderColor = when {
        isCorrect -> Color(0xFF2E7D32)
        isWrong -> Color(0xFFC62828)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Text(text)
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ExpandSection(
    title: String,
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(6.dp))

        ExerciseRichItem(text = text)
    }
}

