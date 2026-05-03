package org.example.project.myProgress.repo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto

class WebProgressRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : IProgressRepo {

    override suspend fun getOverview(userId: String): UserProgressOverviewDto {
        return httpClient.get("$baseUrl/v1/progress/overview") {
            parameter("userId", userId)
        }.body()
    }

    override suspend fun getChatProgressList(userId: String): List<ChatProgressDto> {
        return httpClient.get("$baseUrl/v1/progress/chats") {
            parameter("userId", userId)
        }.body()
    }

    override suspend fun upsertChatProgress(
        progress: ChatProgressDto,
        userId: String,
        token: String,
    ) {
        httpClient.post("$baseUrl/v1/progress/chats") {
            header("Authorization", "Bearer $token")
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
            setBody(progress)
        }
    }


    override suspend fun deleteChatProgress(
        userId: String,
        token: String,
        chatId: String
    ) {
        httpClient.delete("$baseUrl/v1/progress/chats/$chatId") {
            header("Authorization", "Bearer $token")
            parameter("userId", userId)
        }
    }
}