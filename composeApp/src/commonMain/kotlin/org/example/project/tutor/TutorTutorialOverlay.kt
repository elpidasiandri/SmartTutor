package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors
import org.example.project.strings.SmartTutorStrings

enum class TutorTutorialStep {
    NEW_CHAT,
    UPLOAD_PDF,
    MY_PROGRESS
}

@Composable
fun TutorTutorialOverlay(
    step: TutorTutorialStep,
    targetBounds: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    if (targetBounds == null) {
        return
    }
    val title = when (step) {
        TutorTutorialStep.NEW_CHAT ->
            SmartTutorStrings.tutorial_new_chat_title

        TutorTutorialStep.UPLOAD_PDF ->
            SmartTutorStrings.tutorial_upload_title

        TutorTutorialStep.MY_PROGRESS ->
            SmartTutorStrings.tutorial_progress_title
    }

    val description = when (step) {
        TutorTutorialStep.NEW_CHAT ->
            SmartTutorStrings.tutorial_new_chat_description

        TutorTutorialStep.UPLOAD_PDF ->
            SmartTutorStrings.tutorial_upload_description

        TutorTutorialStep.MY_PROGRESS ->
            SmartTutorStrings.tutorial_progress_description
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Σκοτεινό background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // Highlight στην πραγματική θέση του New Chat.
        Box(
            modifier = Modifier
                .offset(
                    x = targetBounds.left.dp,
                    y = targetBounds.top.dp
                )
                .width(targetBounds.width.dp)
                .height(targetBounds.height.dp)
                .border(
                    width = 3.dp,
                    color = AppColors.Purple,
                    shape = RoundedCornerShape(12.dp)
                )
        )

        // Tooltip δίπλα στο πραγματικό target.
        Card(
            modifier = Modifier
                .offset(
                    x = when (step) {
                        TutorTutorialStep.NEW_CHAT ->
                            (targetBounds.right + 24f).dp

                        TutorTutorialStep.UPLOAD_PDF ->
                            (targetBounds.left + 40f).dp


                        TutorTutorialStep.MY_PROGRESS ->
                            (targetBounds.right + 24f).dp
                    },
                    y = when (step) {
                        TutorTutorialStep.NEW_CHAT ->
                            targetBounds.top.dp

                        TutorTutorialStep.UPLOAD_PDF ->
                            (targetBounds.top - 230f).dp

                        TutorTutorialStep.MY_PROGRESS ->
                            (targetBounds.top - 160f).dp
                    }
                )
                .width(320.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (step == TutorTutorialStep.MY_PROGRESS) {

                    Button(onClick = onNext) {
                        Text(SmartTutorStrings.tutorial_finish)
                    }

                } else {

                    Button(onClick = onNext) {
                        Text(SmartTutorStrings.tutorial_next)
                    }

                    TextButton(onClick = onSkip) {
                        Text(SmartTutorStrings.tutorial_skip)
                    }
                }
            }
        }
    }
}
