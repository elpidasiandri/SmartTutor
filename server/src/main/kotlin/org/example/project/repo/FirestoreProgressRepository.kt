package org.example.project.repo

import com.google.cloud.firestore.Firestore
import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.networkModel.LearningUnitExerciseStatsDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto
import org.example.project.myProgress.repo.IProgressRepository

class FirestoreProgressRepository(
    private val db: Firestore
) : IProgressRepository {

    private val usersCol = db.collection("users")

    override fun upsertChatProgress(
        userId: String,
        progress: ChatProgressDto
    ) {
        val docRef = usersCol
            .document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .document(progress.chatId)

        docRef.set(
            mapOf(
                "chatId" to progress.chatId,
                "userId" to progress.userId,
                "documentId" to progress.documentId,
                "title" to progress.title,
                "totalUnits" to progress.totalUnits,
                "currentUnitIndex" to progress.currentUnitIndex,
                "selectedTab" to progress.selectedTab,
                "messagesSentCount" to progress.messagesSentCount,
                "availableExercisesCount" to progress.availableExercisesCount,
                "lastActivityEpochMillis" to progress.lastActivityEpochMillis,
                "progressPercent" to progress.progressPercent
            )
        ).get()
    }


    override fun incrementMessageCount(
        userId: String,
        chatId: String,
        delta: Int
    ) {
        val docRef = usersCol
            .document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .document(chatId)

        db.runTransaction { tx ->
            val snap = tx.get(docRef).get()
            val current = (snap.getLong("messagesSentCount") ?: 0L).toInt()
            tx.update(
                docRef,
                mapOf(
                    "messagesSentCount" to (current + delta),
                    "lastActivityEpochMillis" to System.currentTimeMillis()
                )
            )
        }.get()
    }

    override fun updateOverview(userId: String) {
        val chatsSnap = usersCol.document(userId)
            .collection("chats")
            .get()
            .get()

        val progressSnap = usersCol.document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .get()
            .get()

        val totalChats = chatsSnap.documents.size
        val totalDocuments = chatsSnap.documents
            .mapNotNull { it.getString("documentId") }
            .distinct()
            .size

        val totalMessagesSent = progressSnap.documents.sumOf {
            (it.getLong("messagesSentCount") ?: 0L).toInt()
        }

        val lastActivityEpochMillis = progressSnap.documents
            .mapNotNull { it.getLong("lastActivityEpochMillis") }
            .maxOrNull() ?: 0L

        val overviewRef = usersCol.document(userId)
            .collection("progress")
            .document("overview")

        overviewRef.set(
            mapOf(
                "userId" to userId,
                "totalChats" to totalChats,
                "totalDocuments" to totalDocuments,
                "totalMessagesSent" to totalMessagesSent,
                "lastActivityEpochMillis" to lastActivityEpochMillis,
                "streakDays" to 1
            )
        ).get()
    }

    override fun getOverview(userId: String): UserProgressOverviewDto? {
        val doc = usersCol.document(userId)
            .collection("progress")
            .document("overview")
            .get()
            .get()

        if (!doc.exists()) return null

        return UserProgressOverviewDto(
            userId = doc.getString("userId") ?: "",
            totalChats = (doc.getLong("totalChats") ?: 0L).toInt(),
            totalDocuments = (doc.getLong("totalDocuments") ?: 0L).toInt(),
            totalMessagesSent = (doc.getLong("totalMessagesSent") ?: 0L).toInt(),
            lastActivityEpochMillis = doc.getLong("lastActivityEpochMillis") ?: 0L,
            streakDays = (doc.getLong("streakDays") ?: 0L).toInt()
        )
    }

    override fun getChatProgressList(userId: String): List<ChatProgressDto> {
        val snap = usersCol.document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .get()
            .get()

        return snap.documents.map {
            ChatProgressDto(
                chatId = it.getString("chatId") ?: it.id,
                userId = it.getString("userId") ?: "",
                documentId = it.getString("documentId") ?: "",
                title = it.getString("title") ?: "",
                totalUnits = (it.getLong("totalUnits") ?: 0L).toInt(),
                currentUnitIndex = (it.getLong("currentUnitIndex") ?: 0L).toInt(),
                selectedTab = it.getString("selectedTab") ?: "Learning",
                messagesSentCount = (it.getLong("messagesSentCount") ?: 0L).toInt(),
                availableExercisesCount = (it.getLong("availableExercisesCount") ?: 0L).toInt(),
                lastActivityEpochMillis = it.getLong("lastActivityEpochMillis") ?: 0L,
                progressPercent = (it.getLong("progressPercent") ?: 0L).toInt()
            )
        }.sortedByDescending { it.lastActivityEpochMillis }
    }

    override fun deleteChatProgress(userId: String, chatId: String) {
        usersCol.document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .document(chatId)
            .delete()
            .get()

        updateOverview(userId)
    }

    override suspend fun saveExerciseAnswer(
        userId: String,
        answer: ExerciseAnswerProgressDto,
    ) {
        val docId = "${answer.chatId}_${answer.unitIndex}_${answer.exerciseIndex}"

        usersCol.document(userId)
            .collection("progress")
            .document("exerciseAnswers")
            .collection("items")
            .document(docId)
            .set(
                mapOf(
                    "chatId" to answer.chatId,
                    "userId" to userId,
                    "unitIndex" to answer.unitIndex,
                    "unitTitle" to answer.unitTitle,
                    "exerciseIndex" to answer.exerciseIndex,
                    "exerciseType" to answer.exerciseType,
                    "question" to answer.question,
                    "selectedAnswer" to answer.selectedAnswer,
                    "correctAnswer" to answer.correctAnswer,
                    "isCorrect" to answer.isCorrect,
                    "timeToAnswerMs" to answer.timeToAnswerMs,
                    "answeredAtEpochMillis" to answer.answeredAtEpochMillis
                )
            )
            .get()

        updateOverview(userId)
    }

    override suspend fun getExerciseStats(
        userId: String
    ): List<LearningUnitExerciseStatsDto> {
        val snap = usersCol.document(userId)
            .collection("progress")
            .document("exerciseAnswers")
            .collection("items")
            .get()
            .get()

        return snap.documents
            .groupBy {
                val unitIndex = (it.getLong("unitIndex") ?: 0L).toInt()
                val unitTitle = it.getString("unitTitle") ?: ""
                unitIndex to unitTitle
            }
            .map { (unit, docs) ->
                val correct = docs.count { it.getBoolean("isCorrect") == true }
                val wrong = docs.count { it.getBoolean("isCorrect") == false }

                LearningUnitExerciseStatsDto(
                    unitIndex = unit.first,
                    unitTitle = unit.second,
                    correctCount = correct,
                    wrongCount = wrong,
                    totalAnswered = correct + wrong
                )
            }
            .sortedBy { it.unitIndex }
    }
}