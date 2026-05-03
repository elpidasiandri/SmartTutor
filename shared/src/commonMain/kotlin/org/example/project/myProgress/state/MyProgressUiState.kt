package org.example.project.myProgress.state

data class MyProgressState(
    val loading: Boolean = false,
    val error: String? = null,
    val showCustomMessage: Boolean = false,
    val message: String = "",
    val isError: Boolean = false,

    val totalChats: Int = 0,
    val totalUploadedPdfs: Int = 0,
    val totalQuestionsAsked: Int = 0,
    val totalExercisesCount: Int = 0,

    val currentSubjectTitle: String = "",
    val currentUnitIndex: Int = 0,
    val totalUnits: Int = 0,
    val selectedTab: String = "",
    val progressPercent: Int = 0,

    val lastActivityEpochMillis: Long = 0L,
    val lastActivityLabel: String = "",
    val streakDays: Int = 0,

    val recentChats: List<MyProgressChatItem> = emptyList(),

    val event: MyProgressEvents = MyProgressEvents.None,
    val uiEvent: MyProgressUiEvents = MyProgressUiEvents.None,
)

data class MyProgressChatItem(
    val chatId: String,
    val title: String,
    val progressPercent: Int,
    val lastActivityEpochMillis: Long = 0L
)

sealed class MyProgressEvents {
    data object None : MyProgressEvents()
    data object Loaded : MyProgressEvents()
}

sealed class MyProgressUiEvents {
    data object None : MyProgressUiEvents()
    data object LoadProgress : MyProgressUiEvents()
    data object ClearUiEvent : MyProgressUiEvents()
    data object InitializeStateAfterShowingMessage : MyProgressUiEvents()
    data class ShowErrorMessage(val message: String) : MyProgressUiEvents()
}
