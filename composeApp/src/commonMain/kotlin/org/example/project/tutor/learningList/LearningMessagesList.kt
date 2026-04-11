package org.example.project.tutor.learningList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.dimens.Dimens.spacing8
import org.example.project.tutor.domainModels.tutorChat.ChatUiMessage
import org.example.project.tutor.domainModels.tutorChat.SenderType

@Composable
fun LearningMessagesList(
    modifier: Modifier,
    chatMessages: List<ChatUiMessage>,
    baseUrl: String
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    LazyColumn(modifier = modifier, state = listState) {
        items(
            items = chatMessages.distinctBy { it.id },
            key = { it.id }
        ) { item ->

            if (item.sender == SenderType.USER.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = item.text,
                        modifier = Modifier
                            .background(Color(0xFFD1FFD6), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LearningRichMessage(item.text)

                        if (item.imagePath.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            LearningUnitImages(
                                images = item.imagePath,
                                resolveUrl = { rel -> baseUrl.trimEnd('/') + "/" + rel.trimStart('/') }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing8))
        }
    }
}
