package org.example.project

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.project.firebaseAdmin.FirebaseAdminInit
import io.ktor.server.plugins.cors.routing.*
import org.example.project.jobs.PdfJobWorker
import org.example.project.pdf.PromptLlmClient
import org.example.project.repo.FirestorePdfJobRepository
import org.example.project.repo.PdfJobRepository
import org.example.project.routes.pdfRoutes
import java.io.File
import java.util.Properties

private const val SERVER_PORT = 8080

fun main() {
    FirebaseAdminInit.init()

    embeddedServer(
        Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                encodeDefaults = true
            }
        )
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }

    val props = Properties().apply {
        val f = File("local.properties")
        if (f.exists()) load(f.inputStream())
    }

    val apiKey = props.getProperty("OPENAI_PROMPTING_API_KEY")
        ?: error("Missing OPENAI_PROMPTING_API_KEY in local.properties")

    val firestore = FirebaseAdminInit.firestore()

    val repo: PdfJobRepository = FirestorePdfJobRepository(firestore)
    val llm = PromptLlmClient(apiKey = apiKey)

    val worker = PdfJobWorker(repo = repo, llm = llm)
    worker.start(this)

    routing {
        get("/health") { call.respondText("ok") }

        // serve generated images
        staticFiles("/uploads", File("uploads"))

        // pdf upload + status
        pdfRoutes(repo)
    }
}