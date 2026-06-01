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
import org.example.project.repo.FirestoreChatRepository
import org.example.project.repo.FirestorePdfJobRepository
import org.example.project.repo.FirestoreProgressRepository
import org.example.project.repo.PdfJobRepository
import org.example.project.routes.chatRoutes
import org.example.project.routes.pdfRoutes
import org.example.project.routes.progressRoutes
import org.example.project.tutor.repo.IChatRepository
import org.example.project.myProgress.repo.IProgressRepository
import java.io.File
import java.util.Properties

private const val SERVER_PORT = 8081

fun main() {
    FirebaseAdminInit.init()

    val port = System.getenv("PORT")?.toIntOrNull() ?: SERVER_PORT

    embeddedServer(
        Netty,
        port = port,
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
                isLenient = true
            }
        )
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Put)
        anyHost()
    }

    val props = Properties().apply {
        val f = File("local.properties")
        if (f.exists()) load(f.inputStream())
    }

    val apiKey = System.getenv("OPENAI_PROMPTING_API_KEY")
        ?: props.getProperty("OPENAI_PROMPTING_API_KEY")
        ?: error("Missing OPENAI_PROMPTING_API_KEY")


    val firestore = FirebaseAdminInit.firestore()
    val pdfRepo: PdfJobRepository = FirestorePdfJobRepository(firestore)
    val chatRepo: IChatRepository = FirestoreChatRepository(firestore)
    val llm = PromptLlmClient(apiKey = apiKey)
    val progressRepo: IProgressRepository = FirestoreProgressRepository(firestore)
    val worker = PdfJobWorker(repo = pdfRepo, llm = llm)
    worker.start(this)

    routing {
        get("/health") { call.respondText("ok") }

        staticFiles("/uploads", File("uploads"))
        pdfRoutes(pdfRepo)
        chatRoutes(chatRepo, llm, progressRepo)
        progressRoutes(progressRepo)

    }
}
