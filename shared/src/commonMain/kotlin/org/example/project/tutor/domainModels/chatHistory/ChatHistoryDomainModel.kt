package org.example.project.tutor.domainModels.chatHistory

import org.example.project.utils.EmptyValues

data class ChatHistoryDomain(
    val subjects: List<SubjectHistoryDomain>,
)

data class SubjectHistoryDomain(
    val title: String = EmptyValues.EMPTY_STRING,
    val subjectId: String = EmptyValues.EMPTY_STRING,
    val learningUnits: List<LearningUnitHistoryDomain> = emptyList(),
)

data class LearningUnitHistoryDomain(
    val luId: String = EmptyValues.EMPTY_STRING,
    val luTitle: String = EmptyValues.EMPTY_STRING,
    val materialId: String = EmptyValues.EMPTY_STRING,
    val chatId: String = EmptyValues.EMPTY_STRING,
)