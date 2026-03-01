package org.example.project.pdf

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.*
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.ImageGenRequest
import org.example.project.tutor.networkModel.uploadPdf.ImageGenResponse
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.UnitExercisesOnlyDto
import org.example.project.tutor.networkModel.uploadPdf.UnitWithCarryDto
import kotlin.random.Random
import java.io.File
import java.net.SocketTimeoutException
import java.util.Base64

class PromptLlmClient(
    private val apiKey: String,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {
    private val openAiPermits = Semaphore(permits = 3)

    var onRawHook: ((tag: String, text: String) -> Unit)? = null

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
            requestTimeoutMillis = 180_000
            socketTimeoutMillis = 180_000
        }
        engine { requestTimeout = 180_000 }
    }

    suspend fun generateExercisesForUnit(
        unit: LearningUnitDto,
        language: String,
        fileName: String,
        targetMinutes: Int = 55,
    ): List<ExerciseDto> {
        val schema = unitExercisesOnlySchemaJson()

        val unitText = buildString {
            appendLine("UNIT_TITLE: ${unit.title}")
            appendLine("UNIT_TEXT:")
            appendLine(unit.textInfo ?: "")
            appendLine()
            appendLine("UNIT_QUOTATIONS:")
            unit.quotations.forEach { q -> appendLine("- ${q.text}") }
            appendLine()
            appendLine("UNIT_IMAGES_CAPTIONS:")
            unit.images.forEach { i -> appendLine("- ${i.caption}") }
        }.take(25_000)

        val system = """
        MODE: UNIT_EXERCISES_ONLY
        Return ONLY JSON matching schema (strict).
        No citations.
        
        GOAL:
        Generate a complete ~60-minute exam set based ONLY on the given UNIT.
        
        HARD RULES:
        - Generate exercises ONLY from UNIT_TEXT/QUOTATIONS/IMAGES_CAPTIONS above.
        - exercises MUST NOT be empty.
        
        TIME:
        - Total estimatedMinutes MUST sum to ${targetMinutes - 3}..${targetMinutes + 3} minutes.

        COUNT:
        - Total exercises MUST be between 16 and 30.

        MUST INCLUDE (variety):
        - At least 6 MCQ
        - At least 5 true_false
        - At least 4 open (κριτικής σκέψης/αιτιολόγησης/μικρά προβλήματα/ ρωτήσεις κρίσεως/αιτιολόγησης η και να λυσει προβληματα, η να γραψει κωδικα)
        - At least 1 coding exercise (write Java code). (May be more)

        DIFFICULTY:
        - 2 exercises MUST have difficulty="hard" 
        - All others MUST have difficulty="medium"

        REQUIREMENTS:
        - Total estimatedMinutes sum: 52–60
        - Total exercises: 16–30

        MCQ OPTIONS:
        - MCQ may have 3 to 6 options (NOT necessarily 4).
        - answerIndex MUST be a valid index for the options array.

        FORMAT RULES:
        - mcq: options length 3..6, answerIndex 0..(len-1)
        - true_false: options MUST be ["Σωστό","Λάθος"], answerIndex 0/1
        - open: options MUST be ["(Ελεύθερη απάντηση)"], answerIndex MUST be null
        - Every exercise MUST include hint + explanation + solution (non-empty).
        
        """.trimIndent()

        val user = """
        Filename: $fileName
        Language: $language

        $unitText
        """.trimIndent()

        val req = buildResponsesRequest(
            model = "gpt-4o-2024-08-06",
            system = system,
            user = user,
            schemaOnly = schema,
            maxOutputTokens = 4200
        )

        onRawHook?.invoke("exercises_request", req)

        val resp = callOpenAIResponsesWithRetry(req)
        onRawHook?.invoke("exercises_response", resp)
        ensureCompleted(resp)

        val raw = extractOutputJson(resp)
        val out = coerceToJsonObjectString(raw)
        val dto = json.decodeFromString(UnitExercisesOnlyDto.serializer(), out)

        val normalized = normalizeExercisesToTimeBudget(dto.exercises, targetMinutes)
        val validated = runCatching {
            validateExercisesOrThrow(normalized, targetMinutes, tolerance = 1, hardMin = 50, hardMax = 75)
            normalized
        }.getOrElse { err ->
            val repaired = repairExercisesToMeetConstraints(
                originalJson = out,
                unitText = unitText,
                language = language,
                fileName = fileName,
                targetMinutes = targetMinutes,
                schemaOnly = schema
            )
            validateExercisesOrThrow(normalized, targetMinutes, tolerance = 1, hardMin = 50, hardMax = 75)
            repaired
        }

        return validated
    }
    private suspend fun repairExercisesToMeetConstraints(
        originalJson: String,
        unitText: String,
        language: String,
        fileName: String,
        targetMinutes: Int,
        schemaOnly: String
    ): List<ExerciseDto> {

        val system = """
        MODE: UNIT_EXERCISES_REPAIR
        Return ONLY JSON matching schema (strict). No citations.
        
        GOAL:
        Fix the provided exercises so that ALL constraints are satisfied.
        
        CONSTRAINTS (must be satisfied):
        - Total exercises: 16..30
        - Total estimatedMinutes sum: ${targetMinutes - 3}..${targetMinutes + 3}
        - At least 6 mcq, at least 5 true_false, at least 4 open
        - Exactly 2 hard, all others medium
        - At least 1 Java coding exercise
        - mcq options length 3..6, answerIndex valid
        - true_false options exactly ["Σωστό","Λάθος"]
        - open options exactly ["(Ελεύθερη απάντηση)"] and answerIndex null
        
        IMPORTANT:
        You may:
        - remove some exercises
        - adjust estimatedMinutes down/up (small changes)
        - rewrite questions if needed
        But you must NOT introduce content not supported by UNIT_TEXT/QUOTATIONS/IMAGES_CAPTIONS.
        """.trimIndent()

                val user = """
        Filename: $fileName
        Language: $language
        
        UNIT:
        $unitText
        
        ORIGINAL_EXERCISES_JSON:
        $originalJson
        """.trimIndent()

        val req = buildResponsesRequest(
            model = "gpt-4o-2024-08-06",
            system = system,
            user = user,
            schemaOnly = schemaOnly,
            maxOutputTokens = 4200
        )

        val resp = callOpenAIResponsesWithRetry(req)
        ensureCompleted(resp)

        val raw = extractOutputJson(resp)
        val fixedOut = coerceToJsonObjectString(raw)

        val dto = json.decodeFromString(UnitExercisesOnlyDto.serializer(), fixedOut)
        return dto.exercises
    }
    suspend fun generateAndSaveImage(
        documentId: String,
        unitNumber: Int,
        imageIndex: Int,
        prompt: String,
    ): String? {
        val reqObj = ImageGenRequest(prompt = prompt)
        val reqJson = json.encodeToString(ImageGenRequest.serializer(), reqObj)
        onRawHook?.invoke("image_request", reqJson)

        val respText: String = try {
            callOpenAIImagesWithRetry(reqObj)
        } catch (t: Throwable) {
            onRawHook?.invoke("image_error", "Image gen failed permanently: ${t.message}")
            return null
        }

        onRawHook?.invoke("image_response", respText)

        val parsed = runCatching {
            json.decodeFromString(
                ImageGenResponse.serializer(),
                respText
            )
        }.getOrNull()
            ?: return null

        val b64 = parsed.data.firstOrNull()?.b64Json ?: return null
        val bytes = Base64.getDecoder().decode(b64)

        val dir = File("uploads/$documentId/images").apply { mkdirs() }
        val file = File(dir, "unit_${unitNumber}_img_${imageIndex}.png")
        file.writeBytes(bytes)

        return "uploads/$documentId/images/${file.name}"
    }

    private fun validateExercisesOrThrow(
        exercises: List<ExerciseDto>,
        targetMinutes: Int,
        tolerance: Int = 3,
        hardMin: Int = 50,
        hardMax: Int = 75,
    ) {
        if (exercises.size !in 16..30) error("Exercise count must be 16..30 (got ${exercises.size})")

        val total = exercises.sumOf { it.estimatedMinutes ?: 0 }

        if (total < hardMin || total > hardMax) {
            error("Total minutes hard bounds $hardMin..$hardMax (got $total)")
        }

        val mcq = exercises.count { it.type == "mcq" }
        val tf = exercises.count { it.type == "true_false" }
        val op = exercises.count { it.type == "open" }

        if (mcq < 6) error("Need at least 6 MCQ (got $mcq)")
        if (tf < 5) error("Need at least 5 true_false (got $tf)")
        if (op < 4) error("Need at least 4 open (got $op)")

        val hard = exercises.count { it.difficulty == "hard" }
        if (hard != 2) error("Exactly 2 hard required (got $hard)")

        val notMediumOrHard = exercises.count { it.difficulty != "medium" && it.difficulty != "hard" }
        if (notMediumOrHard != 0) error("Only 'medium' allowed except 2 'hard'")

        exercises.forEachIndexed { idx, ex ->
            val n = ex.options.size
            when (ex.type) {
                "mcq" -> {
                    if (n !in 3..6) error("MCQ options must be 3..6 at #${idx + 1} (got $n)")
                    val ai = ex.answerIndex ?: error("MCQ answerIndex missing at #${idx + 1}")
                    if (ai !in 0 until n) error("MCQ answerIndex out of range at #${idx + 1}")
                }
                "true_false" -> {
                    if (ex.options != listOf("Σωστό", "Λάθος")) error("TF options must be [Σωστό, Λάθος] at #${idx + 1}")
                    if (ex.answerIndex !in listOf(0, 1)) error("TF answerIndex must be 0/1 at #${idx + 1}")
                }
                "open" -> {
                    if (ex.options != listOf("(Ελεύθερη απάντηση)")) error("Open options invalid at #${idx + 1}")
                    if (ex.answerIndex != null) error("Open answerIndex must be null at #${idx + 1}")
                }
                else -> error("Unknown exercise type '${ex.type}' at #${idx + 1}")
            }
        }

        val coding = exercises.count { ex ->
            val q = ex.question?.lowercase().orEmpty()
            val sol = ex.solution?.lowercase().orEmpty()
            (q.contains("java") && (q.contains("γράψε") || q.contains("υλοποίησε") || q.contains("κώδικ"))) ||
                    sol.contains("```java")
        }
        if (coding < 1) error("Need at least 1 coding exercise (Java)")
    }
    private fun normalizeExercisesToTimeBudget(
        exercises: List<ExerciseDto>,
        targetMinutes: Int,
        tolerance: Int = 3,
    ): List<ExerciseDto> {
        val minTotal = targetMinutes - tolerance
        val maxTotal = targetMinutes + tolerance

        fun total(xs: List<ExerciseDto>) = xs.sumOf { it.estimatedMinutes ?: 0 }

        // already ok
        if (total(exercises) in minTotal..maxTotal) return exercises

        // if too small, return as-is -> validation will fail -> retry/repair
        if (total(exercises) < minTotal) return exercises

        val kept = exercises.toMutableList()

        fun hasCoding(xs: List<ExerciseDto>): Boolean {
            return xs.any { ex ->
                val q = ex.question?.lowercase().orEmpty()
                val sol = ex.solution?.lowercase().orEmpty()
                (q.contains("java") && (q.contains("γράψε") || q.contains("υλοποίησε") || q.contains("κώδικ"))) ||
                        sol.contains("```java")
            }
        }

        fun okVariety(xs: List<ExerciseDto>): Boolean {
            val mcq = xs.count { it.type == "mcq" }
            val tf = xs.count { it.type == "true_false" }
            val op = xs.count { it.type == "open" }
            val hard = xs.count { it.difficulty == "hard" }
            return mcq >= 6 && tf >= 5 && op >= 4 && hard == 2 && hasCoding(xs)
        }

        // 1) try removing from end while still meeting constraints
        var i = kept.size - 1
        while (i >= 0 && total(kept) > maxTotal) {
            val candidate = kept.toMutableList().also { it.removeAt(i) }
            if (candidate.size >= 16 && okVariety(candidate)) {
                kept.removeAt(i)
                i = minOf(i, kept.size - 1)
                continue
            }
            i--
        }

        // 2) if still too high, shrink minutes (non-hard preferred)
        // priority: medium mcq/tf reduce to 2; open reduce to 3; keep hard as-is
        fun minAllowed(ex: ExerciseDto): Int {
            return when (ex.type) {
                "mcq" -> 2
                "true_false" -> 1
                "open" -> 3
                else -> 1
            }
        }

        var safety = 500
        while (total(kept) > maxTotal && safety-- > 0) {
            // pick best candidate to shrink:
            val idx = kept.indexOfFirst { ex ->
                val cur = ex.estimatedMinutes ?: 0
                ex.difficulty != "hard" && cur > minAllowed(ex)
            }.takeIf { it >= 0 } ?: break

            val ex = kept[idx]
            val cur = ex.estimatedMinutes ?: 0
            kept[idx] = ex.copy(estimatedMinutes = cur - 1)
        }

        return kept
    }

    suspend fun generatePreviewUnitFromPdfChunkWithCarry(
        pdfChunkFile: File,
        language: String,
        fileName: String,
        unitNumber: Int,
        carry: String,
    ): UnitWithCarryDto {
        val fileId = uploadPdfToOpenAI(pdfChunkFile)
        val schema = unitWithCarrySchemaJson()

        val captionLang = if (language.lowercase().startsWith("el")) "Greek" else "English"

        val system = """
            MODE: PREVIEW_UNIT_FROM_CHUNK_WITH_CARRY
            Return ONLY JSON matching schema (strict).
            No citations.
            
            GOAL:
            - Use CURRENT_CHUNK (attached PDF) + CARRY_FROM_PREVIOUS.
            - Create AT MOST 1 learning unit OR return unit=null if not enough substance.
            - If not enough for a full unit, return unit=null and put dense notes in carry (<=8000 chars).
            
            TEXT:
            - Greek (if language=el), exam-focused, no fluff.
            - 3–7 paragraphs separated by \n.
            - 350–900 words.
            - MUST contain at least 2 fenced Java code blocks relevant to the chunk OR (if theory-heavy) 1 code block + 1 diagram image.
            
            IMAGES:
            - If needed, include 1 diagram image:
              images[0].relativePath=null
              images[0].caption MUST be in $captionLang.
              images[0].prompt MUST be in English, diagram-style (even if caption is Greek).
            
            QUOTATIONS:
            - quotations MUST be [] or 1..2 items.
            - quotations[i].text MUST be a single STRING. If unsure: quotations: [].
            
            EXERCISES:
            - exercises MUST be [] here.
            
            CODE FORMAT RULE:
            - Every Java statement MUST be on a new line.
            - NEVER write multiple Java statements on the same line.
            - Always include line breaks inside code blocks.
            
            ANTI-HALLUCINATION:
            - Use ONLY what is supported by CURRENT_CHUNK + carry.
            """.trimIndent()

        val user = """
            Filename: $fileName
            Language: $language
            
            CARRY_FROM_PREVIOUS:
            ${carry.take(8000)}
            
            CURRENT_CHUNK:
            (see attached PDF)
            """.trimIndent()

        val req = buildResponsesRequestWithPdfFile(
            model = "gpt-4o-mini",
            fileId = fileId,
            system = system,
            user = user,
            schemaOnly = schema,
            maxOutputTokens = 2400
        )

        onRawHook?.invoke("preview_request", req)

        val resp = callOpenAIResponsesWithRetry(req)
        onRawHook?.invoke("preview_response", resp)

        ensureCompleted(resp)

        val raw = extractOutputJson(resp)

        val unwrapped = unwrapIfJsonString(raw)
        val out = extractFirstJsonObjectOrNull(unwrapped)

        if (out == null || !isValidJson(out)) {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                appendLine("[PREVIEW_INVALID_JSON]")
            }.trim().take(8000)
            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }

        val dto =
            runCatching { json.decodeFromString(UnitWithCarryDto.serializer(), out) }.getOrNull()
                ?: run {
                    val mergedCarry = buildString {
                        if (carry.isNotBlank()) appendLine(carry.trim())
                        appendLine("MODEL_OUTPUT_DECODE_FAILED_PREVIEW:")
                        appendLine(out.take(2000))
                    }.trim().take(8000)
                    return UnitWithCarryDto(unit = null, carry = mergedCarry)
                }

        val fixedUnit = dto.unit?.copy(
            numbering = unitNumber,
            quotations = dto.unit?.quotations?.take(2) ?: emptyList(),
            images = dto.unit?.images?.take(2) ?: emptyList(),
            exercises = emptyList()
        )

        val text = fixedUnit?.textInfo.orEmpty().trim()
        val paragraphs =
            text.split(Regex("""\n\s*\n""")).map { it.trim() }.filter { it.isNotBlank() }
        val wordCount = text.split(Regex("""\s+""")).count { it.isNotBlank() }
        val codeBlocks = Regex("```java[\\s\\S]*?```").findAll(text).count()

        if (fixedUnit != null && (paragraphs.size < 3 || wordCount < 350 || codeBlocks < 2)) {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                if (text.isNotBlank()) appendLine(text)
            }.trim().take(8000)
            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }

        return dto.copy(
            unit = fixedUnit,
            carry = dto.carry.take(8000)
        )
    }

    private suspend fun callOpenAIResponsesWithRetry(
        reqBody: String,
        maxAttempts: Int = 8,
    ): String {
        validateJsonOrThrow(reqBody)

        var attempt = 0
        var backoffMs = 1_200L

        while (true) {
            attempt++

            try {
                val resp: HttpResponse = openAiPermits.withPermit {
                    http.post("https://api.openai.com/v1/responses") {
                        header(HttpHeaders.Authorization, "Bearer $apiKey")
                        contentType(ContentType.Application.Json)
                        setBody(TextContent(reqBody, ContentType.Application.Json))
                    }
                }

                val status = resp.status.value
                val text = resp.bodyAsText()

                val retryAfterMs = resp.headers["retry-after"]?.toLongOrNull()?.times(1000)

                val (errType, errMsg) = parseOpenAiError(text)

                val ok = status in 200..299 && errMsg.isNullOrBlank()
                if (ok) return text

                val retryableStatus = status in setOf(408, 409, 425, 429, 500, 502, 503, 504)
                val retryableType = (errType == "server_error")

                val shouldRetry = (retryableStatus || retryableType) && attempt < maxAttempts
                if (shouldRetry) {
                    val jitter = Random.nextLong(0, 900)
                    val sleep = (retryAfterMs ?: (backoffMs + jitter)).coerceAtMost(35_000L)
                    delay(sleep)
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }

                error("OpenAI Responses HTTP $status: ${errMsg ?: text.take(800)}")

            } catch (t: Throwable) {
                val retryable =
                    t is java.io.IOException ||
                            t is IOException ||
                            t is SocketTimeoutException ||
                            t is HttpRequestTimeoutException

                if (retryable && attempt < maxAttempts) {
                    val jitter = Random.nextLong(0, 900)
                    delay((backoffMs + jitter).coerceAtMost(35_000L))
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }
                throw t
            }
        }
    }

    private suspend fun callOpenAIImagesWithRetry(
        reqObj: ImageGenRequest,
        maxAttempts: Int = 8,
    ): String {
        var attempt = 0
        var backoffMs = 1_200L

        while (true) {
            attempt++

            try {
                val resp: HttpResponse = openAiPermits.withPermit {
                    http.post("https://api.openai.com/v1/images/generations") {
                        header(HttpHeaders.Authorization, "Bearer $apiKey")
                        contentType(ContentType.Application.Json)
                        setBody(reqObj)
                    }
                }

                val status = resp.status.value
                val text = resp.bodyAsText()

                val retryAfterMs = resp.headers["retry-after"]?.toLongOrNull()?.times(1000)

                val (errType, errMsg) = parseOpenAiError(text)
                val ok = status in 200..299 && errMsg.isNullOrBlank()
                if (ok) return text

                val retryableStatus = status in setOf(408, 409, 425, 429, 500, 502, 503, 504)
                val retryableType = (errType == "server_error")
                val shouldRetry = (retryableStatus || retryableType) && attempt < maxAttempts

                if (shouldRetry) {
                    val jitter = Random.nextLong(0, 900)
                    val sleep = (retryAfterMs ?: (backoffMs + jitter)).coerceAtMost(35_000L)
                    delay(sleep)
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }

                error("OpenAI Images HTTP $status: ${errMsg ?: text.take(800)}")

            } catch (t: Throwable) {
                val retryable =
                    t is java.io.IOException ||
                            t is IOException ||
                            t is SocketTimeoutException ||
                            t is HttpRequestTimeoutException

                if (retryable && attempt < maxAttempts) {
                    val jitter = Random.nextLong(0, 900)
                    delay((backoffMs + jitter).coerceAtMost(35_000L))
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }
                throw t
            }
        }
    }

    private fun isValidJson(raw: String): Boolean {
        return try {
            json.parseToJsonElement(raw)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun parseOpenAiError(text: String): Pair<String?, String?> {
        return runCatching {
            val root = json.parseToJsonElement(text).jsonObject
            val err = root["error"]?.jsonObject
            val msg = err?.get("message")?.jsonPrimitive?.content
            val typ = err?.get("type")?.jsonPrimitive?.content
            typ to msg
        }.getOrNull() ?: (null to null)
    }

    private suspend fun uploadPdfToOpenAI(pdfFile: File): String {
        val respText: String = openAiPermits.withPermit {
            val resp = http.submitFormWithBinaryData(
                url = "https://api.openai.com/v1/files",
                formData = formData {
                    append("purpose", "user_data")
                    append(
                        key = "file",
                        value = pdfFile.readBytes(),
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "application/pdf")
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${pdfFile.name}\""
                            )
                        }
                    )
                }
            ) { header(HttpHeaders.Authorization, "Bearer $apiKey") }

            resp.bodyAsText()
        }

        val root = json.parseToJsonElement(respText).jsonObject
        val id = root["id"]?.jsonPrimitive?.content
        if (id.isNullOrBlank()) error("OpenAI /files upload failed: ${respText.take(600)}")
        return id
    }

    private fun buildResponsesRequestWithPdfFile(
        model: String,
        fileId: String,
        system: String,
        user: String,
        schemaOnly: String,
        maxOutputTokens: Int,
    ): String = """
        {
          "model": ${json.encodeToString(model)},
          "input": [
            { "role": "system", "content": [ { "type": "input_text", "text": ${
        json.encodeToString(
            system
        )
    } } ] },
            {
              "role": "user",
              "content": [
                { "type": "input_file", "file_id": ${json.encodeToString(fileId)} },
                { "type": "input_text", "text": ${json.encodeToString(user)} }
              ]
            }
          ],
          "max_output_tokens": $maxOutputTokens,
          "text": {
            "format": {
              "type": "json_schema",
              "name": "schema",
              "strict": true,
              "schema": $schemaOnly
            }
          }
        }
        """.trim()

    private fun buildResponsesRequest(
        model: String,
        system: String,
        user: String,
        schemaOnly: String,
        maxOutputTokens: Int,
    ): String = """
        {
          "model": ${json.encodeToString(model)},
          "input": [
            { "role": "system", "content": ${json.encodeToString(system)} },
            { "role": "user", "content": ${json.encodeToString(user)} }
          ],
          "max_output_tokens": $maxOutputTokens,
          "text": {
            "format": {
              "type": "json_schema",
              "name": "schema",
              "strict": true,
              "schema": $schemaOnly
            }
          }
        }
        """.trim()

    private fun ensureCompleted(respText: String) {
        val root = json.parseToJsonElement(respText).jsonObject

        val status = root["status"]?.jsonPrimitive?.content
        if (status == "incomplete") {
            val reason = (root["incomplete_details"] as? JsonObject)
                ?.get("reason")?.jsonPrimitive?.content
            error("OpenAI returned incomplete response (reason=${reason ?: "unknown"})")
        }

        val errMsg = (root["error"] as? JsonObject)
            ?.get("message")?.jsonPrimitive?.content
        if (!errMsg.isNullOrBlank()) error("OpenAI error: $errMsg")
    }

    private fun validateJsonOrThrow(raw: String) {
        try {
            json.parseToJsonElement(raw)
        } catch (e: Throwable) {
            val head = raw.take(1500)
            val tail = raw.takeLast(1500)
            throw IllegalStateException(
                "REQUEST JSON IS INVALID: ${e.message}\n\n---HEAD---\n$head\n\n---TAIL---\n$tail"
            )
        }
    }

    private fun extractOutputJson(responsesJson: String): String {
        val root = json.parseToJsonElement(responsesJson).jsonObject
        val output = root["output"]?.jsonArray ?: error("No output array in response")

        for (item in output) {
            val obj = item.jsonObject
            if (obj["type"]?.jsonPrimitive?.content != "message") continue
            val contentArr = obj["content"]?.jsonArray ?: continue

            for (c in contentArr) {
                val co = c.jsonObject
                val t = co["type"]?.jsonPrimitive?.content ?: continue
                if (t == "output_json" || t == "json") {
                    co["json"]?.let { return it.toString() }
                    co["data"]?.let { return it.toString() }
                }
            }

            for (c in contentArr) {
                val co = c.jsonObject
                if (co["type"]?.jsonPrimitive?.content == "output_text") {
                    return co["text"]?.jsonPrimitive?.content ?: error("No text in output_text")
                }
            }
        }
        error("No output_json/json or output_text found")
    }

    private fun unitWithCarrySchemaJson(): String = """
        {
          "type":"object",
          "additionalProperties":false,
          "required":["unit","carry"],
          "properties":{
            "unit":{
              "type":["object","null"],
              "additionalProperties":false,
              "required":["title","numbering","text","quotations","images","exercises"],
              "properties":{
                "title":{"type":"string","minLength":3},
                "numbering":{"type":"integer","minimum":1},
                "text":{"type":"string","minLength":2400,"maxLength":5200,"pattern":"(?s).*\\n\\n.*"},
                "quotations":{
                  "type":"array",
                  "minItems":0,
                  "maxItems":2,
                  "items":{
                    "type":"object",
                    "additionalProperties":false,
                    "required":["text","numbering"],
                    "properties":{
                      "text":{"type":["string","null"]},
                      "numbering":{"type":["integer","null"]}
                    }
                  }
                },
                "images":{
                  "type":"array",
                  "minItems":0,
                  "maxItems":2,
                  "items":{
                    "type":"object",
                    "additionalProperties":false,
                    "required":["relativePath","caption","numbering","prompt"],
                    "properties":{
                      "relativePath":{"type":["string","null"]},
                      "caption":{"type":["string","null"]},
                      "numbering":{"type":["integer","null"]},
                      "prompt":{"type":["string","null"]}
                    }
                  }
                },
                "exercises":{
                  "type":"array",
                  "maxItems":0,
                  "items":{"type":"object","additionalProperties":false,"properties":{}}
                }
              }
            },
            "carry":{"type":"string"}
          }
        }
        """.trimIndent()

    private fun extractFirstJsonObjectOrNull(raw: String): String? {
        val start = raw.indexOf('{')
        if (start < 0) return null

        var i = start
        var depth = 0
        var inString = false
        var escape = false

        while (i < raw.length) {
            val ch = raw[i]

            if (inString) {
                if (escape) {
                    escape = false
                } else {
                    when (ch) {
                        '\\' -> escape = true
                        '"' -> inString = false
                    }
                }
            } else {
                when (ch) {
                    '"' -> inString = true
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return raw.substring(start, i + 1)
                    }
                }
            }
            i++
        }
        return null
    }

    private fun unwrapIfJsonString(raw: String): String {
        val trimmed = raw.trim()
        return try {
            val el = json.parseToJsonElement(trimmed)
            if (el is JsonPrimitive && el.isString) el.content else trimmed
        } catch (_: Throwable) {
            trimmed
        }
    }

    private fun coerceToJsonObjectString(s: String): String {
        val start = s.indexOf('{')
        val end = s.lastIndexOf('}')
        return if (start >= 0 && end > start) s.substring(start, end + 1) else s
    }

    private fun unitExercisesOnlySchemaJson(): String = """
        {
          "type":"object",
          "additionalProperties":false,
          "required":["exercises"],
          "properties":{
            "exercises":{
              "type":"array",
              "minItems":16,
              "maxItems":30,
              "items":{
                "type":"object",
                "additionalProperties":false,
                "required":["type","difficulty","estimatedMinutes","question","options","answerIndex","hint","explanation","solution"],
                "properties":{
                  "type":{"type":"string","enum":["mcq","true_false","open"]},
                  "difficulty":{"type":"string","enum":["easy","medium","hard"]},
                  "estimatedMinutes":{"type":"integer","minimum":1,"maximum":15},
                  "question":{"type":"string","minLength":10},
                  "options":{
                    "type":"array",
                    "minItems":1,
                    "maxItems":6,
                    "items":{"type":"string","minLength":1}
                  },
                  "answerIndex":{"type":["integer","null"],"minimum":0,"maximum":5},
                  "hint":{"type":"string","minLength":5},
                  "explanation":{"type":"string","minLength":8},
                  "solution":{"type":"string","minLength":5}
                }
              }
            }
          }
        }
        """.trimIndent()

}
