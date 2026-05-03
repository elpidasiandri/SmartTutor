package org.example.project.myProgress.useCases

import org.example.project.myProgress.mappers.toDomain
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.models.ProgressOverviewDomain

class GetProgressOverviewUseCase(
    private val repo: IProgressRepo
) {
    suspend operator fun invoke(userId: String): ProgressOverviewDomain {
        return repo.getOverview(userId).toDomain()
    }
}
