package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.log
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.repo.IChatRepository
import io.ktor.server.routing.*
import org.example.project.pdf.PromptLlmClient
import org.example.project.tutor.networkModel.chatHistory.AddMoreExercisesResponse
import org.example.project.tutor.networkModel.chatHistory.AskTutorRequest
import org.example.project.tutor.networkModel.chatHistory.AskTutorResponse
import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.ChatSender
import java.util.UUID

fun Route.chatRoutes(chatRepo: IChatRepository, llm: PromptLlmClient) {
    route("/v1/chats") {
        post("/{chatId}/units/{unitIndex}/read") {
            val userId = call.request.queryParameters["userId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing userId")
                )

            val chatId = call.parameters["chatId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing chatId")
                )

            val unitIndex = call.parameters["unitIndex"]?.toIntOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing unitIndex")
                )

            val updated = chatRepo.markUnitAsRead(
                userId = userId,
                chatId = chatId,
                unitIndex = unitIndex
            )

            if (!updated) {
                return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Chat or unit not found")
                )
            }

            call.respond(HttpStatusCode.OK, mapOf("success" to true))
        }

        post("/from-document") {
            val req = call.receive<CreateChatFromDocumentRequest>()

            val chat = chatRepo.createOrGetChatFromDocument(
                userId = req.userId,
                documentId = req.documentId,
                mode = req.mode
            )

            call.respond(CreateChatFromDocumentResponse(chat = chat))
        }

        get {
            val userId = call.request.queryParameters["userId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing userId")
                )

            call.respond(chatRepo.getChats(userId))
        }

        post("/{chatId}/units/{unitIndex}/exercises/more") {
            try {
                val userId = call.request.queryParameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

                val chatId = call.parameters["chatId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing chatId"))

                val unitIndex = call.parameters["unitIndex"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing unitIndex"))

                application.log.info("MORE EXERCISES userId=$userId chatId=$chatId unitIndex=$unitIndex")

                val chat = chatRepo.getChat(userId, chatId)
                    ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Chat not found"))

                val prototype = chat.result ?: chat.partialResult
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No content found"))

                val unit = prototype.learningUnits.getOrNull(unitIndex)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid unitIndex"))

                application.log.info("UNIT=${unit.title}")

                val extraExercises = llm.generateExercisesForUnit(
                    unit = unit,
                    language = chat.language ?: "el",
                    fileName = chat.title,
                    targetMinutes = 55
                )

                application.log.info("GENERATED=${extraExercises.size}")

                val saved = chatRepo.appendExercisesToUnit(
                    userId = userId,
                    chatId = chatId,
                    unitIndex = unitIndex,
                    exercises = extraExercises
                )

                application.log.info("SAVED=$saved")

                call.respond(AddMoreExercisesResponse(exercises = extraExercises))

            } catch (t: Throwable) {
                application.log.error("Add more exercises failed", t)

                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (t.message ?: "Unknown server error"))
                )
            }
        }

        post("/{chatId}/messages") {
            val userId = call.request.queryParameters["userId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing userId")
                )

            val chatId = call.parameters["chatId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing chatId")
                )

            val req = call.receive<AskTutorRequest>()

            val messageText = req.message?.trim()
            if (messageText.isNullOrBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing message")
                )
            }

            val chat = chatRepo.getChat(userId, chatId)
                ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Chat not found")
                )

            val prototype = chat.result ?: chat.partialResult
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "No content found for this chat")
            )

            val safeUnitIndex = req.unitIndex ?: 0
            val unit = prototype.learningUnits.getOrNull(safeUnitIndex)
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid unitIndex")
                )

            val userMessage = ChatMessageDto(
                messageId = "msg_" + UUID.randomUUID().toString().replace("-", ""),
                chatId = chatId,
                sender = ChatSender.User.value,
                text = messageText,
                unitNumber = unit.numbering,
                unitTitle = unit.title,
                createdAtEpochMillis = System.currentTimeMillis()
            )

            if (req.saveToChat) {
                chatRepo.addMessage(userId, chatId, userMessage)
            }
            val recentMessages = chatRepo.getMessages(userId, chatId).takeLast(12)

            val contextText = req.contextText?.trim().orEmpty().ifBlank {
                when (req.tab.lowercase()) {
                    "learning" -> unit.textInfo ?: ""
                    "exercises" -> unit.exercises.joinToString("\n\n") { it.question ?: "" }
                    else -> unit.textInfo ?: ""
                }
            }

            val aiAnswer = llm.answerQuestionForUnit(
                language = chat.language ?: "el",
                fileName = chat.title,
                unitTitle = unit.title ?: "",
                tab = req.tab,
                contextText = contextText,
                recentMessages = recentMessages,
                userQuestion = messageText
            )
            val aiMessage = ChatMessageDto(
                messageId = "msg_" + UUID.randomUUID().toString().replace("-", ""),
                chatId = chatId,
                sender = ChatSender.Ai.value,
                text = aiAnswer,
                unitNumber = unit.numbering,
                unitTitle = unit.title,
                createdAtEpochMillis = System.currentTimeMillis()
            )

            if (req.saveToChat) {
                chatRepo.addMessage(userId, chatId, aiMessage)
            }

            call.respond(
                AskTutorResponse(
                    userMessage = userMessage,
                    aiMessage = aiMessage
                )
            )
        }

        get("/{chatId}") {
            val userId = call.request.queryParameters["userId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing userId")
                )

            val chatId = call.parameters["chatId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing chatId")
                )

            val chat = chatRepo.getChat(userId, chatId)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Chat not found")
                )

            call.respond(chat)
        }

        delete("/{chatId}") {
            val userId = call.request.queryParameters["userId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userId")

            val chatId = call.parameters["chatId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing chatId")

            try {
                chatRepo.deleteChat(userId, chatId)
                call.respond(HttpStatusCode.OK, mapOf("success" to true))
            } catch (t: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (t.message ?: "Failed to delete chat"))
                )
            }
        }
    }
}
