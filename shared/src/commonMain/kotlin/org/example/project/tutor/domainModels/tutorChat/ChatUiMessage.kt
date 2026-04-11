package org.example.project.tutor.domainModels.tutorChat

import org.example.project.tutor.domainModels.learningUnits.UnitImageDomain

data class ChatUiMessage(
    val id: String,
    val sender: String,
    val text: String,
    val imagePath: List<UnitImageDomain> = emptyList(),
    val timestamp: Long = 0L
)
