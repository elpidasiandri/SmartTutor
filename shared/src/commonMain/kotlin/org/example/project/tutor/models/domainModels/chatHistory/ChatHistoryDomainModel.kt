package org.example.project.tutor.models.domainModels.chatHistory

import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.utils.EmptyValues

data class ChatHistoryDomain(
    val subjects: List<SubjectHistoryDomain> = emptyList(),
)

data class SubjectHistoryDomain(
    val title: String = EmptyValues.EMPTY_STRING,
    val subjectId: String = EmptyValues.EMPTY_STRING,
    val learningUnits: List<LearningUnitContentDomain> = emptyList(),
)
