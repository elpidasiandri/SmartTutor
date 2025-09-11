package org.example.project.network.remoteModels.registration

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: String
)
