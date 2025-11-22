package org.example.project.tutor.domainModels.chatHistory

data class ChatHistoryItemModel(
    val title: String,
    val subItems: List<String> = emptyList()
)