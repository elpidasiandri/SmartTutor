package org.example.project.tutor.networkModel.learningUnits

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LearningUnitsPrototypeDto(
    @SerialName("prototype_title")
    val prototypeTitle: String? = null,

    @SerialName("language")
    val language: String? = null, // "el" / "en"

    @SerialName("total_units")
    val totalUnits: Int? = null,

    @SerialName("learning_units")
    val learningUnits: List<LearningUnitDto?>? = emptyList(),
)

@Serializable
data class LearningUnitDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("numbering") val numbering: Int? = null,

    @SerialName("basetext") val basetext: String? = null,
    @SerialName("text") val text: String? = null,

    @SerialName("domain_specific_knowledge")
    val domainSpecificKnowledge: List<DskItemDto?>? = emptyList(),

    @SerialName("images")
    val images: List<UnitImageDto?>? = emptyList(),

    @SerialName("quotations")
    val quotations: List<QuotationDto?>? = emptyList(),

    @SerialName("exercises")
    val exercises: List<ExerciseDto?>? = emptyList(),
)


@Serializable
data class UnitImageDto(
    @SerialName("relativePath") val relativePath: String? = null,
    @SerialName("caption") val caption: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
)

@Serializable
data class QuotationDto(
    @SerialName("image") val image: ImageRefDto? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
)

@Serializable
data class ExerciseDto(
    @SerialName("type") val type: String, // "mcq", "open", "true_false"
    @SerialName("question") val question: String? = null,
    @SerialName("options") val options: List<String> = emptyList(),
    @SerialName("answerIndex") val answerIndex: Int? = null,
    @SerialName("explanation") val explanation: String? = null,
    @SerialName("sampleAnswer") val sampleAnswer: String? = null
)


@Serializable
data class ImageRefDto(
    @SerialName("relativePath") val relativePath: String? = null,
)

@Serializable
data class DskItemDto(
    @SerialName("type") val type: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("caption") val caption: String? = null,
    @SerialName("numbering") val numbering: Int? = null,
)


enum class LanguageOfLearningUnitsEnum(val value :String){
    Greek("el"),
    English("en")
}