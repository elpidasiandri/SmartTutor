package org.example.project.tutor.repo

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.example.project.extensions.await
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.signOut
import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.learningUnits.PdfJobStatusRawResponse
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse

class WebTutorRepoImpl(
    private val firebase: FirebaseApp,
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : ITutorRepo {
    private val auth: FirebaseAuth = getAuth(firebase)

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

        return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
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

        return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
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
}
