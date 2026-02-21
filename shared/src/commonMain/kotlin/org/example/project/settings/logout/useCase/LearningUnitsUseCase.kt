package org.example.project.settings.logout.useCase

import org.example.project.tutor.repo.loadResource.ILoadResourceRepo

class LearningUnitsUseCase(private val repo: ILoadResourceRepo) {
    suspend operator fun invoke() = repo.loadPrototype()
}