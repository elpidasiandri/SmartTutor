package org.example.project.repo

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import kotlinx.serialization.json.Json
import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.ChatSender
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.JobStatus
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import org.example.project.tutor.repo.IChatRepository
import java.util.UUID
import com.google.cloud.firestore.FieldValue
import org.example.project.storage.ExperimentContentStorage
import kotlinx.serialization.builtins.ListSerializer
class FirestoreChatRepository(
    private val db: Firestore,
    private val experimentContentStorage: ExperimentContentStorage,
) : IChatRepository {

    private val usersCol = db.collection("users")
    private val pdfJobsCol = db.collection("pdf_jobs")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override fun markUnitAsRead(
        userId: String,
        chatId: String,
        unitIndex: Int,
    ): Boolean {
        val chatRef = usersCol
            .document(userId)
            .collection("chats")
            .document(chatId)

        return db.runTransaction { tx ->
            val snap = tx.get(chatRef).get()
            if (!snap.exists()) return@runTransaction false

            if (
                snap.getString("documentId") ==
                ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
            ) {
                val totalUnits = snap.getLong("totalUnits")
                    ?.toInt()
                    ?: return@runTransaction false

                if (unitIndex !in 0 until totalUnits) {
                    return@runTransaction false
                }

                tx.update(
                    chatRef,
                    mapOf(
                        "readUnitIndices" to
                                FieldValue.arrayUnion(unitIndex.toLong()),
                        "updatedAtEpochMillis" to
                                System.currentTimeMillis()
                    )
                )

                return@runTransaction true
            }

            fun updatePrototypeJson(field: String): String? {
                val raw = snap.getString(field).orEmpty()
                if (raw.isBlank()) return null

                val old = json.decodeFromString(
                    LearningUnitsPrototypeDto.serializer(),
                    raw
                )

                if (unitIndex !in old.learningUnits.indices) return null

                val updated = old.copy(
                    learningUnits = old.learningUnits.mapIndexed { index, unit ->
                        if (index == unitIndex) unit.copy(isRead = true) else unit
                    }
                )

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

    override fun createOrGetChatFromDocument(
        userId: String,
        documentId: String,
        mode: String,
    ): UserChatDto {
        val chatsCol = usersCol.document(userId).collection("chats")

        val existingSnap = chatsCol
            .whereEqualTo("documentId", documentId)
            .whereEqualTo("mode", mode)
            .limit(1)
            .get()
            .get()

        val existingDoc = existingSnap.documents.firstOrNull()
        if (existingDoc != null && existingDoc.exists()) {
            return existingDoc.toUserChatDto()
        }

        if (
            documentId ==
            ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
        ) {
            val result = experimentContentStorage.loadExperimentContent()

            val now = System.currentTimeMillis()
            val chatId = "chat_" +
                    UUID.randomUUID().toString().replace("-", "")

            val chat = UserChatDto(
                chatId = chatId,
                userId = userId,
                documentId = documentId,
                title = "Java - Κεφάλαια 10-13",
                language = result.language ?: "el",
                status = JobStatus.Done.value,
                mode = mode,
                partialResult = null,
                result = result,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )

            // Αποθηκεύουμε μόνο metadata στο προσωπικό chat.
            val chatData = userChatToMap(chat).toMutableMap()

            chatData.remove("resultJson")
            chatData.remove("partialResultJson")

            chatData["totalUnits"] = result.learningUnits.size
            chatData["readUnitIndices"] = emptyList<Long>()

            chatsCol.document(chatId).set(chatData).get()

            return chat
        }

        val pdfDoc = pdfJobsCol.document(documentId).get().get()
        if (!pdfDoc.exists()) {
            error("Document $documentId not found")
        }

        val fileName = pdfDoc.getString("fileName") ?: "Untitled"
        val language = pdfDoc.getString("language")
        val status = pdfDoc.getString("status") ?: JobStatus.Done.value
        val partialJson = pdfDoc.getString("partialResultJson")
        val resultJson = pdfDoc.getString("resultJson")

        val (partial, result) = parsePrototype(
            partialJson = partialJson,
            resultJson = resultJson
        )

        val now = System.currentTimeMillis()
        val chatId = "chat_" + UUID.randomUUID().toString().replace("-", "")

        val chat = UserChatDto(
            chatId = chatId,
            userId = userId,
            documentId = documentId,
            title = fileName.removeSuffix(".pdf"),
            language = language,
            status = status,
            mode = mode,
            partialResult = partial,
            result = result,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        chatsCol.document(chatId).set(userChatToMap(chat)).get()

        return chat
    }

    override fun getChats(userId: String): List<UserChatDto> {
        val chatsCol = usersCol.document(userId).collection("chats")

        val snap = chatsCol
            .orderBy("updatedAtEpochMillis")
            .get()
            .get()

        return snap.documents
            .mapNotNull { doc ->
                runCatching { doc.toUserChatDto() }.getOrNull()
            }
            .sortedByDescending { it.updatedAtEpochMillis }
    }

    override fun getChat(userId: String, chatId: String): UserChatDto? {
        val doc = usersCol
            .document(userId)
            .collection("chats")
            .document(chatId)
            .get()
            .get()

        if (!doc.exists()) return null

        val chat = doc.toUserChatDto()
        val messages = getMessages(userId, chatId)

        return chat.copy(messages = messages)
    }

    override fun getMessages(
        userId: String,
        chatId: String,
    ): List<ChatMessageDto> {
        val snap = usersCol
            .document(userId)
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAtEpochMillis")
            .get()
            .get()

        return snap.documents.mapNotNull { doc ->
            runCatching { doc.toChatMessageDto() }.getOrNull()
        }
    }

    override suspend fun appendExercisesToUnit(
        userId: String,
        chatId: String,
        unitIndex: Int,
        exercises: List<ExerciseDto>,
    ): Boolean {

        val chatRef = usersCol
            .document(userId)
            .collection("chats")
            .document(chatId)

        val chatSnapshot = chatRef.get().get()

        if (!chatSnapshot.exists()) return false

        if (
            chatSnapshot.getString("documentId") ==
            ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
        ) {
            val totalUnits = chatSnapshot.getLong("totalUnits")
                ?.toInt()
                ?: return false

            if (unitIndex !in 0 until totalUnits) return false
            if (exercises.isEmpty()) return false

            val exercisesJson = json.encodeToString(
                ListSerializer(ExerciseDto.serializer()),
                exercises
            )

            val batchRef = chatRef
                .collection("extraExerciseBatches")
                .document()

            return db.runTransaction { tx ->
                val currentChat = tx.get(chatRef).get()

                if (
                    !currentChat.exists() ||
                    currentChat.getString("documentId") !=
                    ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
                ) {
                    return@runTransaction false
                }

                val now = System.currentTimeMillis()

                tx.set(
                    batchRef,
                    mapOf(
                        "unitIndex" to unitIndex,
                        "exercisesJson" to exercisesJson,
                        "createdAtEpochMillis" to now
                    )
                )

                tx.update(
                    chatRef,
                    mapOf("updatedAtEpochMillis" to now)
                )

                true
            }.get()
        }

        return db.runTransaction { tx ->

            val snap = tx.get(chatRef).get()

            if (!snap.exists()) {
                return@runTransaction false
            }

            fun updatePrototype(field: String): String? {

                val raw = snap.getString(field).orEmpty()

                if (raw.isBlank()) return null

                val prototype = json.decodeFromString(
                    LearningUnitsPrototypeDto.serializer(),
                    raw
                )

                if (unitIndex !in prototype.learningUnits.indices) {
                    return null
                }

                val updatedUnits =
                    prototype.learningUnits.mapIndexed { index, unit ->

                        if (index == unitIndex) {
                            unit.copy(
                                exercises = unit.exercises + exercises
                            )
                        } else {
                            unit
                        }
                    }

                val updatedPrototype =
                    prototype.copy(
                        learningUnits = updatedUnits
                    )

                return json.encodeToString(
                    LearningUnitsPrototypeDto.serializer(),
                    updatedPrototype
                )
            }

            val updates = mutableMapOf<String, Any>(
                "updatedAtEpochMillis" to System.currentTimeMillis()
            )

            updatePrototype("resultJson")?.let {
                updates["resultJson"] = it
            }

            updatePrototype("partialResultJson")?.let {
                updates["partialResultJson"] = it
            }

            if (updates.size == 1) {
                return@runTransaction false
            }

            tx.update(chatRef, updates)

            true
        }.get()
    }

    override fun addMessage(
        userId: String,
        chatId: String,
        message: ChatMessageDto,
    ) {
        val chatRef = usersCol
            .document(userId)
            .collection("chats")
            .document(chatId)

        chatRef
            .collection("messages")
            .document(message.messageId)
            .set(chatMessageToMap(message))
            .get()

        chatRef.update(
            mapOf("updatedAtEpochMillis" to System.currentTimeMillis())
        ).get()
    }

    override fun deleteChat(userId: String, chatId: String) {
        val chatRef = usersCol.document(userId).collection("chats").document(chatId)

        val messagesSnap = chatRef.collection("messages").get().get()

        messagesSnap.documents.forEach { doc ->
            doc.reference.delete().get()
        }

        val extraExercisesSnap = chatRef.collection("extraExerciseBatches").get().get()

        extraExercisesSnap.documents.forEach { doc ->
            doc.reference.delete().get()
        }

        chatRef.delete().get()
    }

    private fun parsePrototype(
        partialJson: String?,
        resultJson: String?,
    ): Pair<LearningUnitsPrototypeDto?, LearningUnitsPrototypeDto?> {
        val partial = if (!partialJson.isNullOrBlank()) {
            runCatching {
                json.decodeFromString(LearningUnitsPrototypeDto.serializer(), partialJson)
            }.getOrNull()
        } else null

        val result = if (!resultJson.isNullOrBlank()) {
            runCatching {
                json.decodeFromString(LearningUnitsPrototypeDto.serializer(), resultJson)
            }.getOrNull()
        } else null

        return partial to result
    }

    private fun userChatToMap(chat: UserChatDto): Map<String, Any?> {
        return mapOf(
            "chatId" to chat.chatId,
            "userId" to chat.userId,
            "documentId" to chat.documentId,
            "title" to chat.title,
            "language" to chat.language,
            "status" to chat.status,
            "mode" to chat.mode,
            "partialResultJson" to chat.partialResult?.let {
                json.encodeToString(LearningUnitsPrototypeDto.serializer(), it)
            },
            "resultJson" to chat.result?.let {
                json.encodeToString(LearningUnitsPrototypeDto.serializer(), it)
            },
            "createdAtEpochMillis" to chat.createdAtEpochMillis,
            "updatedAtEpochMillis" to chat.updatedAtEpochMillis,
        )
    }

    private fun DocumentSnapshot.toChatMessageDto(): ChatMessageDto {
        return ChatMessageDto(
            messageId = getString("messageId") ?: id,
            chatId = getString("chatId") ?: "",
            sender = getString("sender") ?: ChatSender.System.value,
            text = getString("text") ?: "",
            unitNumber = getLong("unitNumber")?.toInt(),
            unitTitle = getString("unitTitle"),
            createdAtEpochMillis = getLong("createdAtEpochMillis") ?: 0L
        )
    }

    private fun chatMessageToMap(message: ChatMessageDto): Map<String, Any?> {
        return mapOf(
            "messageId" to message.messageId,
            "chatId" to message.chatId,
            "sender" to message.sender,
            "text" to message.text,
            "unitNumber" to message.unitNumber,
            "unitTitle" to message.unitTitle,
            "createdAtEpochMillis" to message.createdAtEpochMillis
        )
    }

    private fun DocumentSnapshot.mergeExperimentExercises(
        prototype: LearningUnitsPrototypeDto
    ): LearningUnitsPrototypeDto {

        val batches = reference
            .collection("extraExerciseBatches")
            .orderBy("createdAtEpochMillis")
            .get()
            .get()

        val extrasByUnit = mutableMapOf<Int, MutableList<ExerciseDto>>()

        batches.documents.forEach { batch ->
            val unitIndex = requireNotNull(
                batch.getLong("unitIndex")
            ).toInt()

            val rawJson = requireNotNull(
                batch.getString("exercisesJson")
            )

            val exercises = json.decodeFromString(
                ListSerializer(ExerciseDto.serializer()),
                rawJson
            )

            extrasByUnit
                .getOrPut(unitIndex) { mutableListOf() }
                .addAll(exercises)
        }

        return prototype.copy(
            learningUnits = prototype.learningUnits.mapIndexed { index, unit ->
                unit.copy(
                    exercises = unit.exercises +
                            extrasByUnit[index].orEmpty()
                )
            }
        )
    }

    private fun DocumentSnapshot.toUserChatDto(): UserChatDto {
        val partialJson = getString("partialResultJson")
        val resultJson = getString("resultJson")

        val partial = if (!partialJson.isNullOrBlank()) {
            runCatching {
                json.decodeFromString(LearningUnitsPrototypeDto.serializer(), partialJson)
            }.getOrNull()
        } else null


        val result = if (
            getString("documentId") ==
            ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
        ) {
            val readIndices = (get("readUnitIndices") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toInt() }
                ?.toSet()
                .orEmpty()

            val sharedContent =
                experimentContentStorage.loadExperimentContent()

            sharedContent.copy(
                learningUnits = sharedContent.learningUnits.mapIndexed {
                        index, unit ->
                    if (index in readIndices) {
                        unit.copy(isRead = true)
                    } else {
                        unit
                    }
                }
            )
        } else if (!resultJson.isNullOrBlank()) {
            runCatching {
                json.decodeFromString(
                    LearningUnitsPrototypeDto.serializer(),
                    resultJson
                )
            }.getOrNull()
        } else null

        val finalResult = if (
            getString("documentId") ==
            ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID &&
            result != null
        ) {
            mergeExperimentExercises(result)
        } else {
            result
        }


        return UserChatDto(
            chatId = getString("chatId") ?: id,
            userId = getString("userId") ?: "",
            documentId = getString("documentId") ?: "",
            title = getString("title") ?: "Untitled",
            language = getString("language"),
            status = getString("status") ?: JobStatus.Done.value,
            mode = getString("mode") ?: "LEARNING",
            partialResult = partial,
            result = finalResult,
            createdAtEpochMillis = getLong("createdAtEpochMillis") ?: 0L,
            updatedAtEpochMillis = getLong("updatedAtEpochMillis") ?: 0L
        )
    }

}

