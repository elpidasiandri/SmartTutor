package org.example.project.tutor.excercise

import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun ExercisesTab(
    modifier: Modifier = Modifier,
    exercises: List<ExerciseDomain>,
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
        itemsIndexed(exercises) { index, ex ->
            ExerciseCard(
                index = index,
                exercise = ex
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    index: Int,
    exercise: ExerciseDomain,
) {
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }

    // user can answer on this field
    var selectedIndex by remember { mutableStateOf<Int?>(null) }   // MCQ
    var tfSelected by remember { mutableStateOf<Boolean?>(null) }  // TRUE/FALSE
    var openAnswer by remember { mutableStateOf("") }              // OPEN

    val type = exercise.type

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(spacing12)
    ) {
        // Header: "Exercise 1"
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
            )
        }

        Spacer(Modifier.height(spacing8))

        // Question
        ExerciseRichItem(text = exercise.question)
        Spacer(Modifier.height(spacing8))

        when (type) {
            ExerciseType.MCQ -> {
                // options A,B,C, D...
                exercise.options.forEachIndexed { optIndex, opt ->
                    val letter = ('A' + optIndex).toString()
                    OptionRow(
                        label = "$letter)",
                        text = opt,
                        selected = selectedIndex == optIndex,
                        onClick = { selectedIndex = optIndex }
                    )
                }
            }
            ExerciseType.TRUE_FALSE -> {
                // true false options
                val trueLabel = exercise.options.getOrNull(0) ?: "True"
                val falseLabel = exercise.options.getOrNull(1) ?: "False"

                OptionRow(
                    label = "",
                    text = trueLabel,
                    selected = tfSelected == true,
                    onClick = { tfSelected = true }
                )
                OptionRow(
                    label = "",
                    text = falseLabel,
                    selected = tfSelected == false,
                    onClick = { tfSelected = false }
                )
            }
            ExerciseType.OPEN -> {
                OutlinedTextField(
                    value = openAnswer,
                    onValueChange = { openAnswer = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your answer...") }
                )
            }
            else -> {
                exercise.options.forEachIndexed { optIndex, opt ->
                    val letter = ('A' + optIndex).toString()
                    OptionRow(
                        label = "$letter)",
                        text = opt,
                        selected = selectedIndex == optIndex,
                        onClick = { selectedIndex = optIndex }
                    )
                }
            }
        }

        // Expandable sections
        if (showHint && exercise.hint.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Hint", text = exercise.hint)
        }

        if (showExplanation && exercise.explanation.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Explanation", text = exercise.explanation)
        }

        if (showSolution && exercise.solution.isNotBlank()) {
            Spacer(Modifier.height(spacing8))
            ExpandSection(title = "Solution", text = exercise.solution)
        }
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

        SmallToggleChip(
            text = "Solution",
            selected = showSolution,
            onClick = onToggleSolution
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
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFFEDE7F6) else Color(0xFFF6F6F6)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotBlank()) {
            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
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
