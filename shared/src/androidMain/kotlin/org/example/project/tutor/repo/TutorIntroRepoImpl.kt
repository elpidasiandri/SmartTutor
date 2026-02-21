package org.example.project.tutor.repo

import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TutorIntroRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val auth: FirebaseAuth,
) : ITutorRepo {

    override suspend fun uploadFile(
        file: UploadFile,
        idToken: String,
    ): UploadPdfResponse {

        val response = httpClient.submitFormWithBinaryData(
            url = "$baseUrl/v1/pdfs",
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
        }
        val raw = response.bodyAsText()
        return response.body()
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

    override suspend fun getPdfJobStatus(documentId: String, idToken: String): PdfJobStatusResponse {
        return httpClient.get("$baseUrl/v1/pdfs/$documentId") {
            header(HttpHeaders.Authorization, "Bearer $idToken")
            accept(ContentType.Application.Json)
        }.body()
    }

}