package org.example.project.tutor.domainModels

data class ChatHistoryItemModel(
    val title: String,
    val subItems: List<String> = emptyList()
)
