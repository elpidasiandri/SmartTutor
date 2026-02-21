package org.example.project.tutor.domainModels.tutorChat

import org.example.project.tutor.domainModels.learningUnits.UnitImageDomain

data class ChatUiMessage(
    val sender: String,
    val text: String,
    val imagePath: List<UnitImageDomain> = emptyList()
)
