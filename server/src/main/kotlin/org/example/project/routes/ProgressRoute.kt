package org.example.project.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto
import org.example.project.myProgress.repo.IProgressRepository

fun Route.progressRoutes(progressRepo: IProgressRepository) {
    route("/v1/progress") {

        get("/overview") {
            val userId = call.request.queryParameters["userId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

            val overview = progressRepo.getOverview(userId)
                ?: UserProgressOverviewDto(userId = userId)

            call.respond(overview)
        }

        get("/chats") {
            val userId = call.request.queryParameters["userId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

            call.respond(progressRepo.getChatProgressList(userId))
        }

        post("/chats") {
            val userId = call.request.queryParameters["userId"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

            val progress = call.receive<ChatProgressDto>()
            progressRepo.upsertChatProgress(userId, progress)
            progressRepo.updateOverview(userId)

            call.respond(HttpStatusCode.OK, mapOf("success" to true))
        }

        delete("/chats/{chatId}") {
            val userId = call.request.queryParameters["userId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

            val chatId = call.parameters["chatId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing chatId"))

            progressRepo.deleteChatProgress(userId, chatId)

            call.respond(HttpStatusCode.OK, mapOf("success" to true))
        }
    }
}