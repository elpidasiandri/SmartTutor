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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.ask_tutor_about_my_answer
import org.example.project.strings.SmartTutorStrings.attemp_info_retry
import org.example.project.strings.SmartTutorStrings.cannot_answer_this_time
import org.example.project.strings.SmartTutorStrings.current_attemp
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.excercise_write_your_answer
import org.example.project.strings.SmartTutorStrings.excersize_easy
import org.example.project.strings.SmartTutorStrings.excersize_hard
import org.example.project.strings.SmartTutorStrings.excersize_level
import org.example.project.strings.SmartTutorStrings.excersize_medium
import org.example.project.strings.SmartTutorStrings.exercises_more_button
import org.example.project.strings.SmartTutorStrings.exercises_more_loading
import org.example.project.strings.SmartTutorStrings.exersize_has
import org.example.project.strings.SmartTutorStrings.exersize_has_grades
import org.example.project.strings.SmartTutorStrings.explanation_tab
import org.example.project.strings.SmartTutorStrings.grade_
import org.example.project.strings.SmartTutorStrings.hint_tab
import org.example.project.strings.SmartTutorStrings.info_retry
import org.example.project.strings.SmartTutorStrings.must_completed_this_attemp_excersizes
import org.example.project.strings.SmartTutorStrings.progress_unit_not_completed_see_answers
import org.example.project.strings.SmartTutorStrings.solution_tab
import org.example.project.strings.SmartTutorStrings.sum_grade
import org.example.project.strings.SmartTutorStrings.sum_grade_are
import org.example.project.strings.SmartTutorStrings.the_correct_answer_is
import org.example.project.strings.SmartTutorStrings.tutor_answer
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import kotlin.time.TimeSource
import org.example.project.strings.SmartTutorStrings
@Composable
fun ExercisesTab(
    modifier: Modifier = Modifier,
    exercises: List<ExerciseDomain>,
    exerciseTutorAnswers: Map<Int, String>,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String, Double) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
        maxPoint: Double,
    ) -> Unit,
    currentUnitIndex: Int,
    selectedExerciseIndex: Int,
    answeredExerciseKeys: Set<String>,
    onMoreExercisesClick: () -> Unit,
    isLoadingMoreExercises: Boolean,
) {
    val currentAttemptRound = (exercises.minOfOrNull { it.attemptsCount } ?: 0) + 1
    val safeIndex = selectedExerciseIndex.coerceIn(0, exercises.lastIndex)
    val exercise = exercises[safeIndex]
    val isLastExercise = safeIndex == exercises.lastIndex
    val scrollState = rememberScrollState()

    val minAttempts = exercises.minOfOrNull { it.attemptsCount } ?: 0
    val maxAttempts = exercises.maxOfOrNull { it.attemptsCount } ?: 0

    val hasCompletedFullAttempt =
        minAttempts > 0 && minAttempts == maxAttempts

    Column(
        modifier = modifier.padding(horizontal = spacing8).verticalScroll(
            state = scrollState
        ),
        verticalArrangement = Arrangement.spacedBy(spacing12)
    ) {
        Text(
            text = "$current_attemp $currentAttemptRound",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Purple
        )
        ExerciseCard(
            index = safeIndex,
            exercise = exercise,
            tutorAnswer = exerciseTutorAnswers[safeIndex],
            onExerciseAnswered = onExerciseAnswered,
            onAskTutorForOpenExercise = onAskTutorForOpenExercise,
            answeredExerciseKeys = answeredExerciseKeys,
            currentUnitIndex = currentUnitIndex,
            currentAttemptRound = currentAttemptRound,
        )

    }

    if (isLastExercise && hasCompletedFullAttempt) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing24),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = SmartTutorStrings.exercises_more_description,
                fontSize = 13.sp,
                color = AppColors.GrayDark
            )

            Button(
                onClick = onMoreExercisesClick,
                enabled = !isLoadingMoreExercises,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isLoadingMoreExercises) exercises_more_loading
                    else exercises_more_button
                )
            }
        }
    }

}

@Composable
private fun ExerciseCard(
    index: Int,
    exercise: ExerciseDomain,
    tutorAnswer: String?,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String, Double) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
        maxPoint: Double,
    ) -> Unit,
    answeredExerciseKeys: Set<String>,
    currentUnitIndex: Int,
    currentAttemptRound: Int,
    ) {
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }

    var selectedIndex by rememberSaveable(index, currentAttemptRound) {
        mutableStateOf<Int?>(null)
    }

    var tfSelected by rememberSaveable(index, currentAttemptRound) {
        mutableStateOf<Boolean?>(null)
    }

    var openAnswer by rememberSaveable(index) { mutableStateOf("") }

    val latestAttempt = exercise.latestAttempt
    val exerciseAttemptsCount = exercise.attemptsCount

    val canAnswerNow = exerciseAttemptsCount < currentAttemptRound
    val isTemporarilyLocked = !canAnswerNow

    val hasAnsweredCurrentAttempt =
        latestAttempt?.attemptNumber == currentAttemptRound

    val previousAttempts = exercise.attempts
        .filter { it.attemptNumber < currentAttemptRound }
        .sortedBy { it.attemptNumber }

    val answered = hasAnsweredCurrentAttempt


    val type = exercise.type
    var showCorrectAnswer by rememberSaveable(index, currentAttemptRound) {
        mutableStateOf(false)
    }
    val startedAt = remember(exercise.question) {
        TimeSource.Monotonic.markNow()
    }
    var retryError by rememberSaveable(index) { mutableStateOf(false) }

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

            Text(
                text = "$excersize_level : ${difficultyLabel(exercise.difficulty)}",
                fontSize = 12.sp,
                color = AppColors.GrayDark
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            HintExplainSolutionRow(
                showHint = showHint,
                showExplanation = showExplanation,
                showSolution = showSolution,
                onToggleHint = { showHint = !showHint },
                onToggleExplanation = {
                    if (answered) {
                        showExplanation = !showExplanation
                    }
                },
                onToggleSolution = { showSolution = !showSolution },
                type = type,
            )

        }

        Text(
            text = "$exersize_has: ${exercise.maxPoints} $exersize_has_grades",
            fontSize = 12.sp,
            color = AppColors.GrayDark
        )
        Spacer(Modifier.height(4.dp))

        if (hasAnsweredCurrentAttempt && latestAttempt != null) {

            Spacer(Modifier.height(spacing8))

            val isCorrect = latestAttempt.isCorrect
            val isPartial =
                latestAttempt.gradingStatus.equals(
                    "partial",
                    ignoreCase = true
                )

            val feedbackColor = when {
                isCorrect -> Color(0xFF2E7D32)
                isPartial -> Color(0xFF8A6500)
                else -> Color(0xFFC62828)
            }

            val feedbackBackground = when {
                isCorrect -> Color(0xFFE6F4EA)
                isPartial -> Color(0xFFFFF8E1)
                else -> Color(0xFFFFE8E8)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        feedbackBackground,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = SmartTutorStrings.exercise_attempt_label(
                        currentAttemptRound
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = feedbackColor
                )
                Text(
                    text = when {
                        isCorrect ->
                            SmartTutorStrings.exercise_correct_feedback
                        isPartial ->
                            SmartTutorStrings.exercise_partial_feedback
                        else ->
                            SmartTutorStrings.exercise_incorrect_feedback
                    },
                    fontWeight = FontWeight.Bold,
                    color = feedbackColor
                )

                Text(
                    text = SmartTutorStrings.exercise_points_earned(
                        latestAttempt.awardedPoints,
                        exercise.maxPoints
                    ),
                    fontSize = 13.sp
                )

                if (
                    exercise.type != ExerciseType.OPEN &&
                    exercise.explanation.isNotBlank()
                ) {
                    ExerciseRichItem(
                        text = exercise.explanation
                    )
                }
            }

            Spacer(Modifier.height(spacing8))

        }

        if (previousAttempts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Color(0xFFE0E0E0),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = SmartTutorStrings.exercise_previous_attempts,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                previousAttempts.forEach { attempt ->
                    Text(
                        text = "${SmartTutorStrings.exercise_attempt_label(attempt.attemptNumber)}: " +
                                "${attempt.awardedPoints}/${exercise.maxPoints} " +
                                exersize_has_grades,
                        fontSize = 12.sp,
                        color = AppColors.GrayDark
                    )
                }
            }
        }

        if (!hasAnsweredCurrentAttempt && previousAttempts.isNotEmpty()) {
            Text(
                text = SmartTutorStrings.exercise_not_answered_yet,
                fontSize = 12.sp,
                color = AppColors.GrayDark
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
                            if (canAnswerNow) {
                                showCorrectAnswer = true
                                selectedIndex = optIndex
                                onExerciseAnswered(
                                    index,
                                    ExerciseType.MCQ,
                                    opt,
                                    correctAnswerText(exercise),
                                    clickedIsCorrect,
                                    startedAt.elapsedNow().inWholeMilliseconds,
                                    exercise.question,
                                    exercise.maxPoints,
                                )
                            } else retryError = true
                        }
                    )
                }
            }

            ExerciseType.TRUE_FALSE -> {
                val trueLabel = exercise.options.getOrNull(0) ?: "True"
                val falseLabel = exercise.options.getOrNull(1) ?: "False"


                val correctAnswer = exercise.solution.trim()

                val normalizedCorrectAnswer = (
                        exercise.answerIndex
                            ?.let { exercise.options.getOrNull(it) }
                            ?: correctAnswer
                        ).trim().lowercase().removeSuffix(".").trim()

                val trueCorrect = normalizedCorrectAnswer in setOf(
                    "true", "σωστό", "σωστο"
                )

                val falseCorrect = normalizedCorrectAnswer in setOf(
                    "false", "λάθος", "λαθος"
                )


                OptionRow(
                    label = "",
                    text = trueLabel,
                    selected = tfSelected == true,
                    isCorrect = tfSelected == true && trueCorrect,
                    isWrong = tfSelected == true && !trueCorrect,
                    onClick = {
                        if (canAnswerNow) {
                            showCorrectAnswer = true
                            tfSelected = true
                            onExerciseAnswered(
                                index,
                                ExerciseType.TRUE_FALSE,
                                trueLabel,
                                correctAnswer,
                                trueCorrect,
                                startedAt.elapsedNow().inWholeMilliseconds,
                                exercise.question,
                                exercise.maxPoints,
                            )
                        } else retryError = true
                    }
                )

                OptionRow(
                    label = "",
                    text = falseLabel,
                    selected = tfSelected == false,
                    isCorrect = tfSelected == false && falseCorrect,
                    isWrong = tfSelected == false && !falseCorrect,
                    onClick = {
                        if (canAnswerNow) {
                            showCorrectAnswer = true
                            tfSelected = false
                            onExerciseAnswered(
                                index,
                                ExerciseType.TRUE_FALSE,
                                falseLabel,
                                correctAnswer,
                                falseCorrect,
                                startedAt.elapsedNow().inWholeMilliseconds,
                                exercise.question,
                                exercise.maxPoints,
                            )
                        } else retryError = true
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
                        if (canAnswerNow) {
                            onAskTutorForOpenExercise(
                                index,
                                exercise.question,
                                openAnswer,
                                exercise.solution,
                                exercise.maxPoints
                            )
                            openAnswer = ""
                        } else retryError = true
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
                                exercise.question,
                                exercise.maxPoints,
                            )
                        }
                    )

                }
            }

        }

        if (isTemporarilyLocked) {
            Spacer(Modifier.height(spacing12))

            ExerciseLockedInfo()
        }

        if (showCorrectAnswer && hasAnsweredCurrentAttempt) {
            CorrectAnswerBox(
                answer = correctAnswerText(exercise)
            )
        }
        if (retryError) {
            Spacer(Modifier.height(spacing4))
            Text(
                text = info_retry,
                fontSize = 12.sp,
                color = Color.Red
            )
        }
        if (showHint && exercise.hint.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = hint_tab, text = exercise.hint)
        }
        if (
            hasAnsweredCurrentAttempt &&
                    showExplanation &&
                    exercise.explanation.isNotBlank()
        ) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = explanation_tab, text = exercise.explanation)
        }

        if (showSolution && exercise.solution.isNotBlank() && exercise.type == ExerciseType.OPEN) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = solution_tab, text = exercise.solution)
        }
    }
}

@Composable
private fun CorrectAnswerBox(
    answer: String,
) {
    Spacer(Modifier.height(spacing4))

    Text(
        text = "$the_correct_answer_is $answer",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF2E7D32),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6F4EA), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    )

    Spacer(Modifier.height(spacing8))
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
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = AppColors.Purple,
            contentColor = AppColors.White,
        )
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
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
        else -> {
            excersize_easy
        }
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

@Composable
private fun ExerciseLockedInfo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = 1.dp,
                color = Color(0xFFF9A825),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = cannot_answer_this_time,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6D4C00),
        )

        Text(
            text = must_completed_this_attemp_excersizes,
            fontSize = 12.sp,
            color = Color(0xFF6D4C00),
        )

        Text(
            text = progress_unit_not_completed_see_answers,
            fontSize = 12.sp,
            color = Color(0xFF6D4C00),
        )
    }
}
