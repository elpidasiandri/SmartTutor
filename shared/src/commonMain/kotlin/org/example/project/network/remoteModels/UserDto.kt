package org.example.project.network.remoteModels

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val email: String,
    val username: String,
    val password: String
)

val users = mutableMapOf<String, UserDto>()

