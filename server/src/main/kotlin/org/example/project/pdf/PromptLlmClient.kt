package org.example.project.pdf

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import java.io.File
import io.ktor.client.request.forms.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.tutor.networkModel.uploadPdf.UnitWithCarryDto
import kotlin.random.Random

class PromptLlmClient(
    private val apiKey: String,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {
    companion object {
        private val openAiMutex = Mutex()
    }

    // tags: preview_request/preview_response/exercises_request/exercises_response/image_request/image_response
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

    // EXERCISES (ONLY from unit)
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

HARD RULES:
- Generate exercises ONLY from UNIT_TEXT/QUOTATIONS/IMAGES_CAPTIONS above.
- Do NOT ask anything not present there.
- exercises MUST NOT be empty.
- Total time target: $targetMinutes minutes (sum estimatedMinutes within ±3).

MIX (target user: medium):
- 8-10 MCQ (concept + λεπτομέρειες από κείμενο)
- 5-6 true_false (παγίδες/ορισμοί)
- 6 open/problems (to do something, to write code, to answer a question) (σύντομες ερωτήσεις κρίσεως/αιτιολόγησης η και να λυσει προβληματα, η να γραψει κωδικα)

DIFFICULTY:
- Mostly medium
- Exactly 2 hard (mark difficulty="hard")
- Rest medium (or a couple easy)

TIME:
- Total time target: $targetMinutes minutes (sum estimatedMinutes within ±3).

FORMAT RULES:
- mcq: 4 options, answerIndex 0..3
- true_false: options MUST be ["Σωστό","Λάθος"], answerIndex 0/1
- open: options [], answerIndex null
- Every exercise MUST include hint + explanation + solution (always non-empty).
- Questions MUST be answerable ONLY from UNIT_TEXT/QUOTATIONS/IMAGES_CAPTIONS above.
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

        val resp = callOpenAIWithRetry(req)
        onRawHook?.invoke("exercises_response", resp)
        ensureCompleted(resp)

        val raw = extractOutputJson(resp)
        val out = coerceToJsonObjectString(raw)

        val dto = json.decodeFromString(UnitExercisesOnlyDto.serializer(), out)
        if (dto.exercises.isEmpty()) error("LLM returned empty exercises")
        return dto.exercises
    }

    @Serializable
    data class UnitExercisesOnlyDto(
        @SerialName("exercises") val exercises: List<ExerciseDto> = emptyList(),
    )

    // -------------------------
    // IMAGES (generate + save)
    // -------------------------
    @Serializable
    private data class ImageGenRequest(
        val model: String = "gpt-image-1",
        val prompt: String,
        val size: String = "1024x1024",
    )

    @Serializable
    private data class ImageGenResponse(
        val data: List<ImageGenData> = emptyList(),
    )

    @Serializable
    private data class ImageGenData(
        @SerialName("b64_json") val b64Json: String? = null,
    )

    suspend fun generateAndSaveImage(
        documentId: String,
        unitNumber: Int,
        imageIndex: Int,
        prompt: String,
    ): String {
        val reqObj = ImageGenRequest(prompt = prompt)
        val reqJson = json.encodeToString(ImageGenRequest.serializer(), reqObj)
        onRawHook?.invoke("image_request", reqJson)

        val respText: String = openAiMutex.withLock {
            val resp = http.post("https://api.openai.com/v1/images/generations") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(reqObj)
            }
            resp.bodyAsText()
        }

        onRawHook?.invoke("image_response", respText)

        val parsed = json.decodeFromString(ImageGenResponse.serializer(), respText)
        val b64 =
            parsed.data.firstOrNull()?.b64Json ?: error("No b64_json returned from images API")
        val bytes = java.util.Base64.getDecoder().decode(b64)

        val dir = File("uploads/$documentId/images").apply { mkdirs() }
        val file = File(dir, "unit_${unitNumber}_img_${imageIndex}.png")
        file.writeBytes(bytes)

        return "uploads/$documentId/images/${file.name}"
    }
    private fun unitExercisesOnlySchemaJson(): String = """
{
  "type":"object",
  "additionalProperties":false,
  "required":["exercises"],
  "properties":{
    "exercises":{
      "type":"array",
      "minItems":8,
      "items":{
        "type":"object",
        "additionalProperties":false,
        "required":[
          "type","difficulty","estimatedMinutes","question",
          "options","answerIndex","hint","explanation","solution"
        ],
        "properties":{
          "type":{"type":"string","enum":["mcq","true_false","open"]},
          "difficulty":{"type":["string","null"],"enum":["easy","medium","hard",null]},
          "estimatedMinutes":{"type":["integer","null"]},
          "question":{"type":["string","null"]},
          "options":{"type":"array","items":{"type":"string"}},
          "answerIndex":{"type":["integer","null"]},
          "hint":{"type":["string","null"]},
          "explanation":{"type":["string","null"]},
          "solution":{"type":["string","null"]}
        }
      }
    }
  }
}
""".trimIndent()

    // -------------------------
    // OpenAI plumbing
    // -------------------------
    private suspend fun uploadPdfToOpenAI(pdfFile: File): String {
        val respText: String = openAiMutex.withLock {
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
    { "role": "system", "content": [ { "type": "input_text", "text": ${json.encodeToString(system)} } ] },
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

    private suspend fun callOpenAIWithRetry(reqBody: String, maxAttempts: Int = 8): String {
        var attempt = 0
        var backoffMs = 2_000L

        while (true) {
            attempt++

            val respText: String = openAiMutex.withLock {
                val resp = http.post("https://api.openai.com/v1/responses") {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    contentType(ContentType.Application.Json)
                    setBody(reqBody)
                }
                resp.bodyAsText()
            }

            val errMsg = runCatching {
                val root = json.parseToJsonElement(respText).jsonObject
                (root["error"] as? JsonObject)?.get("message")?.jsonPrimitive?.content
            }.getOrNull()

            if (!errMsg.isNullOrBlank()) {
                val isRateLimit = errMsg.contains("rate limit", ignoreCase = true) ||
                        errMsg.contains("too many requests", ignoreCase = true)

                if (isRateLimit && attempt < maxAttempts) {
                    val jitter = Random.nextInt(0, 900).toLong()
                    delay(backoffMs + jitter)
                    backoffMs = (backoffMs * 2).coerceAtMost(35_000L)
                    continue
                }
                error("OpenAI error: $errMsg")
            }

            return respText
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
         "text":{"type":"string","minLength":2300,"maxLength":5000},
        "quotations":{
          "type":"array",
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
          "maxItems":1,
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
          "items":{
            "type":"object",
            "additionalProperties":false,
            "properties":{}
          }
        }
      }
    },
    "carry":{"type":"string"}
  }
}
""".trimIndent()

    suspend fun generatePreviewUnitFromPdfChunkWithCarry(
        pdfChunkFile: File,
        language: String,
        fileName: String,
        unitNumber: Int,
        carry: String,
    ): UnitWithCarryDto {

        val fileId = uploadPdfToOpenAI(pdfChunkFile)
        val schema = unitWithCarrySchemaJson()

        val system = """
MODE: PREVIEW_UNIT_FROM_CHUNK_WITH_CARRY
Return ONLY JSON matching schema (strict).
No citations.

TEXT FORMAT (HARD):
- Write 3–6 paragraphs.
- Separate paragraphs with a blank line (i.e., include "\n\n" between paragraphs).

GOAL:
- Use CURRENT_CHUNK (attached PDF) + CARRY_FROM_PREVIOUS (text).
- Create AT MOST 1 learning unit OR return unit=null if not enough substance.
- The unit text MUST be 3–6 paragraphs (each paragraph 2–4 sentences, no bullets).
- text length:  350–700 words (exam-focused, clear Greek if language=el)

QUOTATIONS:
- Return 0–2 quotations ONLY if the PDF contains a definition/rule that is worth quoting or you want to emphasize something.
- If you include a quotation, keep it short and exam-focused.

IMAGES:
- Return 0–2 image ONLY if a diagram/comparison/core preview would truly help (e.g. JVM/JRE/JDK pipeline).
- If image exists: relativePath MUST be null, caption in $language, prompt can be English.

EXERCISES:
- exercises MUST be [] here.

WHEN TO SKIP (use carry):
- If you cannot form a strong unit meeting the 3–6 paragraph + 650–1400 char rule:
  - unit MUST be null
  - carry MUST contain the most important exam-focused points (max ~2500 chars)
  - Then continue with next chunk.

HARD RULE:
- Do NOT create micro-units.
""".trimIndent()

        val user = """
Filename: $fileName
Language: $language

CARRY_FROM_PREVIOUS:
${carry.take(2500)}

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

        val resp = callOpenAIWithRetry(req)
        onRawHook?.invoke("preview_response", resp)
        ensureCompleted(resp)

        val raw = extractOutputJson(resp)
        val out = coerceToJsonObjectString(raw)

        val dto = json.decodeFromString(UnitWithCarryDto.serializer(), out)

        val fixedUnit = dto.unit?.copy(
            numbering = unitNumber,
            quotations = dto.unit?.quotations?.take(2) ?: emptyList(),
            images = dto.unit?.images?.take(1) ?: emptyList(),
            exercises = emptyList()
        )
        // ---- HARD VALIDATION: enforce 3+ paragraphs ----
        val text = fixedUnit?.textInfo.orEmpty().trim()

// paragraphs separated by blank line
        val paragraphs = text.split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val wordCount = text.split(Regex("""\s+"""))
            .map { it.trim() }
            .count { it.isNotBlank() }
        val minWords = 350
// If model violated paragraph rule, treat as "not enough" => move content into carry and return unit=null
        if (fixedUnit != null && (paragraphs.size < 3 || wordCount < minWords)) {
            val mergedCarry = buildString {
                if (carry.isNotBlank()) appendLine(carry.trim())
                if (text.isNotBlank()) appendLine(text)
            }.trim().take(2500)

            return UnitWithCarryDto(unit = null, carry = mergedCarry)
        }
        return dto.copy(
            unit = fixedUnit,
            carry = dto.carry.take(2500)
        )
    }

    private fun coerceToJsonObjectString(s: String): String {
        val start = s.indexOf('{')
        val end = s.lastIndexOf('}')
        return if (start >= 0 && end > start) s.substring(start, end + 1) else s
    }
}
