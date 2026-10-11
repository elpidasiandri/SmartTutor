package org.example.project.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import kotlinx.serialization.json.Json
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto

class ExperimentContentStorage(
    private val storage: Storage,
    private val bucketName: String,
) {

    companion object {
        const val EXPERIMENT_DOCUMENT_ID =
            "duth_java_experiment"

        const val EXPERIMENT_JSON_PATH =
            "experiments/duth_java_experiment.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun loadExperimentContent(): LearningUnitsPrototypeDto {
        val blob = storage.get(
            BlobId.of(bucketName, EXPERIMENT_JSON_PATH)
        ) ?: error(
            "Experiment JSON not found: $EXPERIMENT_JSON_PATH"
        )

        val rawJson = blob.getContent()
            .toString(Charsets.UTF_8)

        require(rawJson.isNotBlank()) {
            "Experiment JSON is empty"
        }

        val result = json.decodeFromString(
            LearningUnitsPrototypeDto.serializer(),
            rawJson
        )

        require(result.learningUnits.isNotEmpty()) {
            "Experiment must contain learning units"
        }

        return result
    }
}
