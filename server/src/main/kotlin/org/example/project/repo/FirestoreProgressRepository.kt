package org.example.project.repo

import com.google.cloud.firestore.Firestore
import kotlinx.serialization.json.Json
import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.networkModel.ExerciseQuestionStatsDto
import org.example.project.myProgress.networkModel.LearningUnitExerciseStatsDto
import org.example.project.myProgress.networkModel.UnitReadProgressDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto
import org.example.project.myProgress.repo.IProgressRepository
import org.example.project.tutor.networkModel.uploadPdf.ExerciseAttemptDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto

class FirestoreProgressRepository(
    private val db: Firestore,
) : IProgressRepository {

    private val usersCol = db.collection("users")

    override fun upsertChatProgress(
        userId: String,
        progress: ChatProgressDto,
    ) {
        val docRef = usersCol
            .document(userId)
            .collection("progress")
            .document("chats")
            .collection("items")
            .document(progress.chatId)
        val calculatedProgressPercent = calculateRealProgressPercent(
            userId = userId,
            progress = progress
        )

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
                "progressPercent" to calculatedProgressPercent,
                "units" to progress.units.map {
                    mapOf(
                        "unitIndex" to it.unitIndex,
                        "unitTitle" to it.unitTitle,
                        "isRead" to it.isRead
                    )
                }
            )
        ).get()
    }

    private fun calculateRealProgressPercent(
        userId: String,
        progress: ChatProgressDto,
    ): Int {
        val totalUnits = progress.totalUnits
        val totalExercises = progress.availableExercisesCount

        val totalTasks = totalUnits + totalExercises
        if (totalTasks == 0) return 0

        val readUnits = progress.units.count { it.isRead }

        val answersSnap = usersCol.document(userId)
            .collection("progress")
            .document("exerciseAnswers")
            .collection("items")
            .get()
            .get()

        val answeredUniqueExercises = answersSnap.documents
            .filter { it.getString("chatId") == progress.chatId }
            .map {
                val unitIndex = (it.getLong("unitIndex") ?: -1L).toInt()
                val exerciseIndex = (it.getLong("exerciseIndex") ?: -1L).toInt()
                "$unitIndex-$exerciseIndex"
            }
            .distinct()
            .count()

        val completedTasks = readUnits + answeredUniqueExercises.coerceAtMost(totalExercises)

        return ((completedTasks * 100) / totalTasks).coerceIn(0, 100)
    }

    override fun incrementMessageCount(
        userId: String,
        chatId: String,
        delta: Int,
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
            val unitsRaw = it.get("units") as? List<*>
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
                progressPercent = (it.getLong("progressPercent") ?: 0L).toInt(),
                units = unitsRaw?.mapNotNull { raw ->
                    val unit = raw as? Map<*, *> ?: return@mapNotNull null

                    UnitReadProgressDto(
                        unitIndex = (unit["unitIndex"] as? Long)?.toInt() ?: 0,
                        unitTitle = unit["unitTitle"] as? String ?: "",
                        isRead = unit["isRead"] as? Boolean ?: false
                    )
                } ?: emptyList(),
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
        val docRef = usersCol.document(userId)
            .collection("progress")
            .document("exerciseAnswers")
            .collection("items")
            .document("${answer.chatId}_${answer.unitIndex}_${answer.exerciseIndex}")

        val savedAttempts = db.runTransaction<List<ExerciseAttemptDto>> { tx ->
            val snap = tx.get(docRef).get()

            val oldAttempts = if (snap.exists()) {
                val raw = snap.get("attempts") as? List<*> ?: emptyList<Any>()
                raw.mapNotNull { mapToExerciseAttempt(it) }
            } else {
                emptyList()
            }

            val incoming = answer.attempts.lastOrNull()
                ?: error("Missing attempt")

            val newAttempt = incoming.copy(
                attemptNumber = oldAttempts.size + 1,
                answeredAtEpochMillis = answer.answeredAtEpochMillis
            )

            val allAttempts = oldAttempts + newAttempt
            val lastAttempt = allAttempts.last()

            tx.set(
                docRef,
                mapOf(
                    "chatId" to answer.chatId,
                    "userId" to userId,
                    "unitIndex" to answer.unitIndex,
                    "unitTitle" to answer.unitTitle,
                    "exerciseIndex" to answer.exerciseIndex,
                    "exerciseType" to answer.exerciseType,
                    "question" to answer.question,
                    "selectedAnswer" to lastAttempt.selectedAnswer,
                    "correctAnswer" to answer.correctAnswer,
                    "isCorrect" to lastAttempt.isCorrect,
                    "timeToAnswerMs" to answer.timeToAnswerMs,
                    "answeredAtEpochMillis" to lastAttempt.answeredAtEpochMillis,
                    "maxPoints" to answer.maxPoints,
                    "awardedPoints" to lastAttempt.awardedPoints,
                    "gradingStatus" to lastAttempt.gradingStatus,
                    "attempts" to allAttempts.map { it.toMap() },
                )
            )

            allAttempts
        }.get()

        updateExerciseAttemptsInChat(
            userId = userId,
            answer = answer,
            attempts = savedAttempts
        )

        val chatProgress = getChatProgressList(userId)
            .firstOrNull { it.chatId == answer.chatId }

        if (chatProgress != null) {
            val percent = calculateRealProgressPercent(userId, chatProgress)

            usersCol.document(userId)
                .collection("progress")
                .document("chats")
                .collection("items")
                .document(answer.chatId)
                .update("progressPercent", percent)
                .get()
        }

        updateOverview(userId)
    }

    private fun updateExerciseAttemptsInChat(
        userId: String,
        answer: ExerciseAnswerProgressDto,
        attempts: List<ExerciseAttemptDto>,
    ) {
        val chatRef = usersCol.document(userId)
            .collection("chats")
            .document(answer.chatId)

        db.runTransaction { tx ->
            val snap = tx.get(chatRef).get()
            if (!snap.exists()) return@runTransaction false

            fun updatePrototypeJson(field: String): String? {
                val raw = snap.getString(field).orEmpty()
                if (raw.isBlank()) return null
                val json = Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                }
                val old = json.decodeFromString(
                    LearningUnitsPrototypeDto.serializer(),
                    raw
                )

                if (answer.unitIndex !in old.learningUnits.indices) return null

                val updatedUnits = old.learningUnits.mapIndexed { unitIndex, unit ->
                    if (unitIndex != answer.unitIndex) return@mapIndexed unit

                    val updatedExercises = unit.exercises.mapIndexed { exerciseIndex, exercise ->
                        if (exerciseIndex == answer.exerciseIndex) {
                            exercise.copy(
                                attempts = attempts
                            )
                        } else {
                            exercise
                        }
                    }

                    unit.copy(exercises = updatedExercises)
                }

                val updated = old.copy(learningUnits = updatedUnits)

                return json.encodeToString(
                    LearningUnitsPrototypeDto.serializer(),
                    updated
                )
            }

            val updates = mutableMapOf<String, Any>(
                "updatedAtEpochMillis" to System.currentTimeMillis()
            )

            updatePrototypeJson("resultJson")?.let {
                updates["resultJson"] = it
            }

            updatePrototypeJson("partialResultJson")?.let {
                updates["partialResultJson"] = it
            }

            if (updates.size == 1) return@runTransaction false

            tx.update(chatRef, updates)
            true
        }.get()
    }

    fun mapToExerciseAttempt(raw: Any?): ExerciseAttemptDto? {
        val map = raw as? Map<*, *> ?: return null

        return ExerciseAttemptDto(
            attemptNumber = (map["attemptNumber"] as? Long)?.toInt()
                ?: (map["attemptNumber"] as? Int)
                ?: 1,
            awardedPoints = (map["awardedPoints"] as? Double)
                ?: (map["awardedPoints"] as? Long)?.toDouble()
                ?: 0.0,
            gradingStatus = map["gradingStatus"] as? String ?: "not_answered",
            isCorrect = map["isCorrect"] as? Boolean ?: false,
            selectedAnswer = map["selectedAnswer"] as? String ?: "",
            answeredAtEpochMillis = map["answeredAtEpochMillis"] as? Long ?: 0L
        )
    }

    private fun ExerciseAttemptDto.toMap(): Map<String, Any?> {
        return mapOf(
            "attemptNumber" to attemptNumber,
            "awardedPoints" to awardedPoints,
            "gradingStatus" to gradingStatus,
            "isCorrect" to isCorrect,
            "selectedAnswer" to selectedAnswer,
            "answeredAtEpochMillis" to answeredAtEpochMillis
        )
    }

    override suspend fun getExerciseStats(
        userId: String,
    ): List<LearningUnitExerciseStatsDto> {

        val snap = usersCol.document(userId)
            .collection("progress")
            .document("exerciseAnswers")
            .collection("items")
            .get()
            .get()

        val allAnswers = snap.documents.map { doc ->
            val attemptsRaw = doc.get("attempts") as? List<*> ?: emptyList<Any>()
            val attempt = mapToExerciseAttempt(doc.get("attempt"))
            val attempts = attemptsRaw.mapNotNull {
                mapToExerciseAttempt(it)
            }

            ExerciseAnswerProgressDto(
                chatId = doc.getString("chatId") ?: "",
                userId = doc.getString("userId") ?: userId,
                unitIndex = (doc.getLong("unitIndex") ?: 0L).toInt(),
                unitTitle = doc.getString("unitTitle") ?: "",
                exerciseIndex = (doc.getLong("exerciseIndex") ?: 0L).toInt(),
                exerciseType = doc.getString("exerciseType") ?: "",
                question = doc.getString("question") ?: "",
                selectedAnswer = doc.getString("selectedAnswer") ?: "",
                correctAnswer = doc.getString("correctAnswer") ?: "",
                isCorrect = doc.getBoolean("isCorrect") == true,
                timeToAnswerMs = doc.getLong("timeToAnswerMs") ?: 0L,
                answeredAtEpochMillis = doc.getLong("answeredAtEpochMillis") ?: 0L,
                maxPoints = doc.getDouble("maxPoints") ?: 0.0,
                attempts = attempts
            )
        }

        return allAnswers
            .groupBy { it.unitIndex to it.unitTitle }
            .map { (unit, unitAnswers) ->

                val questionStats = unitAnswers
                    .groupBy {
                        "${it.chatId}_${it.unitIndex}_${it.exerciseIndex}_${it.question.hashCode()}"
                    }
                    .map { (_, answersForQuestion) ->
                        val lastAnswer = answersForQuestion.maxBy { it.answeredAtEpochMillis }

                        val realAttempts = answersForQuestion
                            .flatMap { it.attempts }
                            .sortedBy { it.attemptNumber }

                        val lastAttempt = realAttempts.lastOrNull()

                        ExerciseQuestionStatsDto(
                            exerciseIndex = lastAnswer.exerciseIndex,
                            question = lastAnswer.question,
                            attemptsCount = realAttempts.size,
                            correctCount = realAttempts.count { it.isCorrect },
                            wrongCount = realAttempts.count { !it.isCorrect },
                            lastSelectedAnswer = lastAttempt?.selectedAnswer ?: lastAnswer.selectedAnswer,
                            correctAnswer = lastAnswer.correctAnswer,
                            attempts = realAttempts
                        )
                    }
                    .sortedBy { it.exerciseIndex }

                LearningUnitExerciseStatsDto(
                    unitIndex = unit.first,
                    unitTitle = unit.second,
                    correctCount = questionStats.sumOf { it.correctCount },
                    wrongCount = questionStats.sumOf { it.wrongCount },
                    totalAnswered = questionStats.sumOf { it.attemptsCount },
                    questions = questionStats
                )
            }
            .sortedBy { it.unitIndex }
    }

}
