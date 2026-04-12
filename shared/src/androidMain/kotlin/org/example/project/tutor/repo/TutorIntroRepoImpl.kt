package org.example.project.tutor.repo

import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.AskTutorRequest
import org.example.project.tutor.networkModel.chatHistory.AskTutorResponse
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.learningUnits.PdfJobStatusRawResponse
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TutorIntroRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val auth: FirebaseAuth,
) : ITutorRepo {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun uploadFile(
        file: UploadFile,
        idToken: String,
    ): UploadPdfResponse {
        val response = httpClient.submitFormWithBinaryData(
            url = "$baseUrl/v1/pdfs/upload",
            formData = formData {
                append(
                    key = "file",
                    value = file.bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, file.contentType)
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"file\"; filename=\"${file.fileName}\""
                        )
                    }
                )
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $idToken")
            accept(ContentType.Application.Json)
            expectSuccess = false
        }
        val raw = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("Upload failed: ${response.status} body=$raw")
        }

        return json.decodeFromString(raw)
    }


    override suspend fun getIdTokenSuspend(forceRefresh: Boolean): String =
        suspendCancellableCoroutine { cont ->
            getIdToken { ok, tokenOrErr ->
                if (ok && !tokenOrErr.isNullOrBlank()) cont.resume(tokenOrErr)
                else {
                    auth.signOut()
                    cont.resumeWithException(
                        IllegalStateException(tokenOrErr ?: "No ID token")
                    )
                }
            }
        }

    override fun getIdToken(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No current user")
            return
        }

        user.getIdToken(true)
            .addOnSuccessListener { res ->
                onResult(true, res.token)
            }
            .addOnFailureListener { e ->
                auth.signOut()
                onResult(false, e.message)
            }
    }

    override suspend fun getPdfJobStatus(
        documentId: String,
        idToken: String,
    ): PdfJobStatusResponse {
        val response = httpClient.get("$baseUrl/v1/pdfs/$documentId") {
            header(HttpHeaders.Authorization, "Bearer $idToken")
            accept(ContentType.Application.Json)
            expectSuccess = false
        }

        val raw = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("Get status failed: ${response.status} body=$raw")
        }

        return json.decodeFromString(raw)
    }

    override suspend fun getPdfJobRaw(
        documentId: String,
        idToken: String,
    ): PdfJobStatusRawResponse {
        val response = httpClient.get("$baseUrl/v1/pdfs/$documentId") {
            header(HttpHeaders.Authorization, "Bearer $idToken")
            accept(ContentType.Application.Json)
            expectSuccess = false
        }

        val raw = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("Get status failed: ${response.status} body=$raw")
        }

        return json.decodeFromString(raw)
    }

    override suspend fun getChat(
        chatId: String,
        userId: String,
    ): UserChatDto {
        val response = httpClient.get("$baseUrl/v1/chats/$chatId") {
            parameter("userId", userId)
            accept(ContentType.Application.Json)
            expectSuccess = false
        }

        val raw = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("Get chat failed: ${response.status} body=$raw")
        }

        return json.decodeFromString(raw)
    }

    override suspend fun deleteChat(
        chatId: String,
        userId: String,
        token: String,
    ) {
        val response = httpClient.delete("$baseUrl/v1/chats/$chatId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("userId", userId)
            accept(ContentType.Application.Json)
            expectSuccess = false
        }

        val raw = response.bodyAsText()

        if (!response.status.isSuccess()) {
            error("Delete chat failed: ${response.status} body=$raw")
        }
    }

    override suspend fun createChatFromDocument(
        request: CreateChatFromDocumentRequest,
        token: String,
    ): CreateChatFromDocumentResponse {
        return httpClient.post("$baseUrl/v1/chats/from-document") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getChats(userId: String): List<UserChatDto> {
        return httpClient.get("$baseUrl/v1/chats") {
            parameter("userId", userId)
        }.body()
    }

    override suspend fun askTutor(
        chatId: String,
        userId: String,
        request: AskTutorRequest,
        token: String,
    ): AskTutorResponse {
        return httpClient.post("$baseUrl/v1/chats/$chatId/messages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            expectSuccess = false
            setBody(request)

            timeout {
                requestTimeoutMillis = 300_000
                socketTimeoutMillis = 300_000
                connectTimeoutMillis = 30_000
            }
        }.let { response ->
            val raw = response.bodyAsText()

            if (!response.status.isSuccess()) {
                error("Ask tutor failed: ${response.status} body=$raw")
            }

            json.decodeFromString(raw)
        }
    }

}