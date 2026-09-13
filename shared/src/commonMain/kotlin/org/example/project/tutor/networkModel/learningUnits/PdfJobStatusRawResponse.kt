package org.example.project.tutor.networkModel.learningUnits

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PdfJobStatusRawResponse(
    @SerialName("documentId") val documentId: String,
    @SerialName("language") val language: String? = null,
    @SerialName("totalUnits") val totalUnits: Int? = null,
    @SerialName("resultJson") val resultJson: String? = null,
    @SerialName("partialResultJson") val partialResultJson: String? = null,
)
