package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.repo.IChatRepository
import io.ktor.server.routing.*

fun Route.chatRoutes(chatRepo: IChatRepository) {
    route("/v1/chats") {

        post("/from-document") {
            val req = call.receive<CreateChatFromDocumentRequest>()

            val chat = chatRepo.createOrGetChatFromDocument(
                userId = req.userId,
                documentId = req.documentId
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
    }
}
