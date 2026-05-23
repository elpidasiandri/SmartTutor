@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.example.project.tutor.repo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.example.project.extensions.await
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.signOut
import org.example.project.tutor.models.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.AskTutorRequest
import org.example.project.tutor.networkModel.chatHistory.AskTutorResponse
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.learningUnits.PdfJobStatusRawResponse
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse

class WebTutorRepoImpl(
    private val firebase: FirebaseApp,
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : ITutorRepo {
    private val auth: FirebaseAuth = getAuth(firebase)
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
    override suspend fun uploadFile(file: UploadFile, idToken: String): UploadPdfResponse {
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

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun getIdToken(onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val token = getIdTokenSuspend(forceRefresh = false)
                onResult(true, token)
            } catch (t: Throwable) {
                signOut(auth)
                onResult(false, null)
            }
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun getIdTokenSuspend(forceRefresh: Boolean): String {
        val auth = getAuth(firebase)
        val user = auth.currentUser ?: throw IllegalStateException("No logged-in user")

        val jsToken = user.getIdToken(forceRefresh).await()
        return jsToken.toString()
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

    override suspend fun markUnitAsRead(
        documentId: String,
        unitIndex: Int,
        token: String,
    ): PdfJobStatusResponse {
        return httpClient.post("$baseUrl/v1/pdfs/$documentId/units/$unitIndex/read") {
            header("Authorization", "Bearer $token")
        }.body()
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

        return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
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

    override suspend fun saveExerciseAnswer(
        answer: ExerciseAnswerProgressDto,
        userId: String,
        token: String,
    ) {
        httpClient.post("$baseUrl/v1/progress/exercise-answers") {
            header("Authorization", "Bearer $token")
            parameter("userId", userId)
            contentType(ContentType.Application.Json)
            setBody(answer)
        }
    }
}
