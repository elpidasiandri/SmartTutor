package org.example.project.tutor.repo.loadResource

import kotlinproject.shared.generated.resources.Res
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.example.project.tutor.mappers.learningUnits.toDomain
import org.example.project.tutor.networkModel.learningUnits.LearningUnitsPrototypeDto

class LocalLearningUnitsRepositoryImpl(
    private val filePath: String = "files/material_learning_units.json",
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ILoadResourceRepo {
    override suspend fun loadPrototype(): org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain {
        val bytes = Res.readBytes(filePath)
//
//        val jsonText = bytes.decodeToString()
//
//        val dto = json.decodeFromString(
//            LearningUnitsPrototypeDto.serializer(),
//            jsonText
//        )
        return org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain()
    }
}