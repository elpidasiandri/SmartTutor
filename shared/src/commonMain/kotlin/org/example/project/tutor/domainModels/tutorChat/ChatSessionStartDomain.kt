package org.example.project.tutor.domainModels.tutorChat

import org.example.project.tutor.domainModels.learningUnits.UnitImageDomain
import org.example.project.utils.EmptyValues.EMPTY_BOOLEAN
import org.example.project.utils.EmptyValues.EMPTY_INT
import org.example.project.utils.EmptyValues.EMPTY_LONG
import org.example.project.utils.EmptyValues.EMPTY_STRING

data class ChatSessionStartDomain(
    val material: MaterialDomain = MaterialDomain(),
    val learningUnit: LearningUnitDomain = LearningUnitDomain(),
    val instance: LearningUnitInstanceDomain = LearningUnitInstanceDomain(),
    val chat: ChatDomain = ChatDomain(),
)

data class MaterialDomain(
    val materialId: String = EMPTY_STRING,
    val fileUploaded: Boolean = EMPTY_BOOLEAN,
    val fileName: String = EMPTY_STRING,
    val canUpload: Boolean = EMPTY_BOOLEAN,
    val totalUnits: Int = EMPTY_INT,
)

data class LearningUnitDomain(
    val title: String = EMPTY_STRING,
    val orderIndex: Int = EMPTY_INT,
    val quizAvailable: Boolean = EMPTY_BOOLEAN,
    val analysisCompleted: Boolean = EMPTY_BOOLEAN,
    val quizCompleted: Boolean = EMPTY_BOOLEAN
)

enum class LUStatusDomain { ACTIVE, COMPLETED, INACTIVE, NONE }

data class LearningUnitInstanceDomain(
    val instanceId: String = EMPTY_STRING,
    val status: LUStatusDomain = LUStatusDomain.NONE,
    val progressPercent: Int = EMPTY_INT,
    val startedAt: Long = EMPTY_LONG,
    val lastUpdatedAt: Long = EMPTY_LONG,
    val canContinue: Boolean = EMPTY_BOOLEAN,
    val canRestart: Boolean = EMPTY_BOOLEAN,
    val isLocked: Boolean = EMPTY_BOOLEAN,
)

data class ChatDomain(
    val chatSessionId: String = EMPTY_STRING,
    val createNewSession: Boolean = EMPTY_BOOLEAN,
    val messages: List<ChatMessageDomain> = emptyList(),
)

data class ChatMessageDomain(
    val sender: String = EMPTY_STRING,
    val content: String = EMPTY_STRING,
    val timestamp: Long = EMPTY_LONG,
    val type: String = EMPTY_STRING,
    val id: String = EMPTY_STRING, //todo fix
    val imagePath: List<UnitImageDomain> = emptyList(),
)

enum class SenderType {
    USER,
    AI
}
