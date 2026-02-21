package org.example.project.tutor.useCases.getIdToken

import org.example.project.tutor.repo.ITutorRepo

class GetIdTokenSuspendUseCase(  val repo: ITutorRepo) {
    suspend operator fun invoke(
        forceRefresh: Boolean = true
    ) = repo.getIdTokenSuspend(
        forceRefresh
    )
}