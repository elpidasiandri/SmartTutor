package org.example.project.tutor.repo.loadResource

import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitsPrototypeDomain

class LocalLearningUnitsRepositoryImpl() : ILoadResourceRepo {
    override suspend fun loadPrototype(): LearningUnitsPrototypeDomain {
        return LearningUnitsPrototypeDomain()
    }
}