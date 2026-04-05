package org.example.project.tutor.mappers.chatHistory

import org.example.project.tutor.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.tutor.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.domainModels.learningUnits.ExerciseType
import org.example.project.tutor.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.domainModels.learningUnits.QuotationDomain
import org.example.project.tutor.domainModels.learningUnits.UnitImageDomain
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.QuotationDto
import org.example.project.tutor.networkModel.uploadPdf.UnitImageDto
import org.example.project.utils.nonNull

fun List<UserChatDto>.toChatHistoryDomain(): ChatHistoryDomain {
    return ChatHistoryDomain(
        subjects = this.map { chat ->
            val units = chat.result?.learningUnits
                ?: chat.partialResult?.learningUnits
                ?: emptyList()

            SubjectHistoryDomain(
                subjectId = chat.chatId,
                title = chat.title,
                learningUnits = units.map { it.toDomain() }
            )
        }
    )
}

fun LearningUnitDto.toDomain(): LearningUnitContentDomain {
    return LearningUnitContentDomain(
        title = title.nonNull,
        numbering = numbering.nonNull,
        text = textInfo.nonNull,
        images = images.map { it.toDomain() },
        quotations = quotations.map { it.toDomain() },
        exercises = exercises.map { it.toDomain() }
    )
}
fun UnitImageDto.toDomain(): UnitImageDomain {
    return UnitImageDomain(
        relativePath = relativePath.nonNull,
        caption = caption.nonNull,
        prompt = prompt.nonNull,
        numbering = numbering.nonNull
    )
}

fun QuotationDto.toDomain(): QuotationDomain {
    return QuotationDomain(
        text = text.nonNull,
        numbering = numbering.nonNull
    )
}

fun ExerciseDto.toDomain(): ExerciseDomain {
    return ExerciseDomain(
        type = runCatching { ExerciseType.valueOf(type) }.getOrDefault(ExerciseType.NONE),
        difficulty = difficulty.nonNull,
        estimatedMinutes = estimatedMinutes.nonNull,
        question = question.nonNull,
        options = options,
        answerIndex = answerIndex,
        hint = hint.nonNull,
        explanation = explanation.nonNull,
        solution = solution.nonNull
    )
}