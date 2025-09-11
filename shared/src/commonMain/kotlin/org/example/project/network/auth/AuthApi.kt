package org.example.project.network.auth

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import org.example.project.network.ApiClient
import org.example.project.network.remoteModels.registration.AuthRequestDto
import org.example.project.network.remoteModels.registration.AuthResponseDto

class AuthApi(
    private val baseUrl: String
) {
    private val client = ApiClient.httpClient

    suspend fun login(email: String, password: String): AuthResponseDto {
        return client.post {
            url("$baseUrl/login")
            setBody(AuthRequestDto(email, password))
        }.body()
    }

    suspend fun signup(email: String, password: String): AuthResponseDto {
        return client.post {
            url("$baseUrl/signup")
            setBody(AuthRequestDto(email, password))
        }.body()
    }
}