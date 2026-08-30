package org.example.project.tutor.state

import org.example.project.tutor.models.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.utils.EmptyValues

data class TutorState(
    val event: TutorEvents = TutorEvents.None,
    val uiEvent: TutorUiEvents = TutorUiEvents.None,
    val email: String = EmptyValues.EMPTY_STRING,
    val message: String = EmptyValues.EMPTY_STRING,
    val isError: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val showCustomMessage: Boolean = EmptyValues.EMPTY_BOOLEAN,
    val loading: Boolean = true,
    val canGoNext: Boolean = true,
    val askTutor: Boolean = false,
    val error: String? = null,
    val session: ChatSessionStartDomain = ChatSessionStartDomain(),
    val currentUnit: LearningUnitContentDomain = LearningUnitContentDomain(),
    val chatHistory: ChatHistoryDomain = ChatHistoryDomain(),
    val allUnits: List<LearningUnitContentDomain> = emptyList(),
    val currentIndex: Int = 0,
    val subjectTitle: String = "",
    val unlockedCount: Int = 1,
    val selectedTab: TutorTab = TutorTab.Learning,
    val selectedExerciseIndex: Int = 0,
    val tabInfoMessage: String = "",
    val activeDocumentId: String = EmptyValues.EMPTY_STRING,
    val activeChatId: String = EmptyValues.EMPTY_STRING,
    val savedChats: List<UserChatDto> = emptyList(),
    val allChatMessages: List<ChatMessageDomain> = emptyList(),
    val exerciseTutorAnswers: Map<Int, String> = emptyMap(),
    val answeredExerciseKeys: Set<String> = emptySet(),
    val exercisesLoading: Boolean = false,
    val pdfProcessingProgress: Int? = null,
)

sealed class TutorUiEvents {
    data object None : TutorUiEvents()
    data object NewChat : TutorUiEvents()
    data object Logout : TutorUiEvents()
    data class SelectLearningUnit(val title: String, val subjectId: String) : TutorUiEvents()
    data object InitializeStateAfterShowingMessage : TutorUiEvents()
    data object ClearUiEvent : TutorUiEvents()
    data object NextUnit : TutorUiEvents()
    data object OpenMyProgress : TutorUiEvents()
    data class ShowErrorMessage(val message: String) : TutorUiEvents()
    data object LoadMoreExercises : TutorUiEvents()
    data class AskTutorForOpenExercise(
        val exerciseIndex: Int,
        val question: String,
        val userAnswer: String,
        val suggestedSolution: String,
        val maxPoints: Double
    ) : TutorUiEvents()
    data class OpenUnitSection(
        val title: String,
        val subjectId: String,
        val tab: TutorTab,
        val exerciseIndex: Int? = null
    ) : TutorUiEvents()
    data class ExerciseAnswered(
        val exerciseIndex: Int,
        val exerciseType: ExerciseType,
        val selectedAnswer: String,
        val correctAnswer: String,
        val isCorrect: Boolean,
        val timeToAnswerMs: Long,
        val question: String,
        val maxPoints: Double,
    ) : TutorUiEvents()

    data class SendMessage(val text: String) : TutorUiEvents()
    data class OpenExistingChat(val chatId: String) : TutorUiEvents()
    data class DeleteChat(val subjectId: String) : TutorUiEvents()
    data class UploadPdf(val file: PickedFile) : TutorUiEvents()
    data class ChangeTab(val tab: TutorTab) : TutorUiEvents()
}

sealed class TutorEvents {
    data object None : TutorEvents()
    data object Loaded : TutorEvents()
    data object NavigateToLogOut : TutorEvents()
    data object NavigateToMyProgress : TutorEvents()
}
