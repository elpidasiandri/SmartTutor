package org.example.project.repo

import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import kotlinx.serialization.json.Json
import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.uploadPdf.JobStatus
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import org.example.project.tutor.repo.IChatRepository
import java.util.UUID


class FirestoreChatRepository(private val db: Firestore) : IChatRepository {

    private val usersCol = db.collection("users")
    private val pdfJobsCol = db.collection("pdf_jobs")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override fun createOrGetChatFromDocument(
        userId: String,
        documentId: String
    ): UserChatDto {
        val chatsCol = usersCol.document(userId).collection("chats")

        val existingSnap = chatsCol
            .whereEqualTo("documentId", documentId)
            .limit(1)
            .get()
            .get()

        val existingDoc = existingSnap.documents.firstOrNull()
        if (existingDoc != null && existingDoc.exists()) {
            return existingDoc.toUserChatDto()
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
        return doc.toUserChatDto()
    }

    override fun getMessages(
        userId: String,
        chatId: String,
    ): List<ChatMessageDto> {
        TODO("Not yet implemented")
    }

    override fun addMessage(
        userId: String,
        chatId: String,
        message: ChatMessageDto,
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteChat(userId: String, chatId: String) {
        TODO("Not yet implemented")
    }

    private fun parsePrototype(
        partialJson: String?,
        resultJson: String?
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

    private fun DocumentSnapshot.toUserChatDto(): UserChatDto {
        val partialJson = getString("partialResultJson")
        val resultJson = getString("resultJson")

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

        return UserChatDto(
            chatId = getString("chatId") ?: id,
            userId = getString("userId") ?: "",
            documentId = getString("documentId") ?: "",
            title = getString("title") ?: "Untitled",
            language = getString("language"),
            status = getString("status") ?: JobStatus.Done.value,
            partialResult = partial,
            result = result,
            createdAtEpochMillis = getLong("createdAtEpochMillis") ?: 0L,
            updatedAtEpochMillis = getLong("updatedAtEpochMillis") ?: 0L
        )
    }
}

