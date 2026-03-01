package org.example.project.tutor.repo.loadResource

class LocalLearningUnitsRepositoryImpl() : ILoadResourceRepo {
    override suspend fun loadPrototype(): org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain {
        return org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain()
    }
}