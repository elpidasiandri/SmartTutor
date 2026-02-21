package org.example.project.tutor.repo.loadResource

import org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain

interface ILoadResourceRepo {

    suspend fun loadPrototype(): LearningUnitsPrototypeDomain
}