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
import org.example.project.utils.nonNull

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
