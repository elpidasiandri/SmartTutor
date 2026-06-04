package org.example.project.tutor.excercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.ask_tutor_about_my_answer
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.excercise_write_your_answer
import org.example.project.strings.SmartTutorStrings.excersize_easy
import org.example.project.strings.SmartTutorStrings.excersize_hard
import org.example.project.strings.SmartTutorStrings.excersize_level
import org.example.project.strings.SmartTutorStrings.excersize_medium
import org.example.project.strings.SmartTutorStrings.exercises_more_button
import org.example.project.strings.SmartTutorStrings.exercises_more_loading
import org.example.project.strings.SmartTutorStrings.explanation_tab
import org.example.project.strings.SmartTutorStrings.hint_tab
import org.example.project.strings.SmartTutorStrings.solution_tab
import org.example.project.strings.SmartTutorStrings.tutor_answer
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
    currentUnitIndex: Int,
    selectedExerciseIndex: Int,
    answeredExerciseKeys: Set<String>,
    onMoreExercisesClick: () -> Unit,
    isLoadingMoreExercises: Boolean,
) {
    val safeIndex = selectedExerciseIndex.coerceIn(0, exercises.lastIndex)
    val exercise = exercises[safeIndex]
    val isLastExercise = safeIndex == exercises.lastIndex
    Column(
        modifier = modifier.padding(horizontal = spacing8),
        verticalArrangement = Arrangement.spacedBy(spacing12)
    ) {
        Text(
            text = "$excercise ${safeIndex + 1}/${exercises.size}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        ExerciseCard(
            index = safeIndex,
            exercise = exercise,
            tutorAnswer = exerciseTutorAnswers[safeIndex],
            onExerciseAnswered = onExerciseAnswered,
            onAskTutorForOpenExercise = onAskTutorForOpenExercise,
            answeredExerciseKeys = answeredExerciseKeys,
            currentUnitIndex = currentUnitIndex
        )

    }
    if (isLastExercise) {
        Button(
            onClick = onMoreExercisesClick,
            enabled = !isLoadingMoreExercises,
            modifier = Modifier.fillMaxWidth().padding(bottom = spacing24)
        ) {
            Text(
                if (isLoadingMoreExercises) exercises_more_loading
                else exercises_more_button
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
    answeredExerciseKeys: Set<String>,
    currentUnitIndex: Int,

    ) {
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }
    val exerciseKey = "$currentUnitIndex-$index"
    val answered = answeredExerciseKeys.contains(exerciseKey)
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
                text = "$excercise ${index + 1}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(4.dp))

            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = "$excersize_level : ${difficultyLabel(exercise.difficulty)}",
                fontSize = 12.sp,
                color = AppColors.GrayDark
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
                val shuffledOptions = remember(exercise.question) {
                    exercise.options
                        .mapIndexed { originalIndex, text -> originalIndex to text }
                        .shuffled()
                }

                shuffledOptions.forEachIndexed { optIndex, option ->
                    val originalIndex = option.first
                    val opt = option.second

                    val letter = ('A' + optIndex).toString()
                    val isSelected = selectedIndex == optIndex
                    val clickedIsCorrect = originalIndex == exercise.answerIndex

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
                    placeholder = { Text(excercise_write_your_answer) }
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
                    Text(ask_tutor_about_my_answer)
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
                            text = tutor_answer,
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
            ExpandSection(title = hint_tab, text = exercise.hint)
        }

        if (showExplanation && exercise.explanation.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = explanation_tab, text = exercise.explanation)
        }

        if (showSolution && exercise.solution.isNotBlank() && exercise.type == ExerciseType.OPEN) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = solution_tab, text = exercise.solution)
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
            text = hint_tab,
            selected = showHint,
            onClick = onToggleHint
        )

        SmallToggleChip(
            text = explanation_tab,
            selected = showExplanation,
            onClick = onToggleExplanation
        )
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

private fun difficultyLabel(difficulty: String?): String {
    return when (difficulty?.lowercase()) {
        "easy" -> "$excersize_easy"
        "medium" -> "$excersize_medium"
        "hard" -> "$excersize_hard"
        else -> "Άγνωστο"
    }
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

