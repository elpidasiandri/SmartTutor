package org.example.project.tutor.mappers.chatHistory

import org.example.project.tutor.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.domainModels.chatHistory.LearningUnitHistoryDomain
import org.example.project.tutor.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.tutor.networkModel.chatHistory.ChatHistoryResponse
import org.example.project.utils.EmptyValues

fun ChatHistoryResponse.toDomain(): ChatHistoryDomain {
    return ChatHistoryDomain(
        subjects = this.subjects.orEmpty().map { subject ->
            SubjectHistoryDomain(
                title = subject?.title ?: EmptyValues.EMPTY_STRING,
                subjectId = subject?.subjectId ?: EmptyValues.EMPTY_STRING,
                learningUnits = subject?.learningUnits.orEmpty().map { lu ->
                    LearningUnitHistoryDomain(
                        luId = lu?.luId ?: EmptyValues.EMPTY_STRING,
                        materialId = lu?.materialId ?: EmptyValues.EMPTY_STRING,
                        chatId = lu?.chatId ?: EmptyValues.EMPTY_STRING
                    )
                }
            )
        }
    )
}


fun fakeChatHistory(): ChatHistoryDomain {
    val subjects = listOf(
        SubjectHistoryDomain(
            title = "Physics",
            subjectId = "subject_001",
            learningUnits = listOf(
                LearningUnitHistoryDomain(
                    luId = "lu_01",
                    luTitle = "Newton's Laws",
                    materialId = "material_001",
                    chatId = "1"
                ),
                LearningUnitHistoryDomain(
                    luId = "lu_02",
                    materialId = "material_002",
                    chatId = "2"
                )
            )
        ),
        SubjectHistoryDomain(
            title = "Math",
            subjectId = "subject_002",
            learningUnits = listOf(
                LearningUnitHistoryDomain(
                    luId = "lu_002",
                    luTitle = "matha maths",
                    materialId = "material_02",
                    chatId = "12"
                )
            )
        )
    )

    return ChatHistoryDomain(subjects)
}
