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
import org.example.project.tutor.networkModel.chatHistory.OpenExerciseEvaluationDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.ImageGenRequest
import org.example.project.tutor.networkModel.uploadPdf.ImageGenResponse
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.UnitExercisesOnlyDto
import org.example.project.tutor.networkModel.uploadPdf.UnitsWithCarryDto
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

    suspend fun gradeOpenExercise(
        language: String,
        fileName: String,
        unitTitle: String,
        question: String,
        suggestedSolution: String,
        userAnswer: String,
        maxPoints: Double,
        unitContext: String = "",
    ): OpenExerciseEvaluationDto {
        require(maxPoints > 0.0) {
            "maxPoints must be greater than zero"
        }
        val resolvedLanguage = language.trim().ifBlank { "en" }

        val system = """
            MODE: OPEN_EXERCISE_GRADING
        
            You are grading a learner's open-ended answer.
        
            LANGUAGE:
            - Default response language code: $resolvedLanguage
            - Write all learner-facing feedback in that language.
            - If the learner explicitly asks to receive feedback in another language,
              follow that request when clear.
            - Do not translate programming syntax, identifiers, formulas,
              technical notation, or language-specific examples when translation
              would make them incorrect.
            - gradingStatus must always remain exactly one of:
              correct, partial, wrong.
        
            GROUNDING:
            - Grade the answer using:
              1. the exercise question,
              2. the suggested solution,
              3. the CURRENT UNIT CONTEXT when provided.
            - The suggested solution is an exemplary answer, not necessarily
              the only valid wording.
            - Accept equivalent correct answers.
            - Do not require information that was not requested by the exercise.
            - Do not introduce requirements from unrelated learning units.
            - Do not penalize a learner for using different wording when the
              educational meaning is correct.
        
            SUBJECT ADAPTATION:
            - Do NOT assume the subject is Java or programming.
            - Adapt grading to the actual subject.
            - For programming exercises:
              evaluate correctness of logic, syntax and requested behavior.
              Do not require a complete program unless explicitly requested.
            - For language-learning exercises:
              evaluate only the vocabulary, grammar, meaning, structure or
              production skills actually taught and requested.
            - For theoretical subjects:
              prioritize conceptual correctness and completeness.
        
            SCORING:
            - awardedPoints must be between 0 and $maxPoints.
            - maxPoints must always be exactly $maxPoints.
        
            STATUS RULES:
            - correct:
              The answer is correct and substantially complete.
              Normally award 80% to 100% of maxPoints.
        
            - partial:
              The answer demonstrates meaningful understanding but contains
              omissions, inaccuracies or incomplete reasoning.
              awardedPoints must be greater than 0 and less than maxPoints.
        
            - wrong:
              The answer is substantially incorrect, irrelevant,
              or does not answer the exercise.
              awardedPoints must be 0.
        
            BOOLEAN RULE:
            - isCorrect = true only when gradingStatus = correct.
            - isCorrect = false for partial and wrong.
        
            FEEDBACK:
            - Explain briefly what the learner did correctly.
            - Explain what is missing or incorrect.
            - Be specific to this exercise.
            - improvementSuggestion should explain what to improve,
              without introducing unrelated material.
            - exampleAnswer should be provided when it would genuinely help.
            - Do not mention internal grading rules or enum names.
            - Do not write GRADE:, STATUS:, RESULT:, or similar metadata
              inside feedback.
            - Do not wrap the whole response in Markdown code fences.
        """.trimIndent()

        val user = """
            DOCUMENT:
            $fileName
        
            LEARNING UNIT:
            $unitTitle
        
            CURRENT UNIT CONTEXT:
            ${unitContext.take(12_000)}
        
            EXERCISE:
            $question
        
            SUGGESTED SOLUTION:
            $suggestedSolution
        
            STUDENT ANSWER:
            $userAnswer
        
            MAXIMUM POINTS:
            $maxPoints
        """.trimIndent()

        val request = buildResponsesRequest(
            model = "gpt-4o-mini",
            system = system,
            user = user,
            schemaOnly = openExerciseEvaluationSchemaJson(),
            maxOutputTokens = 1400,
        )

        onRawHook?.invoke(
            "open_exercise_grade_request",
            request
        )

        val response = callOpenAIResponsesWithRetry(request)

        onRawHook?.invoke(
            "open_exercise_grade_response",
            response
        )

        if (!ensureCompleted(response)) {
            error("Open exercise grading response was incomplete")
        }

        val rawOutput = extractOutputJson(response)
        val jsonOutput = coerceToJsonObjectString(rawOutput)

        val parsed = json.decodeFromString(
            OpenExerciseEvaluationDto.serializer(),
            jsonOutput
        )

        val safeStatus = when (
            parsed.gradingStatus.trim().lowercase()
        ) {
            "correct" -> "correct"
            "partial" -> "partial"
            "wrong" -> "wrong"
            else -> "wrong"
        }

        val safePoints = parsed.awardedPoints.coerceIn(
            minimumValue = 0.0,
            maximumValue = maxPoints
        )

        val normalizedPoints = when (safeStatus) {
            "wrong" -> 0.0

            "partial" -> {
                when {
                    maxPoints <= 0.0 -> 0.0
                    safePoints <= 0.0 -> minOf(maxPoints / 2.0, maxPoints)
                    safePoints >= maxPoints -> maxPoints * 0.75
                    else -> safePoints
                }
            }

            else -> safePoints
        }

        return parsed.copy(
            awardedPoints = normalizedPoints,
            maxPoints = maxPoints,
            gradingStatus = safeStatus,
            isCorrect = safeStatus == "correct",
        )
    }

    private fun openExerciseEvaluationSchemaJson(): String {
        return """
        {
          "type": "object",
          "additionalProperties": false,
          "properties": {
            "awardedPoints": {
              "type": "number"
            },
            "maxPoints": {
              "type": "number"
            },
            "gradingStatus": {
              "type": "string",
              "enum": [
                "correct",
                "partial",
                "wrong"
              ]
            },
            "isCorrect": {
              "type": "boolean"
            },
            "feedback": {
              "type": "string"
            },
            "improvementSuggestion": {
              "type": [
                "string",
                "null"
              ]
            },
            "exampleAnswer": {
              "type": [
                "string",
                "null"
              ]
            }
          },
          "required": [
            "awardedPoints",
            "maxPoints",
            "gradingStatus",
            "isCorrect",
            "feedback",
            "improvementSuggestion",
            "exampleAnswer"
          ]
        }
    """.trimIndent()
    }

    suspend fun generateAdaptiveExercisesForUnit(
        unit: LearningUnitDto,
        language: String,
        fileName: String,
        adaptiveFocus: String,
        targetMinutes: Int = 55,
    ): List<ExerciseDto> {
        val schema = adaptiveExercisesOnlySchemaJson()
        val unitText = buildString {
            appendLine("UNIT_TITLE:")
            appendLine(unit.title)

            appendLine()
            appendLine("UNIT_TEXT:")
            appendLine(unit.textInfo.orEmpty())

            appendLine()
            appendLine("EXISTING_EXERCISES:")
            unit.exercises.forEachIndexed { index, ex ->
                appendLine("Exercise ${index + 1}:")
                appendLine("Question: ${ex.question.orEmpty()}")
                appendLine("Solution: ${ex.solution.orEmpty()}")
                appendLine()
            }

            appendLine("STUDENT_WEAKNESSES:")
            appendLine(
                adaptiveFocus.ifBlank {
                    "No incorrect attempts are available. Generate new medium/hard practice based only on UNIT_TEXT."
                }
            )
        }.take(30_000)

        val system = """
    MODE: ADAPTIVE_UNIT_EXERCISES
    Return ONLY JSON matching schema.

    GOAL:
    Generate additional exercises for the CURRENT learning unit.

    PRIMARY SOURCE:
    - Use the UNIT_TEXT as the main educational source.
    - Every generated exercise must be answerable from concepts taught in UNIT_TEXT.
    - EXISTING_EXERCISES may be used to understand the style and concepts already tested.
    - Do NOT introduce concepts that are not taught in UNIT_TEXT.

    STUDENT PERFORMANCE:
    - STUDENT_WEAKNESSES contains information about exercises the student answered incorrectly or partially.
    - Give higher priority to concepts where the student made mistakes.
    - Create NEW exercises targeting those misconceptions.
    - Do NOT simply repeat the same question.
    - Test the same concept using a different example, wording, scenario, or reasoning path.

    DIFFICULTY ADAPTATION:
    - Every generated exercise must have difficulty = "medium" or "hard".
    - Do not generate easy exercises.
    - Exercises targeting a previously wrong concept should first test
      whether the learner now understands the concept and then move toward
      more demanding application or reasoning.
    - Prefer hard exercises when the student's previous performance shows
      that the underlying concept has already been partially understood.
    - When the student has mistakes, progressively increase difficulty after reinforcing the weak concept.
    - When the student has answered everything correctly, generate harder exercises that deepen understanding.
    - If there is no performance history, generate balanced medium/hard practice based on UNIT_TEXT.

    REQUIRED OUTPUT:
    - Generate EXACTLY 12 new exercises.
    - Include mcq, true_false and open exercises.
    - At least 3 exercises must be open.
    - Do not duplicate existing exercises.

    SUBJECT ADAPTATION:
    - Adapt exercises to the actual subject of UNIT_TEXT.

    - For language-learning material:
      use vocabulary, grammar, sentence construction, ordering,
      translation, comprehension or production tasks when appropriate.

    - For programming material:
      use code reading, debugging, reasoning and code-writing when appropriate.
      At least one open exercise should require writing code when UNIT_TEXT teaches programming.
      Use the programming language appearing in UNIT_TEXT.
      NEVER assume Java.

    - For non-programming material:
      do NOT generate programming exercises.

    SELF-CONTAINED:
    - Every exercise must be understandable on its own.
    - Do not refer to "the previous exercise", "the code above",
      or material that is not included in the question.
    - If code is needed to answer the exercise, include the necessary code
      inside the same question.

    HINT:
    - Every exercise must include a useful hint.
    - The hint should guide the learner toward the relevant concept
      without revealing the answer.

    SOLUTION:
    - Every exercise must contain a correct solution.
    - Open exercises must contain an exemplary expected answer.
    - Programming solutions must contain code when code is required.

    FORMAT:
    - mcq: options length 3..6 and valid answerIndex.
    - true_false: exactly 2 localized True/False options and answerIndex 0 or 1.
    - open: one localized free-response placeholder and answerIndex null.
    - Every exercise must include hint, explanation and solution.

    CODE FORMAT:
    - If code is included, always use Markdown fenced code blocks.
    - Use the correct programming-language tag.
    - Never assume Java.

    LANGUAGE:
    - Generate all learner-facing content in language code: $language
    - Keep programming keywords, identifiers and code syntax unchanged.
""".trimIndent()
        val user = """
        Filename: $fileName
        Language: $language

        $unitText
    """.trimIndent()

        val req = buildResponsesRequest(
            model = "gpt-4o-mini",
            system = system,
            user = user,
            schemaOnly = schema,
            maxOutputTokens = 4800
        )

        val resp = callOpenAIResponsesWithRetry(req)

        if (!ensureCompleted(resp)) {
            error("Adaptive exercise response was incomplete")
        }

        val raw = extractOutputJson(resp)
        val out = coerceToJsonObjectString(raw)

        val exercises = json.decodeFromString(
            UnitExercisesOnlyDto.serializer(),
            out
        ).exercises

        val normalizedExercises = normalizeExercisePoints(
            exercises
        )

        validateAdaptiveExercisesOrThrow(
            normalizedExercises
        )

        return normalizedExercises
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

            EXERCISE GENERATION RULES:

            Generate EXACTLY 20 exercises for this learning unit.

            DIFFICULTY DISTRIBUTION:
            - Exactly 4 exercises with difficulty = "easy"
            - Exactly 8 exercises with difficulty = "medium"
            - Exactly 8 exercises with difficulty = "hard"

            SOURCE GROUNDING:
            - EVERY exercise must be based ONLY on this learning unit.
            - Do not introduce concepts that do not appear in UNIT_TEXT, quotations or image captions.
            - Avoid generic questions that could fit unrelated learning units.

            EXERCISE TYPES:
            - Use a pedagogically appropriate mix of:
              mcq
              true_false
              open
            - Open-ended exercises MUST be included.

            SUBJECT ADAPTATION:
            - If the material is language learning, generate vocabulary, ordering, grammar, sentence creation, translation or comprehension exercises when appropriate.
            - If the material is programming, include code-reading, debugging, reasoning and code-writing exercises.
            - If programming is taught in this unit, at least one open exercise MUST require writing code.
            - Use the programming language that appears in the learning unit.
            - NEVER assume Java.
            - For non-programming material, do NOT generate programming exercises.

            DIFFICULTY:
            - easy = direct recall, recognition or very simple application
            - medium = understanding, interpretation, comparison or multi-step application
            - hard = deeper reasoning, synthesis, problem solving, debugging or production

            HINT:
            - Every exercise must have a useful hint.
            - The hint must guide the learner without revealing the answer.

            SOLUTION:
            - Every exercise must include a correct solution.
            - Open exercises must include an exemplary expected answer.
            - Programming solutions must include code when code is required.

            SCORING:
            - easy: maxPoints = 0.5
            - medium: maxPoints = 1.0
            - hard: maxPoints = 1.25
            - Total maxPoints of all 20 exercises MUST equal exactly 20.0.

            TIME:
            - estimatedMinutes must be realistic for an average learner.
            - Easy exercises should usually take less time than medium exercises.
            - Hard/open/problem-solving exercises may take more time.

            FORMAT:
            - mcq: options length 3..6, answerIndex valid
            - true_false: 2 options, answerIndex 0 or 1
            - open: one placeholder option and answerIndex null
            - Every exercise must include hint, explanation and solution.

            CODE:
            - If code is included, use markdown fenced code blocks.
            - Use the correct language tag for the programming language in the unit.
            - Example:
              ```python
              print("hello")
              ```
        """.trimIndent()

        val user = """
            Filename: $fileName
            Language: $language

            $unitText
        """.trimIndent()


        val req = buildResponsesRequest(
            model = "gpt-4o-mini",
            system = system,
            user = user,
            schemaOnly = schema,
            maxOutputTokens = 6500
        )

        onRawHook?.invoke("exercises_request", req)

        val resp = callOpenAIResponsesWithRetry(req)

        if (!ensureCompleted(resp)) {
            error("Exercise response was incomplete")
        }
        onRawHook?.invoke("exercises_response", resp)

        val raw = extractOutputJson(resp)
        val out = coerceToJsonObjectString(raw)
        val dto = json.decodeFromString(UnitExercisesOnlyDto.serializer(), out)
        val normalizedExercises = normalizeExercisePoints(
            dto.exercises
        )
        val validated = runCatching {
            validateExercisesOrThrow(
                normalizedExercises,
                targetMinutes
            )

            normalizedExercises
        }.getOrElse { err ->
            onRawHook?.invoke(
                "exercises_validation_failed",
                err.message ?: "validation failed"
            )

            val repaired = repairExercisesToMeetConstraints(
                originalJson = out,
                unitText = unitText,
                language = language,
                fileName = fileName,
                targetMinutes = targetMinutes,
                schemaOnly = schema
            )

            val normalizedRepaired = normalizeExercisePoints(
                repaired
            )

            validateExercisesOrThrow(
                normalizedRepaired,
                targetMinutes
            )

            normalizedRepaired

        }

        return validated
    }

    private fun normalizeExercisePoints(
        exercises: List<ExerciseDto>,
    ): List<ExerciseDto> {
        return exercises.map { exercise ->
            val points = when (
                exercise.difficulty
                    ?.trim()
                    ?.lowercase()
            ) {
                "easy" -> 0.5
                "medium" -> 1.0
                "hard" -> 1.25
                else -> exercise.maxPoints
            }

            exercise.copy(
                maxPoints = points
            )
        }
    }

    private fun validateAdaptiveExercisesOrThrow(
        exercises: List<ExerciseDto>,
    ) {
        require(exercises.size == 12) {
            "Expected exactly 12 adaptive exercises, got ${exercises.size}"
        }

        require(exercises.count { it.type == "open" } >= 3) {
            "Adaptive exercises require at least 3 open exercises"
        }

        exercises.forEachIndexed { index, ex ->
            require(
                ex.difficulty.equals("medium", true) ||
                        ex.difficulty.equals("hard", true)
            ) {
                "Adaptive exercise #${index + 1} must be medium or hard"
            }

            when (ex.type) {
                "mcq" -> {
                    require(ex.options.size in 3..6) {
                        "Adaptive MCQ options invalid at #${index + 1}"
                    }

                    val answerIndex = ex.answerIndex
                        ?: error(
                            "Adaptive MCQ answerIndex missing at #${index + 1}"
                        )

                    require(answerIndex in ex.options.indices) {
                        "Adaptive MCQ answerIndex invalid at #${index + 1}"
                    }
                }

                "true_false" -> {
                    require(ex.options.size == 2) {
                        "Adaptive true/false must have 2 options at #${index + 1}"
                    }

                    require(
                        ex.answerIndex == 0 ||
                                ex.answerIndex == 1
                    ) {
                        "Adaptive true/false answerIndex invalid at #${index + 1}"
                    }
                }

                "open" -> {
                    require(ex.options.size == 1) {
                        "Open exercise must have one placeholder option at #${index + 1}"
                    }

                    require(ex.answerIndex == null) {
                        "Open exercise answerIndex must be null at #${index + 1}"
                    }
                }

                else -> {
                    error(
                        "Unknown adaptive exercise type '${ex.type}' at #${index + 1}"
                    )
                }
            }

            require(ex.hint?.isNotBlank() == true) {
                "Adaptive hint missing at #${index + 1}"
            }

            require(ex.solution?.isNotBlank() == true) {
                "Adaptive solution missing at #${index + 1}"
            }

            require((ex.estimatedMinutes ?: 0) > 0) {
                "Adaptive estimatedMinutes invalid at #${index + 1}"
            }
        }
    }

    private fun validateExercisesOrThrow(
        exercises: List<ExerciseDto>,
        targetMinutes: Int,
        tolerance: Int = 3,
        hardMin: Int = 50,
        hardMax: Int = 75,
    ) {
        require(exercises.size == 20) {
            "Expected exactly 20 exercises, got ${exercises.size}"
        }

        val easy = exercises.count { it.difficulty.equals("easy", true) }
        val medium = exercises.count { it.difficulty.equals("medium", true) }
        val hard = exercises.count { it.difficulty.equals("hard", true) }

        require(easy == 4) { "Expected 4 easy, got $easy" }
        require(medium == 8) { "Expected 8 medium, got $medium" }
        require(hard == 8) { "Expected 8 hard, got $hard" }
        val totalPoints = exercises.sumOf {
            it.maxPoints ?: 0.0
        }

        require(kotlin.math.abs(totalPoints - 20.0) < 0.0001) {
            "Expected total maxPoints = 20.0, got $totalPoints"
        }
        require(exercises.any { it.type == "open" }) {
            "At least one open exercise is required"
        }

        exercises.forEachIndexed { idx, ex ->
            val expectedPoints = when (
                ex.difficulty
                    ?.trim()
                    ?.lowercase()
            ) {
                "easy" -> 0.5
                "medium" -> 1.0
                "hard" -> 1.25
                else -> error(
                    "Unknown difficulty '${ex.difficulty}' at #${idx + 1}"
                )
            }

            require(
                kotlin.math.abs(
                    (ex.maxPoints ?: 0.0) - expectedPoints
                ) < 0.0001
            ) {
                "Invalid maxPoints at #${idx + 1}"
            }

            when (ex.type) {
                "mcq" -> {
                    require(ex.options.size in 3..6) {
                        "MCQ options invalid at #${idx + 1}"
                    }

                    val answerIndex = ex.answerIndex
                        ?: error("MCQ answerIndex missing at #${idx + 1}")

                    require(answerIndex in ex.options.indices) {
                        "MCQ answerIndex invalid at #${idx + 1}"
                    }
                }

                "true_false" -> {
                    require(ex.options.size == 2) {
                        "True/false must have exactly 2 options at #${idx + 1}"
                    }

                    require(ex.answerIndex == 0 || ex.answerIndex == 1) {
                        "True/false answerIndex invalid at #${idx + 1}"
                    }
                }

                "open" -> {
                    require(ex.options.size == 1) {
                        "Open exercise must have one placeholder option at #${idx + 1}"
                    }

                    require(ex.answerIndex == null) {
                        "Open answerIndex must be null at #${idx + 1}"
                    }
                }

                else -> error("Unknown exercise type '${ex.type}'")
            }

            require(ex.hint?.isNotBlank() == true) {
                "Hint missing at #${idx + 1}"
            }

            require(ex.solution?.isNotBlank() == true) {
                "Solution missing at #${idx + 1}"
            }

            require((ex.estimatedMinutes ?: 0) > 0) {
                "estimatedMinutes invalid at #${idx + 1}"
            }
        }
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
    Repair the provided exercise set so that ALL rules below are satisfied.

    REQUIRED COUNT:
    - Exactly 20 exercises total.
    - Exactly 4 easy.
    - Exactly 8 medium.
    - Exactly 8 hard.
    - Include mcq, true_false and open exercises.
    - At least one exercise MUST be open.

    SOURCE GROUNDING:
    - Every exercise must be based ONLY on the provided UNIT.
    - Do not introduce concepts that are not taught in the UNIT.
    - Questions must test the actual educational content of this specific unit.
    - Avoid generic questions that could be reused for unrelated units.

    DIFFICULTY:
    - easy: direct recall, recognition or simple application.
    - medium: understanding, interpretation, comparison or multi-step application.
    - hard: deeper reasoning, synthesis, problem solving, production or debugging.

    SUBJECT ADAPTATION:
    - Adapt exercises to the subject of the UNIT.
    - For language learning, use appropriate vocabulary, grammar,
      ordering, sentence construction, translation or comprehension tasks.
    - For programming, include appropriate code reading, debugging,
      reasoning and code-writing tasks.
    - If the UNIT teaches programming, at least one open exercise
      MUST require writing code.
    - Use the programming language actually taught in the UNIT.
    - NEVER assume Java.
    - For non-programming units, do NOT create programming exercises.

    SELF-CONTAINED:
    - Every exercise must be understandable on its own.
    - If code is required to answer a question, include the necessary
      code inside that question.
    - Never refer to previous exercises or hidden context.

    HINT:
    - Every exercise must have a useful hint.
    - A hint must guide the learner without directly revealing the answer.

    SOLUTION:
    - Every exercise must have a correct solution.
    - Open exercises must provide an exemplary expected answer.
    - Code solutions must contain code when code is required.

    SCORING:
    - easy: maxPoints = 0.5
    - medium: maxPoints = 1.0
    - hard: maxPoints = 1.25
    - Total maxPoints MUST equal exactly 20.0.

    TIME:
    - estimatedMinutes must be realistic for an average learner.
    - Easy exercises should normally require less time than medium exercises.
    - Hard/open/problem-solving exercises may require more time.

    FORMAT:
    - mcq: 3..6 options and valid answerIndex.
    - true_false: exactly 2 options and answerIndex 0 or 1.
    - open: answerIndex must be null.
    - Every exercise must contain hint, explanation and solution.

    CODE FORMAT:
    - When code is required, use a Markdown fenced code block.
    - Use the correct programming-language tag.
    - NEVER hardcode Java unless the UNIT actually teaches Java.

    You may rewrite, remove or add exercises as necessary.
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
            model = "gpt-4o-mini",
            system = system,
            user = user,
            schemaOnly = schemaOnly,
            maxOutputTokens = 6500
        )

        val resp = callOpenAIResponsesWithRetry(req)
        if (!ensureCompleted(resp)) return emptyList()

        val raw = extractOutputJson(resp)
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

    suspend fun generatePreviewUnitsFromPdfChunkWithCarry(
        pdfChunkFile: File,
        language: String,
        fileName: String,
        unitNumber: Int,
        carry: String,
        isLastChunk: Boolean,
    ): UnitsWithCarryDto {
        val fileId = uploadPdfToOpenAI(pdfChunkFile)
            ?: error(
                "Could not upload PDF chunk to OpenAI"
            )
        val schema = unitsWithCarrySchemaJson()
        val outputLanguage = language.ifBlank { "en" }

        val system = """
            MODE: PREVIEW_UNIT_FROM_CHUNK_WITH_CARRY
            Return ONLY JSON matching schema (strict).
            No citations.

         GOAL:
            - Read CURRENT_CHUNK together with CARRY_FROM_PREVIOUS.
            - Identify ALL meaningful educational concepts contained in this material.
            - Create as many focused learning units as educationally appropriate.
            - Do NOT assume that one PDF chapter equals one learning unit.
            - One chapter may produce several learning units.
            - A learning unit must represent one coherent teachable concept or a very small group of tightly related concepts.
            
            COMPLETE COVERAGE:
            - Do not silently discard meaningful educational content.
            - Every meaningful concept in the processed material must either:
              1. be taught in one of the returned learning units, or
              2. remain in carry ONLY when the concept is incomplete and genuinely requires information from the next chunk.
            - Do not put complete concepts in carry simply because there are many concepts.
            - Prefer several short learning units instead of one overloaded learning unit.
            
            FINAL CHUNK RULE:
            - IS_LAST_CHUNK tells you whether CURRENT_CHUNK is the final chunk
              of the uploaded PDF.
            - When IS_LAST_CHUNK = true:
              - There is NO next chunk.
              - carry MUST be returned as an empty string.
              - Do not postpone any complete educational concept.
              - Any meaningful concept supported by the remaining carry and
                CURRENT_CHUNK must be converted into an appropriate learning unit.
              - Do not invent missing information simply to complete a concept.
              - If the final pages contain only administrative, bibliographic,
                incomplete or non-educational fragments, ignore those fragments
                instead of creating a fake learning unit.
            - When IS_LAST_CHUNK = false:
              - carry may contain only genuinely incomplete material that requires
                the next chunk.
    
            UNIT SELECTION:
            - Create units from educational concepts, not from the document structure.
            - A heading alone is not a reason to create a unit.
            - Ignore table of contents, copyright text, acknowledgements,
              bibliography, author biography, headers, footers and administrative material.
            - Do not create learning units about what the author or document is doing.
            
            UNIT CREATION:
            - Create as many learning units as required to teach all meaningful concepts.
            - Never reduce the number of units merely to make the response shorter.
            - If a chunk contains several distinct major concepts, create separate units.
            - If a concept contains several tightly related fundamental sub-concepts,
              keep them together only when they form one coherent lesson.
            - Coverage is more important than minimizing the number of units.
            
            EDUCATIONAL STYLE:
            - Rewrite the source as direct teaching material for a learner.
            - Never say:
              "the author says",
              "the author explains",
              "the document says",
              "according to the author",
              "in this PDF",
              "in this chapter",
              "on this page".
            - SmartTutor must teach the concept directly.
            
        TEXT QUALITY AND TEACHING COMPLETENESS:
        
        LANGUAGE:
        - Output language code: $outputLanguage
        - Write the title, theory, explanations and captions in that language.
        - Preserve programming keywords, identifiers, syntax, formulas and technical notation when translation would make them incorrect.
        - Do not translate the educational material into another language unless necessary for understanding.
        
        CORE PURPOSE:
        - UNIT_TEXT is the actual lesson from which the learner will study.
        - It must contain enough theory for the learner to understand the concept without needing to reopen the PDF.
        - Do NOT write a short summary.
        - Do NOT merely mention important terms.
        - Explain them.
        
        COVERAGE OF THE CONCEPT:
        - Identify the main concept of the learning unit and ALL important sub-concepts that belong to it and are supported by CURRENT_CHUNK + CARRY_FROM_PREVIOUS.
        - Do not omit a fundamental member of a group merely to keep the text short.
        - If the source teaches a category or collection of related items, explain the important members individually.
        
        EXAMPLE:
        - If a programming source teaches primitive data types, do not write only that "int is used for integers".
        - If supported by the source, explain the relevant types individually, such as integer types, floating-point types, characters and booleans, including their purpose and meaningful differences.
        - This example illustrates completeness only. Never assume Java or programming when the source is about another subject.
        
        EXPLANATION REQUIREMENTS:
        For each important concept or sub-concept, include when supported by the source:
        - what it is,
        - what it is used for,
        - how it works,
        - important distinctions from closely related concepts,
        - rules or constraints,
        - common mistakes, warnings or edge cases,
        - at least one useful example when an example improves understanding.
        
        RELATIONSHIPS:
        - When several concepts form a family, taxonomy, process, sequence, comparison or hierarchy, explain their relationship.
        - Do not teach isolated facts when the source presents them as connected concepts.
        
        EXAMPLES:
        - Include concrete examples throughout the theory, not only at the end.
        - Prefer examples that demonstrate the concept rather than merely restating the definition.
        - If the source is programming-related, use self-contained code examples in the programming language actually present in the source.
        - Never assume Java.
        - If the material is not programming-related, do not invent code examples.
        - Do not force exactly two examples when the concept needs more or fewer; include enough examples to teach the material clearly.
        
        SOURCE GROUNDING:
        - Use only information supported by CURRENT_CHUNK + CARRY_FROM_PREVIOUS.
        - You may reorganize and rewrite the information pedagogically.
        - Do not add factual details that are not supported by the source.
        - Do not omit important source-supported information merely to satisfy a short word target.
        
        STYLE:
        - Teach directly to the learner.
        - Use clear paragraphs and, when useful, short bullet lists or compact comparisons.
        - Prefer clarity and completeness over artificial brevity.
        - Avoid filler, repetition and long introductions.
        - Never say "the author says", "the document explains", "in this PDF", "on this page", "in this chapter", or similar phrases.
                   
        UNIT LENGTH:
        - Keep each learning unit focused and learner-friendly.
        - A learning unit should cover one coherent educational concept
          or one tightly related concept family.
        - Prefer approximately 180–500 words when that is sufficient.
        - A unit MAY be longer when the source contains several essential
          sub-concepts that must be taught together.
        - Never omit fundamental information only to satisfy a word target.
        - If the material contains a second distinct major concept,
          create another learning unit instead of overloading the current one.
        - Avoid repetition and unnecessary introductory text.

            IMAGES:
            - Do not add an image just to satisfy formatting.
            - Include 0 or 1 educational diagram image only if it clearly helps understanding.
            - Prefer NO image over a wrong, cropped, incomplete, or confusing image.
            - images[0].relativePath = null
            - images[0].caption MUST use the same language as the learning unit: $outputLanguage.
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
            - Do not assume the learner remembers previous examples.
            - If a later example needs context, repeat the necessary context briefly.
            
            CODE EXAMPLE INDEPENDENCE:
            - When programming examples are relevant, every code block must be independently understandable.
            - Never make one code example depend on variables, methods, functions, classes, labels, registers, or other definitions that exist only in a previous example.
            - Use the programming language actually present in the source material.
            - Never assume Java.
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
            - Across the whole document, cover all meaningful educational concepts.
            - Create a new unit when a distinct teachable concept begins.
            - Do not create a unit only because a new heading appears.
            - Do not merge unrelated concepts into one learning unit.

            QUOTATIONS:
            - quotations MUST be [] or 1..2 items.
            - quotations[i].text MUST be a single STRING. If unsure: quotations: [].

            EXERCISES:
            - exercises MUST be [] here.

            CODE FORMAT RULE:
            - When code is present, format it clearly using Markdown fenced code blocks.
            - Use the correct programming-language tag.
            - Keep statements/instructions clearly separated when appropriate for that language.
            - Never assume Java.

            ANTI-HALLUCINATION:
            - Use ONLY what is supported by CURRENT_CHUNK + carry.
        """.trimIndent()

        val user = """
            Filename: $fileName
            Language: $language
            IS_LAST_CHUNK: $isLastChunk
        
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
            maxOutputTokens = 8000
        )

        onRawHook?.invoke("preview_request", req)

        val resp = callOpenAIResponsesWithRetry(req)

        onRawHook?.invoke("preview_response", resp)

        if (!ensureCompleted(resp)) {
            error(
                "Preview generation returned an incomplete response"
            )
        }

        val raw = extractOutputJson(resp)

        val unwrapped = unwrapIfJsonString(raw)
        val out = extractFirstJsonObjectOrNull(unwrapped)

        if (out == null || !isValidJson(out)) {
            error(
                "Preview generation returned invalid JSON"
            )
        }

        val dto = runCatching {
            json.decodeFromString(
                UnitsWithCarryDto.serializer(),
                out
            )
        }.getOrElse {
            onRawHook?.invoke(
                "preview_decode_error",
                it.message ?: "decode failed"
            )

            throw IllegalStateException(
                "Could not decode generated learning units",
                it
            )
        }
        val validUnits = dto.units?.mapIndexedNotNull { index, unit ->

            val fixedUnit = unit.copy(
                numbering = unitNumber + index,
                quotations = unit.quotations.take(2),
                images = unit.images.take(1),
                exercises = emptyList()
            )

            val text = fixedUnit.textInfo.orEmpty().trim()

            val paragraphs =
                text.split(Regex("""\n\s*\n""")).map { it.trim() }.filter { it.isNotBlank() }

            val wordCount = text.split(Regex("""\s+""")).count { it.isNotBlank() }

            val hasEnoughText =
                paragraphs.size >= 2 &&
                        wordCount >= 80

            if (hasEnoughText) fixedUnit else null
        }

        return UnitsWithCarryDto(
            units = validUnits,
            carry = if (isLastChunk) {
                ""
            } else {
                dto.carry.take(8000)
            }
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
                error(
                    "OpenAI request failed: " +
                            "status=$status, " +
                            "type=${errType ?: "unknown"}, " +
                            "message=${errMsg ?: text.take(1200)}"
                )

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

    private fun ensureCompleted(
        respText: String,
    ): Boolean {
        val root = runCatching {
            json.parseToJsonElement(respText).jsonObject
        }.getOrNull() ?: return false

        val status = root["status"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return false

        val errMsg = (root["error"] as? JsonObject)
            ?.get("message")
            ?.jsonPrimitive
            ?.contentOrNull

        if (!errMsg.isNullOrBlank()) {
            return false
        }

        return status == "completed"
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

    private fun unitsWithCarrySchemaJson(): String = """
{
  "type": "object",
  "additionalProperties": false,
  "required": [
    "units",
    "carry"
  ],
  "properties": {
    "units": {
      "type": "array",
      "items": {
        "type": "object",
        "additionalProperties": false,
        "required": [
          "title",
          "numbering",
          "text",
          "quotations",
          "images",
          "exercises"
        ],
        "properties": {
          "title": {
            "type": "string"
          },
          "numbering": {
            "type": "integer"
          },
          "text": {
            "type": "string"
          },
          "quotations": {
            "type": "array",
            "items": {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "text",
                "numbering"
              ],
              "properties": {
                "text": {
                  "type": [
                    "string",
                    "null"
                  ]
                },
                "numbering": {
                  "type": [
                    "integer",
                    "null"
                  ]
                }
              }
            }
          },
          "images": {
            "type": "array",
            "items": {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "relativePath",
                "caption",
                "numbering",
                "prompt"
              ],
              "properties": {
                "relativePath": {
                  "type": [
                    "string",
                    "null"
                  ]
                },
                "caption": {
                  "type": [
                    "string",
                    "null"
                  ]
                },
                "numbering": {
                  "type": [
                    "integer",
                    "null"
                  ]
                },
                "prompt": {
                  "type": [
                    "string",
                    "null"
                  ]
                }
              }
            }
          },
          "exercises": {
            "type": "array",
            "items": {
              "type": "object",
              "additionalProperties": false,
              "properties": {}
            }
          }
        }
      }
    },
    "carry": {
      "type": "string"
    }
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

    private fun adaptiveExercisesOnlySchemaJson(): String = """
            {
              "type":"object",
              "additionalProperties":false,
              "required":["exercises"],
              "properties":{
                "exercises":{
                  "type":"array",
                  "minItems":12,
                  "maxItems":12,
                  "items":{
                    "type":"object",
                    "additionalProperties":false,
                    "required":[
                      "type",
                      "difficulty",
                      "estimatedMinutes",
                      "maxPoints",
                      "question",
                      "options",
                      "answerIndex",
                      "hint",
                      "explanation",
                      "solution"
                    ],
                    "properties":{
                      "type":{
                        "type":"string",
                        "enum":["mcq","true_false","open"]
                      },
                      "difficulty":{
                        "type":"string",
                        "enum":["medium","hard"]
                      },
                      "estimatedMinutes":{
                        "type":"integer",
                        "minimum":1,
                        "maximum":15
                      },
                      "maxPoints":{
                        "type":"number",
                        "minimum":1.0,
                        "maximum":1.25
                      },
                      "question":{
                        "type":"string",
                        "minLength":10
                      },
                      "options":{
                        "type":"array",
                        "minItems":1,
                        "maxItems":6,
                        "items":{
                          "type":"string",
                          "minLength":1
                        }
                      },
                      "answerIndex":{
                        "type":["integer","null"],
                        "minimum":0,
                        "maximum":5
                      },
                      "hint":{
                        "type":"string",
                        "minLength":5
                      },
                      "explanation":{
                        "type":"string",
                        "minLength":8
                      },
                      "solution":{
                        "type":"string",
                        "minLength":5
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()

    private fun unitExercisesOnlySchemaJson(): String = """
        {
          "type":"object",
          "additionalProperties":false,
          "required":["exercises"],
          "properties":{
            "exercises":{
              "type":"array",
              "minItems":20,
              "maxItems":20,
              "items":{
                "type":"object",
                "additionalProperties":false,
            "required":["type","difficulty","estimatedMinutes","maxPoints","question","options","answerIndex","hint","explanation","solution"],
                "properties":{
                  "type":{"type":"string","enum":["mcq","true_false","open"]},
                  "difficulty":{"type":"string","enum":["easy","medium","hard"]},
                  "estimatedMinutes":{"type":"integer","minimum":1,"maximum":15},
                   "maxPoints":{"type":"number","minimum":0.5,"maximum":1.25},
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
        val answerLanguage = language.trim().ifBlank { "en" }

        val recentHistory = recentMessages.takeLast(10).joinToString("\n") { msg ->
            "${msg.sender}: ${msg.text}"
        }

        val system = """
            MODE: LEARNING_UNIT_TUTOR
        
            You are SmartTutor, an educational tutor helping a learner
            understand the CURRENT learning unit.
        
            LANGUAGE:
            - Default response language code: $answerLanguage
            - Answer in that language by default.
            - If the learner explicitly asks you to explain or answer in another
              language, follow that request.
            - Preserve technical notation, formulas, code syntax and identifiers
              when translating them would make them incorrect.
        
            PRIMARY SOURCE:
            - CURRENT_CONTEXT is the main source for your answer.
            - Base explanations on concepts actually taught in CURRENT_CONTEXT.
            - Do not invent facts that are not supported by the provided context.
            - Do not assume the subject is programming.
            - Do not assume Java.
        
            SCOPE:
            - Focus on the CURRENT learning unit.
            - If the learner asks something unrelated to the available context,
              clearly say that it is outside the material currently available
              instead of inventing an answer.
            - Never pretend that you can see other parts of the document
              unless they are included in CURRENT_CONTEXT.
        
            TEACHING STYLE:
            - Explain the idea clearly and directly.
            - Prefer concise explanations first.
            - Expand when the learner asks for more detail.
            - Use examples when they genuinely improve understanding.
            - Adapt examples to the actual subject of the learning unit.
            - Do not talk about "the author", "the PDF", "the chapter",
              or "the document" unless the learner explicitly asks about them.
            - Teach the concept directly.
        
            EXERCISE HELP:
            - If the learner asks for help with an exercise,
              do not immediately reveal the final answer unless they explicitly
              ask for the solution.
            - First explain the relevant concept or provide a useful hint.
            - Hints must guide reasoning without giving the answer away.
            - If the learner asks why an answer was wrong, explain the mistake
              using the CURRENT_CONTEXT.
        
            PROGRAMMING:
            - Only use code when programming is relevant to CURRENT_CONTEXT
              or explicitly requested by the learner.
            - Use the programming language found in the learning unit.
            - Never assume Java.
            - Every code example must be self-contained.
            - Put code inside Markdown fenced code blocks with the correct
              programming-language tag.
        
            LANGUAGE LEARNING:
            - For language-learning material, preserve the language forms being
              taught.
            - Explain grammar, vocabulary, sentence structure or meaning only
              using concepts appropriate to the CURRENT_CONTEXT.
        
            RESPONSE QUALITY:
            - Do not make up citations or page references.
            - Do not add unrelated information merely to make the answer longer.
            - Prefer an educational explanation over a generic encyclopedia answer.
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
            return "I could not generate an answer at this time."
        }

        return extractOutputText(resp).trim().ifBlank {
            "No answer was generated."
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
