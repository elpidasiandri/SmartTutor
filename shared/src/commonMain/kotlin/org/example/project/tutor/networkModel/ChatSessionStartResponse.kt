package org.example.project.tutor.networkModel

import org.example.project.tutor.models.domainModels.learningUnits.UnitImageDomain


data class MaterialState(
    val materialId: String?,
    val fileUploaded: Boolean?,
    val fileName: String?,
    val canUpload: Boolean?, // user CAN upload only if no LU for this material
    val totalUnits: Int?,
)

data class LearningUnitState(
    val luId: String?,
    val title: String?,
    val orderIndex: Int?,
    val quizAvailable: Boolean?,
    val analysisCompleted: Boolean?, // analysis before quiz
    val quizCompleted: Boolean?,
    val nextAction: NextAction?,
)

enum class NextAction {
    CONTINUE_CHAT,     // LU is active, continue messages
    DO_ANALYSIS,       // Waiting for analysis by AI
    DO_QUIZ,           // Must complete quiz
    CHOOSE_NEXT_LU,    // analysis + quiz done → ready to move on
    FINISH_LU,         // user said "no more", LU becomes inactive
    None
}

enum class LUStatus { ACTIVE, COMPLETED, INACTIVE }

data class LearningUnitInstanceState(
    val instanceId: String?,
    val status: LUStatus?,
    val progressPercent: Int?,
    val startedAt: Long?,
    val lastUpdatedAt: Long?,
    val canContinue: Boolean?,   // true if ACTIVE
    val canRestart: Boolean?,    // true if COMPLETED
    val isLocked: Boolean?,      // true if FINISHED and cannot study again
)

data class ChatState(
    val chatSessionId: String?,
    val createNewSession: Boolean?,  // true if LU is completed or inactive
    val messages: List<ChatMessageState?>?,
)

data class ChatMessageState(
    val id: String?,
    val sender: String?,  // "USER", "AI"
    val content: String?,
    val timestamp: Long?,
    val type: String,    // "TEXT", "FILE", "MARKDOWN"
    val imagePath: List<UnitImageDomain>?,
)
