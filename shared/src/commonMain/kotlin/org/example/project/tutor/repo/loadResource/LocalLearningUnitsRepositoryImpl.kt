package org.example.project.tutor.repo.loadResource

import kotlinx.serialization.json.Json

class LocalLearningUnitsRepositoryImpl(
    private val filePath: String = "files/material_learning_units.json",
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ILoadResourceRepo {
    override suspend fun loadPrototype(): org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain {
        return org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain()
    }
}