package org.example.project.tutor.mappers.chatHistory

import kotlinx.datetime.Clock
import org.example.project.tutor.mappers.learningUnits.toExerciseType
import org.example.project.tutor.models.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.models.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitExerciseStatsDomain
import org.example.project.tutor.models.domainModels.learningUnits.QuotationDomain
import org.example.project.tutor.models.domainModels.learningUnits.UnitImageDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.models.domainModels.tutorChat.LUStatusDomain
import org.example.project.tutor.models.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.models.domainModels.tutorChat.LearningUnitInstanceDomain
import org.example.project.tutor.models.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.models.domainModels.tutorChat.SenderType
import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseAttemptDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.QuotationDto
import org.example.project.tutor.networkModel.uploadPdf.UnitImageDto
import org.example.project.utils.nonNull
import kotlin.uuid.ExperimentalUuidApi

fun List<UserChatDto>.toChatHistoryDomain(): ChatHistoryDomain {
    return ChatHistoryDomain(
        subjects = this.map { chat ->

            val units = chat.result?.learningUnits
                ?: chat.partialResult?.learningUnits
                ?: emptyList()

            val title = chat.result?.prototypeTitle?.takeIf { it.isNotBlank() }
                ?: chat.partialResult
                    ?.prototypeTitle
                    ?.takeIf { it.isNotBlank() } ?: chat.title

            SubjectHistoryDomain(
                subjectId = chat.chatId,
                title = title,
                learningUnits = units.map {
                    it.toDomain()
                }
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
        exercises = exercises.map { it.toDomain() },
        isRead = isRead ?: false,
        totalPoints = totalPoints.nonNull
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

fun ChatMessageDto.toDomain(): ChatMessageDomain {
    val normalizedSender = when (sender.lowercase()) {
        "user" -> SenderType.USER.name
        "ai" -> SenderType.AI.name
        "system" -> SenderType.AI.name
        else -> SenderType.AI.name
    }

    return ChatMessageDomain(
        id = messageId,
        sender = normalizedSender,
        content = text,
        timestamp = createdAtEpochMillis,
        imagePath = emptyList(),
        unitNumber = unitNumber,
        unitTitle = unitTitle,
    )
}


fun buildChatSessionForSelectedUnit(
    fileName: String,
    unit: LearningUnitContentDomain,
    allStoredMessages: List<ChatMessageDomain>,
): ChatSessionStartDomain {
    val ts = Clock.System.now().toEpochMilliseconds()

    val introText = buildString {
        appendLine(unit.title)
        appendLine()
        appendLine(unit.text)
    }.trim()

    val introMessage = if (introText.isNotBlank()) {
        listOf(
            ChatMessageDomain(
                sender = SenderType.AI.name,
                content = introText,
                timestamp = ts,
                imagePath = unit.images,
                id = "intro_unit_${unit.numbering}"
            )
        )
    } else emptyList()

    val unitMessages = messagesForUnit(allStoredMessages, unit)

    val allMessages = (introMessage + unitMessages)
        .distinctBy { it.id }

    return ChatSessionStartDomain(
        material = MaterialDomain(
            fileUploaded = true,
            fileName = fileName,
            canUpload = true,
            totalUnits = 0
        ),
        learningUnit = LearningUnitDomain(
            title = unit.title,
            orderIndex = unit.numbering,
            quizAvailable = true,
            analysisCompleted = true,
            quizCompleted = false,
        ),
        instance = LearningUnitInstanceDomain(
            status = LUStatusDomain.ACTIVE,
            progressPercent = 0,
            startedAt = ts,
            lastUpdatedAt = ts,
            canContinue = true,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            chatSessionId = "session_unit_${unit.numbering}",
            createNewSession = false,
            messages = allMessages
        )
    )
}

private fun messagesForUnit(
    allMessages: List<ChatMessageDomain>,
    unit: LearningUnitContentDomain,
): List<ChatMessageDomain> {
    return allMessages.filter { msg ->
        msg.unitNumber == unit.numbering || msg.unitTitle == unit.title
    }
}

@OptIn(ExperimentalUuidApi::class)
fun buildChatSessionFromStoredChat(
    chat: UserChatDto,
    units: List<LearningUnitContentDomain>,
    activeIndex: Int = 0,
): ChatSessionStartDomain {
    val total = units.size
    val lu = units.getOrNull(activeIndex)
    val ts = Clock.System.now().toEpochMilliseconds()

    val introText = buildString {
        appendLine(lu?.title.orEmpty())
        appendLine()
        appendLine(lu?.text.orEmpty())
    }.trim()

    val introMessage = if (introText.isNotBlank()) {
        listOf(
            ChatMessageDomain(
                sender = SenderType.AI.name,
                content = introText,
                timestamp = ts,
                type = "TEXT",
                imagePath = lu?.images.orEmpty(),
                id = "intro_${chat.chatId}_${lu?.numbering ?: activeIndex}"
            )
        )
    } else {
        emptyList()
    }

    val storedMessagesForActiveUnit = chat.messages
        .sortedBy { it.createdAtEpochMillis }
        .filter { msg ->
            msg.unitNumber == lu?.numbering
        }
        .map { it.toDomain() }

    val allMessages = (introMessage + storedMessagesForActiveUnit)
        .distinctBy { it.id }

    return ChatSessionStartDomain(
        material = MaterialDomain(
            fileUploaded = true,
            fileName = chat.title,
            canUpload = true,
            totalUnits = total
        ),
        learningUnit = LearningUnitDomain(
            title = lu?.title.orEmpty(),
            orderIndex = lu?.numbering ?: activeIndex,
            quizAvailable = true,
            analysisCompleted = true,
            quizCompleted = false,
        ),
        instance = LearningUnitInstanceDomain(
            status = LUStatusDomain.ACTIVE,
            progressPercent = 0,
            startedAt = ts,
            lastUpdatedAt = ts,
            canContinue = true,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            chatSessionId = chat.chatId,
            createNewSession = false,
            messages = allMessages
        )
    )
}

fun ExerciseDto.toDomain(): ExerciseDomain {
    return ExerciseDomain(
        type = type.toExerciseType(),
        difficulty = difficulty.nonNull,
        estimatedMinutes = estimatedMinutes.nonNull,
        question = question.nonNull,
        options = options,
        answerIndex = answerIndex,
        hint = hint.nonNull,
        explanation = explanation.nonNull,
        solution = solution.nonNull,
        maxPoints = maxPoints.nonNull,
        attempts = attempts.map {
            it.toDomain()
        },
    )
}

fun ExerciseAttemptDto.toDomain(): LearningUnitExerciseStatsDomain {
    return LearningUnitExerciseStatsDomain(
        attemptNumber = attemptNumber,
        awardedPoints = awardedPoints,
        gradingStatus = gradingStatus,
        isCorrect = isCorrect,
        selectedAnswer = selectedAnswer,
        answeredAtEpochMillis = answeredAtEpochMillis
    )
}