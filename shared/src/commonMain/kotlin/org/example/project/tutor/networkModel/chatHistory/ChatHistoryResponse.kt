package org.example.project.tutor.networkModel.chatHistory
data class ChatHistoryResponse(
    val subjects: List<SubjectHistoryResponse?>?,
)

data class SubjectHistoryResponse(
    val title: String?,
    val subjectId: String?,
    val learningUnits: List<LearningUnitHistoryResponse?>?,
    val latestChatId: String?,
)

data class LearningUnitHistoryResponse(
    val luId: String?,
    val luTitle: String?,
    val materialId: String?,
    val chatId: String?
)