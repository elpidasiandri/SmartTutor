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
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.*
import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.ImageGenRequest
import org.example.project.tutor.networkModel.uploadPdf.ImageGenResponse
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.UnitExercisesOnlyDto
import org.example.project.tutor.networkModel.uploadPdf.UnitWithCarryDto
import kotlin.random.Random
import java.io.File
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
            Generate a complete exam set based ONLY on the given UNIT.

            IMPORTANT GENERATION STRATEGY:
            - To maximize reliability, generate EXACTLY 16 exercises.
            - This satisfies the required overall range 16..30.

            HARD RULES:
            - Generate exercises ONLY from UNIT_TEXT/QUOTATIONS/IMAGES_CAPTIONS above.
            - exercises MUST NOT be empty.
            - Every exercise must be fully self-contained.
            - Never assume the student remembers any previous example, previous exercise, or earlier code from the learning unit.
            - Never refer to "the previous code", "the example above", "the program shown earlier", or similar wording.

            EXACT REQUIRED COMPOSITION:
            - Exactly 6 exercises of type="mcq"
            - Exactly 5 exercises of type="true_false"
            - Exactly 5 exercises of type="open"
            - At least 2 of the open exercises MUST be a Java coding exercise
            - Exactly 2 exercises MUST have difficulty="hard"
            - All other exercises MUST have difficulty="medium"

            TIME:
            - Total estimatedMinutes MUST sum to ${targetMinutes - 3}..${targetMinutes + 3} minutes.

            SELF-CONTAINED EXERCISE RULES:
            - Every exercise must be answerable by reading only that exercise.
            - If a question depends on code, include the full necessary code snippet inside the same exercise question.
            - If an exercise asks for output, bug-fixing, completion, extension, or explanation of code, the code must appear inside that same exercise.
            - Do not rely on hidden context from the unit text.
            - Do not assume that the learner remembers earlier code examples.

            OPEN EXERCISES:
            - Open exercises may be theory, reasoning, short problem solving, code analysis, or Java coding.
            - At least 1 open exercise MUST explicitly ask the student to write Java code.
            - If an open exercise asks to analyze/fix/extend/predict output of code, the full code must be included in that exercise question.

            MCQ OPTIONS:
            - MCQ may have 3 to 6 options.
            - answerIndex MUST be a valid index for the options array.

            FORMAT RULES:
            - mcq: options length 3..6, answerIndex 0..(len-1)
            - true_false: options MUST be ["Σωστό","Λάθος"], answerIndex 0/1
            - open: options MUST be ["(Ελεύθερη απάντηση)"], answerIndex MUST be null
            - Every exercise MUST include hint + explanation + solution (non-empty).

            OUTPUT DISCIPLINE:
            - Do not omit any exercise.
            - Do not return fewer than 16.
            - Do not return more than 30.
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
            validateExercisesOrThrow(
                normalized,
                targetMinutes,
                tolerance = 1,
                hardMin = 50,
                hardMax = 75
            )
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
            validateExercisesOrThrow(
                normalized,
                targetMinutes,
                tolerance = 1,
                hardMin = 50,
                hardMax = 75
            )
            repaired
        }

        return validated
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

        val notMediumOrHard =
            exercises.count { it.difficulty != "medium" && it.difficulty != "hard" }
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
                    if (ex.options != listOf(
                            "Σωστό",
                            "Λάθος"
                        )
                    ) error("TF options must be [Σωστό, Λάθος] at #${idx + 1}")
                    if (ex.answerIndex !in listOf(
                            0,
                            1
                        )
                    ) error("TF answerIndex must be 0/1 at #${idx + 1}")
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

    private suspend fun repairExercisesToMeetConstraints(
        originalJson: String,
        unitText: String,
        language: String,
        fileName: String,
        targetMinutes: Int,
        schemaOnly: String,
    ): List<ExerciseDto> {
        val system = """
            MODE: UNIT_EXERCISES_REPAIR
            Return ONLY JSON matching schema (strict).
            No citations.

            GOAL:
            Fix the provided exercises so that ALL constraints are satisfied.

            IMPORTANT REPAIR STRATEGY:
            - Produce EXACTLY 16 exercises total.
            - This must satisfy the required range 16..30.

            CONSTRAINTS (must be satisfied):
            - Exactly 16 exercises total
            - Exactly 6 mcq
            - Exactly 5 true_false
            - Exactly 5 open
            - At least 1 Java coding exercise among the open exercises
            - Exactly 2 hard, all others medium
            - Total estimatedMinutes sum: ${targetMinutes - 3}..${targetMinutes + 3}
            - mcq options length 3..6, answerIndex valid
            - true_false options exactly ["Σωστό","Λάθος"]
            - open options exactly ["(Ελεύθερη απάντηση)"] and answerIndex null

            SELF-CONTAINED EXERCISE RULES:
            - Every exercise must be fully self-contained.
            - Never assume the student remembers previous examples, previous exercises, or earlier code from the learning unit.
            - If a question depends on code, include the full necessary code snippet inside the same exercise.
            - If an exercise asks for output, bug-fixing, completion, extension, or explanation of code, the code must appear inside that same exercise.
            - Do not refer to "the code above", "the previous example", "the program shown earlier", or similar wording.

            IMPORTANT:
            You may:
            - remove some exercises
            - add missing exercises
            - adjust estimatedMinutes down/up
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
            maxOutputTokens = 5200
        )

        val resp = callOpenAIResponsesWithRetry(req) ?: return emptyList()
        if (!ensureCompleted(resp)) return emptyList()

        val raw = extractOutputJson(resp) ?: return emptyList()
        val fixedOut = coerceToJsonObjectString(raw)

        return runCatching {
            json.decodeFromString(UnitExercisesOnlyDto.serializer(), fixedOut).exercises
        }.getOrElse {
            onRawHook?.invoke("repair_decode_error", it.message ?: "decode failed")
            emptyList()
        }
    }

    suspend fun generateAndSaveImage(
        documentId: String,
        unitNumber: Int,
        imageIndex: Int,
        prompt: String,
    ): String? {
        val safePrompt = hardenImagePrompt(prompt)
        val reqObj = ImageGenRequest(prompt = safePrompt)
        val reqJson = json.encodeToString(ImageGenRequest.serializer(), reqObj)
        onRawHook?.invoke("image_request", reqJson)

        val respText = callOpenAIImagesWithRetry(reqObj) ?: return null
        onRawHook?.invoke("image_response", respText)

        val parsed = runCatching {
            json.decodeFromString(ImageGenResponse.serializer(), respText)
        }.getOrElse {
            onRawHook?.invoke("image_decode_error", it.message ?: "decode failed")
            return null
        }

        val b64 = parsed.data.firstOrNull()?.b64Json ?: return null
        val bytes = runCatching { Base64.getDecoder().decode(b64) }.getOrNull() ?: return null

        val dir = File("uploads/$documentId/images").apply { mkdirs() }
        val file = File(dir, "unit_${unitNumber}_img_${imageIndex}.png")
        runCatching { file.writeBytes(bytes) }.getOrElse {
            onRawHook?.invoke("image_save_error", it.message ?: "save failed")
            return null
        }

        return "uploads/$documentId/images/${file.name}"
    }

    private fun isCodingExercise(ex: ExerciseDto): Boolean {
        val q = ex.question.orEmpty().lowercase()
        val sol = ex.solution.orEmpty().lowercase()

        return q.contains("```java") ||
                sol.contains("```java") ||
                (
                        q.contains("java") && listOf(
                            "γράψε", "υλοποίησε", "κώδικ", "πρόγραμμα",
                            "write", "implement", "code", "program", "complete"
                        ).any { q.contains(it) }
                        )
    }

    private fun normalizeExercisesToTimeBudget(
        exercises: List<ExerciseDto>,
        targetMinutes: Int,
        tolerance: Int = 3,
    ): List<ExerciseDto> {
        val minTotal = targetMinutes - tolerance
        val maxTotal = targetMinutes + tolerance

        fun total(xs: List<ExerciseDto>) = xs.sumOf { it.estimatedMinutes ?: 0 }

        if (total(exercises) in minTotal..maxTotal) return exercises
        if (total(exercises) < minTotal) return exercises

        val kept = exercises.toMutableList()

        fun hasCoding(xs: List<ExerciseDto>): Boolean = xs.any(::isCodingExercise)

        fun okVariety(xs: List<ExerciseDto>): Boolean {
            val mcq = xs.count { it.type == "mcq" }
            val tf = xs.count { it.type == "true_false" }
            val op = xs.count { it.type == "open" }
            val hard = xs.count { it.difficulty == "hard" }
            return mcq == 6 && tf == 5 && op == 5 && hard == 2 && hasCoding(xs) && xs.size == 16
        }

        var i = kept.size - 1
        while (i >= 0 && total(kept) > maxTotal) {
            val candidate = kept.toMutableList().also { it.removeAt(i) }
            if (candidate.size == 16 && okVariety(candidate)) {
                kept.removeAt(i)
                i = minOf(i, kept.size - 1)
                continue
            }
            i--
        }

        var safety = 500
        while (total(kept) > maxTotal && safety-- > 0) {
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

    private fun minAllowed(ex: ExerciseDto): Int {
        return when (ex.type) {
            "mcq" -> 2
            "true_false" -> 1
            "open" -> 3
            else -> 1
        }
    }

    suspend fun generatePreviewUnitFromPdfChunkWithCarry(
        pdfChunkFile: File,
        language: String,
        fileName: String,
        unitNumber: Int,
        carry: String,
    ): UnitWithCarryDto {
        val fileId = uploadPdfToOpenAI(pdfChunkFile)
            ?: return UnitWithCarryDto(unit = null, carry = carry.take(3000))

        val schema = unitWithCarrySchemaJson()
        val captionLang = if (language.lowercase().startsWith("el")) "Greek" else "English"

        val system = """
            MODE: PREVIEW_UNIT_FROM_CHUNK_WITH_CARRY
            Return ONLY JSON matching schema (strict).
            No citations.

            GOAL:
            - Use CURRENT_CHUNK (attached PDF) + CARRY_FROM_PREVIOUS.
            - Create EXACTLY 1 small learning unit for ONE distinct concept from CURRENT_CHUNK + CARRY_FROM_PREVIOUS.

            Do NOT merge multiple concepts into one unit.
            
            If the chunk contains multiple concepts, choose the next uncovered concept and put the remaining concepts in carry.
            
            ONE CONCEPT = ONE UNIT.
            
            Examples:
            - byte = separate unit
            - short = separate unit
            - int = separate unit
            - long = separate unit
            - float = separate unit
            - double = separate unit
            - char = separate unit
            - boolean = separate unit
            - if = separate unit
            - if...else = separate unit
            - switch = separate unit
            - while = separate unit
            - do-while = separate unit
            - for = separate unit
            
            - If not enough for a full unit, return unit=null and put dense notes in carry (<=8000 chars).

            TEXT:
            - Greek (if language=el), exam-focused, no fluff.
            - Do NOT summarize.
            - Teach the concept in depth.
            - Include definitions, rules, warnings, edge cases, and examples from the PDF.
            - Include at least 2 examples.
            - Include at least 1 Java code block when the concept supports code.
            - Never say "according to the author", "in the table", "in figure", "on page", or "in this chapter".
            - Rewrite as standalone learning material.
            - 5-10 paragraphs separated by \n.
            - 350–1200 words.
            - Prefer at least 2 fenced Java code blocks when the topic supports code examples.
            - For theory-heavy topics, 1 fenced Java code block is enough.
            - Do not add an image just to satisfy formatting.

            IMAGES:
            - Include 0 or 1 educational diagram image only if it clearly helps understanding.
            - Prefer NO image over a wrong, cropped, incomplete, or confusing image.
            - images[0].relativePath = null
            - images[0].caption MUST be in $captionLang.
            - images[0].prompt MUST be in ENGLISH.
            - All diagram labels and text inside the image MUST be in ENGLISH ONLY.
            - Use short labels (1-3 words per node) to reduce text rendering errors.

            IMAGE SUITABILITY:
            - Generate an image only for simple conceptual diagrams such as taxonomy trees, type groups, operator classification, or simple control-flow diagrams.
            - Do not generate an image for code-heavy explanations, syntax summaries, or topics requiring many text labels.
            - If a correct and complete diagram is unlikely, return no image.

            LAYOUT SAFETY RULES:
            - Main idea should be centered.
            - Center the full composition in the frame.
            - Everything must fit fully inside the frame with large margins.
            - No cropped nodes, no cropped arrows, no cropped text.
            - Use large readable font.
            - Maximum 5 nodes for flowcharts.
            - Maximum 6 nodes for concept maps.
            - Maximum 8 nodes for very simple taxonomy diagrams.
            - Prefer simple straight connectors.
            - Avoid crossing lines.
            - Ensure the full diagram is visible.
            - Prefer square or landscape composition if portrait risks cropping.
            - Reduce content instead of shrinking text.

            TEXT RENDERING RULES:
            - Use only uppercase/lowercase English labels.
            - Avoid long sentences inside the diagram.
            - Do not include paragraphs inside the image.
            - Do not include code syntax unless the image is specifically a syntax chart.
            - For syntax charts, use large simple text and very few examples.

            SELF-CONTAINED EXPLANATION RULES:
            - Every explanation paragraph must be self-contained.
            - Never refer to "previous example", "above code", "the code we saw earlier", or "as mentioned before".
            - If you include multiple Java code examples, each code block MUST be independently understandable.
            - Do not assume the learner remembers previous code blocks.
            - If a later example needs context, repeat the necessary code/context briefly inside that example's explanation.

            CODE EXAMPLE INDEPENDENCE:
            - Each Java code block is a standalone example.
            - Never create an example that depends on variables, methods, or classes defined only in a previous example unless they are repeated again in the same example.
            - Avoid phrases like "continue the previous program" or "modify the code above".

            DIAGRAM QUALITY RULES:
            - The diagram MUST represent concepts exactly as described in the UNIT_TEXT.
            - Do NOT invent new concepts not present in the text.
            - Do NOT duplicate labels.
            - Every label must appear only once unless repetition is logically required.
            - Ensure all major concepts mentioned in the explanation are represented.
            - Relationships between nodes must be logical.
            - Do not omit key nodes.
            - Do not create invalid parent-child relationships.

            TARGET COVERAGE:
            - Across the whole book, aim to cover the major exam topics.
            - Prefer creating a unit whenever you see a new major topic heading, rule, or algorithm.
            - Avoid merging unrelated topics into one unit.

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
            ${carry.take(3000)}

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
            ?: return UnitWithCarryDto(unit = null, carry = carry.take(3000))

        onRawHook?.invoke("preview_response", resp)

        if (!ensureCompleted(resp)) {
            return UnitWithCarryDto(unit = null, carry = carry.take(3000))
        }

        val raw = extractOutputJson(resp)
            ?: return UnitWithCarryDto(unit = null, carry = carry.take(3000))

        val unwrapped = unwrapIfJsonString(raw)
        val out = extractFirstJsonObjectOrNull(unwrapped)

        if (out == null || !isValidJson(out)) {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                appendLine("[PREVIEW_INVALID_JSON]")
            }.trim().take(3000)
            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }

        val dto = runCatching {
            json.decodeFromString(UnitWithCarryDto.serializer(), out)
        }.getOrElse {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                appendLine("MODEL_OUTPUT_DECODE_FAILED_PREVIEW:")
                appendLine(out.take(2000))
            }.trim().take(3000)
            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }

        val fixedUnit = dto.unit?.copy(
            numbering = unitNumber,
            quotations = dto.unit?.quotations?.take(2) ?: emptyList(),
            images = dto.unit?.images?.take(2) ?: emptyList(),
            exercises = emptyList()
        )

        val text = fixedUnit?.textInfo.orEmpty().trim()
        val paragraphs = text.split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val wordCount = text.split(Regex("""\s+""")).count { it.isNotBlank() }
        val codeBlocks = Regex("```java[\\s\\S]*?```").findAll(text).count()

        val hasEnoughText = paragraphs.size >= 2 && wordCount >= 180
        val hasCodeOrEnoughTheory = (codeBlocks >= 1) || (wordCount >= 260)

        if (fixedUnit != null && (!hasEnoughText || !hasCodeOrEnoughTheory)) {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                if (text.isNotBlank()) appendLine(text)
            }.trim().take(3000)
            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }

        return dto.copy(
            unit = fixedUnit,
            carry = dto.carry.take(3000)
        )
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

            } catch (t: Throwable) {
                val retryable = t is java.io.IOException

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
    ): String? {
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

                onRawHook?.invoke(
                    "images_failed",
                    "status=$status type=${errType ?: "unknown"} msg=${errMsg ?: text.take(1200)}"
                )
                return null
            } catch (t: Throwable) {
                val retryable = t is java.io.IOException

                if (retryable && attempt < maxAttempts) {
                    val jitter = Random.nextLong(0, 900)
                    delay((backoffMs + jitter).coerceAtMost(35_000L))
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }

                onRawHook?.invoke("images_exception", t.message ?: t::class.simpleName.orEmpty())
                return null
            }
        }
    }

    private fun hardenImagePrompt(prompt: String): String = """
        $prompt

        Create a clean educational diagram.

        MANDATORY LAYOUT RULES:
        - Main idea centered in the image
        - Center the full composition in the frame
        - Symmetric composition around the center when possible
        - Large empty margins on all four sides
        - Keep every node fully inside the frame
        - Keep all arrows fully inside the frame
        - Do not place text near edges
        - Do not crop any title, label, box, arrow, or shape
        - Prefer square or landscape composition over tall portrait composition
        - Use a simple centered layout
        - Prefer fewer nodes rather than dense content
        - Maximum 5 nodes for flowcharts
        - Maximum 6 nodes for concept maps
        - Maximum 8 nodes for very simple taxonomy diagrams
        - Maximum 2 short words per label
        - Large readable text only
        - If content does not fit, reduce content instead of shrinking text
        - The entire diagram must be visible at once
        - Place the main concept in the visual center
        - Keep the whole diagram as a single centered cluster
        - Avoid top-heavy or bottom-heavy layouts
        - Avoid long vertical chains unless there are at most 3 nodes
        
        TEXT RULES:
        - ENGLISH labels only
        - Very short labels
        - No paragraphs
        - No long sentences
        - No tiny text

        QUALITY RULES:
        - no cropped text
        - no cropped arrows
        - no missing nodes
        - no duplicated nodes
        - simple structure
        - accurate logical relationships only
        - everything fully visible inside the frame
        - if the diagram is complex, simplify it
    """.trimIndent()

    private fun isValidJson(raw: String): Boolean {
        return runCatching {
            json.parseToJsonElement(raw)
            true
        }.getOrDefault(false)
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

    private suspend fun uploadPdfToOpenAI(pdfFile: File): String? {
        val respText = runCatching {
            openAiPermits.withPermit {
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
                ) {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                }

                resp.bodyAsText()
            }
        }.getOrElse {
            onRawHook?.invoke("pdf_upload_exception", it.message ?: "upload failed")
            return null
        }

        val root = runCatching { json.parseToJsonElement(respText).jsonObject }.getOrNull()
            ?: return null

        return root["id"]?.jsonPrimitive?.content
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

    private fun ensureCompleted(respText: String): Boolean {
        val root =
            runCatching { json.parseToJsonElement(respText).jsonObject }.getOrNull()
                ?: return false

        val status = root["status"]?.jsonPrimitive?.content

        if (status == "incomplete") {
            val reason = (root["incomplete_details"] as? JsonObject)
                ?.get("reason")?.jsonPrimitive?.content
            return false
        }

        val errMsg = (root["error"] as? JsonObject)
            ?.get("message")?.jsonPrimitive?.content

        if (!errMsg.isNullOrBlank()) {
            return false
        }

        return true
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
                  "difficulty":{"type":"string","enum":["medium","hard"]},
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

    suspend fun answerQuestionForUnit(
        language: String,
        fileName: String,
        unitTitle: String,
        contextText: String,
        tab: String,
        recentMessages: List<ChatMessageDto>,
        userQuestion: String,
    ): String {
        val answerLanguage = if (language.lowercase().startsWith("el")) {
            "Απάντησε στα Ελληνικά."
        } else {
            "Answer in English."
        }


        val recentHistory = recentMessages.takeLast(10).joinToString("\n") { msg ->
            "${msg.sender}: ${msg.text}"
        }

        val system = """
        You are a helpful tutor.
        
        MAIN RULE:
        - Answer based on the student's uploaded material.
        - Prefer the CURRENT UNIT when it is relevant.
        - If the student asks something that relates to another unit from the same material, you may use that broader material context.
        - Stay as specific as possible to the student's question.
        - answerLanguage = $answerLanguage


        TEACHING STYLE:
        - First explain the idea simply.
        - Then, if a Java example would help, include a short self-contained example.
        - After the code, explain what happens step by step.
        - If you think that with a code example the user will understand, give an example and then explain the code step by step.
        
        EXERCISES:
        - If the student asks about an exercise, guide them with hints first.
        
        CODE FORMAT:
        -  Αν γράψεις κώδικα, βάλ' τον ΠΑΝΤΑ μέσα σε markdown code block με γλώσσα.
            Παράδειγμα:
            ```java
            int counter = 0;
            while (counter < 5) {
                counter++;
            }
            ```
    """.trimIndent()

        val user = """
        FILE NAME:
        $fileName

        UNIT TITLE:
        $unitTitle

        CURRENT TAB:
        $tab

        CURRENT_CONTEXT:
        $contextText

        RECENT CHAT HISTORY:
        $recentHistory

        STUDENT QUESTION:
        $userQuestion
    """.trimIndent()

        val req = buildTextResponsesRequest(
            model = "gpt-4o-mini",
            system = system,
            user = user,
            maxOutputTokens = 500
        )

        onRawHook?.invoke("tutor_answer_request", req)

        val resp = callOpenAIResponsesWithRetry(req)
        onRawHook?.invoke("tutor_answer_response", resp)

        if (!ensureCompleted(resp)) {
            return if (language.lowercase().startsWith("el")) {
                "Δεν μπόρεσα να δημιουργήσω απάντηση αυτή τη στιγμή."
            } else {
                "I could not generate an answer at this time."
            }
        }

        return extractOutputText(resp).trim().ifBlank {
            if (language.lowercase().startsWith("el")) {
                "Δεν βρέθηκε απάντηση."
            } else {
                "No answer was generated."
            }
        }
    }

    private fun buildTextResponsesRequest(
        model: String,
        system: String,
        user: String,
        maxOutputTokens: Int,
    ): String = """
        {
          "model": ${json.encodeToString(model)},
          "input": [
            {
              "role": "system",
              "content": [
                {
                  "type": "input_text",
                  "text": ${json.encodeToString(system)}
                }
              ]
            },
            {
              "role": "user",
              "content": [
                {
                  "type": "input_text",
                  "text": ${json.encodeToString(user)}
                }
              ]
            }
          ],
          "max_output_tokens": $maxOutputTokens
        }
        """.trim()

    private fun extractOutputText(responsesJson: String): String {
        val root = json.parseToJsonElement(responsesJson).jsonObject

        root["output_text"]?.jsonPrimitive?.contentOrNull?.let {
            if (it.isNotBlank()) return it.trim()
        }

        val output = root["output"]?.jsonArray ?: return ""

        val parts = mutableListOf<String>()

        for (item in output) {
            val obj = item.jsonObject
            val contentArr = obj["content"]?.jsonArray ?: continue

            for (c in contentArr) {
                val co = c.jsonObject
                val type = co["type"]?.jsonPrimitive?.content ?: continue

                if (type == "output_text" || type == "text") {
                    val text = co["text"]?.jsonPrimitive?.contentOrNull
                    if (!text.isNullOrBlank()) {
                        parts += text
                    }
                }
            }
        }

        return parts.joinToString("\n").trim()
    }

}
