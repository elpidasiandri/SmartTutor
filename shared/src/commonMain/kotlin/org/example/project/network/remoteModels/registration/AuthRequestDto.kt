package org.example.project.network.remoteModels.registration

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    val email: String,
    val password: String
)
