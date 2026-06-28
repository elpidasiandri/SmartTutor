package org.example.project.tutor.networkModel.uploadPdf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadPdfResponse(
    @SerialName("documentId") val documentId: String,
    @SerialName("status") val status: String,
    @SerialName("storagePath") val storagePath: String,
)

@Serializable
data class PdfJobStatusResponse(
    @SerialName("documentId") val documentId: String,
    @SerialName("status") val status: String, // queued | processing | done | error
    @SerialName("progress") val progress: Int? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("partialResult") val partialResult: LearningUnitsPrototypeDto? = null,
    @SerialName("result") val result: LearningUnitsPrototypeDto? = null,
)

@Serializable
data class LearningUnitsPrototypeDto(
    @SerialName("prototype_title") val prototypeTitle: String? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("total_units") val totalUnits: Int? = null,
    @SerialName("learning_units") val learningUnits: List<LearningUnitDto> = emptyList(),
)

@Serializable
data class LearningUnitDto(
    @SerialName("title") val title: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
    @SerialName("text") val textInfo: String? = null,
    @SerialName("isRead") val isRead: Boolean? = false,
    @SerialName("quotations") val quotations: List<QuotationDto> = emptyList(),
    @SerialName("images") val images: List<UnitImageDto> = emptyList(),
    @SerialName("exercises") val exercises: List<ExerciseDto> = emptyList(),
    @SerialName("totalPoints") val totalPoints: Double = 20.0,
)

@Serializable
data class QuotationDto(
    @SerialName("text") val text: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
)

@Serializable
data class UnitImageDto(
    @SerialName("relativePath") val relativePath: String? = null, // will be filled after image gen
    @SerialName("caption") val caption: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
    @SerialName("prompt") val prompt: String? = null, // if present -> generate image
)

@Serializable
data class ExerciseDto(
    @SerialName("type") val type: String, // mcq|true_false|open
    @SerialName("difficulty") val difficulty: String? = null, // easy|medium|hard
    @SerialName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerialName("question") val question: String? = null,
    @SerialName("options") val options: List<String> = emptyList(),
    @SerialName("answerIndex") val answerIndex: Int? = null,
    @SerialName("hint") val hint: String? = null,
    @SerialName("explanation") val explanation: String? = null,
    @SerialName("solution") val solution: String? = null,
    @SerialName("maxPoints") val maxPoints: Double?,
    @SerialName("attempts") val attempts: List<ExerciseAttemptDto> = emptyList()
)

@Serializable
data class ExerciseAttemptDto(
    @SerialName("attemptNumber") val attemptNumber: Int,
    @SerialName("awardedPoints") val awardedPoints: Double,
    @SerialName("gradingStatus") val gradingStatus: String, // correct | partial | wrong
    @SerialName("isCorrect") val isCorrect: Boolean,
    @SerialName("selectedAnswer") val selectedAnswer: String,
    @SerialName("answeredAtEpochMillis") val answeredAtEpochMillis: Long,
)

@Serializable
data class UnitWithCarryDto(
    @SerialName("unit") val unit: LearningUnitDto? = null,
    @SerialName("carry") val carry: String = "",
)

// only for prompting
@Serializable
data class UnitExercisesOnlyDto(
    @SerialName("exercises") val exercises: List<ExerciseDto> = emptyList(),
)

@Serializable
data class ImageGenRequest(
    @SerialName("model") val model: String = "gpt-image-1",
    @SerialName("prompt") val prompt: String,
    @SerialName("size") val size: String = "900x900",
)

@Serializable
data class ImageGenResponse(
    @SerialName("data") val data: List<ImageGenData> = emptyList(),
)

@Serializable
data class ImageGenData(
    @SerialName("b64_json") val b64Json: String? = null,
)

enum class JobStatus(val value: String) {
    Queued("queued"),
    Processing("processing"),
    Done("done"),
    Failed("failed")
}

enum class LanguageOfLearningUnitsEnum(val value: String) {
    Greek("el"),
    English("en")
}
