package org.example.project.tutor.mappers

import org.example.project.tutor.domainModels.tutorChat.ChatDomain
import org.example.project.tutor.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.domainModels.tutorChat.LUStatusDomain
import org.example.project.tutor.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.domainModels.tutorChat.LearningUnitInstanceDomain
import org.example.project.tutor.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.domainModels.tutorChat.NextActionDomain
import org.example.project.tutor.networkModel.ChatMessageState
import org.example.project.tutor.networkModel.ChatSessionStartResponse
import org.example.project.tutor.networkModel.ChatState
import org.example.project.tutor.networkModel.LearningUnitInstanceState
import org.example.project.tutor.networkModel.LearningUnitState
import org.example.project.tutor.networkModel.MaterialState
import org.example.project.utils.convertToLong
import org.example.project.utils.nonNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun ChatSessionStartResponse.toDomain() = ChatSessionStartDomain(
    material = this.material?.toDomain() ?: MaterialDomain(),
    learningUnit = this.learningUnit?.toDomain() ?: LearningUnitDomain(),
    instance = this.instance?.toDomain()?: LearningUnitInstanceDomain(),
    chat = this.chat?.toDomain()?: ChatDomain()
)

fun MaterialState.toDomain() = MaterialDomain(
    materialId = materialId.nonNull,
    fileUploaded = fileUploaded.nonNull,
    fileName = fileName.nonNull,
    canUpload = canUpload.nonNull,
    totalUnits = totalUnits.nonNull
)

fun LearningUnitState.toDomain() = LearningUnitDomain(
    luId = luId.nonNull,
    title = title.nonNull,
    orderIndex = orderIndex.nonNull,
    quizAvailable = quizAvailable.nonNull,
    analysisCompleted = analysisCompleted.nonNull,
    quizCompleted = quizCompleted.nonNull,
    nextAction = nextAction?.let { NextActionDomain.valueOf(it.name) } ?: NextActionDomain.NONE
)

fun LearningUnitInstanceState.toDomain() = LearningUnitInstanceDomain(
    instanceId = instanceId.nonNull,
    status = status?.let { LUStatusDomain.valueOf(it.name) } ?: LUStatusDomain.NONE,
    progressPercent = progressPercent.nonNull,
    startedAt = startedAt.nonNull,
    lastUpdatedAt = lastUpdatedAt.nonNull,
    canContinue = canContinue.nonNull,
    canRestart = canRestart.nonNull,
    isLocked = isLocked.nonNull
)

fun ChatState.toDomain() = ChatDomain(
    chatSessionId = chatSessionId.nonNull,
    createNewSession = createNewSession.nonNull,
    messages = messages
        ?.mapNotNull { it?.toDomain() }
        ?: emptyList()
)

fun ChatMessageState.toDomain() = ChatMessageDomain(
    id = id.nonNull,
    sender = sender.nonNull,
    content = content.nonNull,
    timestamp = timestamp.nonNull,
    type = type.nonNull
)

@OptIn(ExperimentalTime::class)
fun fakeChatSession(): ChatSessionStartDomain {
    return ChatSessionStartDomain(
        material = MaterialDomain(
            materialId = "material_001",
            fileUploaded = false,
            fileName = "physics.pdf",
            canUpload = false,
            totalUnits = 5
        ),
        learningUnit = LearningUnitDomain(
            luId = "lu_01",
            title = "Newton's Laws",
            orderIndex = 1,
            quizAvailable = true,
            analysisCompleted = false,
            quizCompleted = false,
            nextAction = NextActionDomain.CONTINUE_CHAT
        ),
        instance = LearningUnitInstanceDomain(
            instanceId = "instance_123",
            status = LUStatusDomain.ACTIVE,
            progressPercent = 20,
            startedAt = Clock.System.now().convertToLong(500000),
            lastUpdatedAt = Clock.System.now().convertToLong(0L),
            canContinue = true,
            canRestart = false,
            isLocked = false
        ),
        chat = ChatDomain(
            chatSessionId = "chat_999",
            createNewSession = false,
            messages = listOf(
                ChatMessageDomain(
                    id = "1",
                    sender = "AI",
                    content = "Hello! How can I help you today?",
                    timestamp = Clock.System.now().convertToLong(60000),
                    type = "TEXT"
                ),
                ChatMessageDomain(
                    id = "2",
                    sender = "USER",
                    content = "Can you give me an example?",
                    timestamp = Clock.System.now().convertToLong(30000),
                    type = "TEXT"
                ),
                ChatMessageDomain(
                    id = "3",
                    sender = "AI",
                    content = "Sure! Here is an example...",
                    timestamp = Clock.System.now().convertToLong(10000),
                    type = "TEXT"
                )
            )
        )
    )
}
