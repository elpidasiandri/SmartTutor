package org.example.project.tutor.viewModel

import org.example.project.tutor.speakText
import org.example.project.tutor.startVoiceRecognition
import org.example.project.tutor.stopVoiceRecognition

class TutorController {

    fun playAIMessage(text: String) {
        val normalized = normalizeTranscript(text)
        speakText(normalized)
    }

    fun startVoiceInput(onResult: (String) -> Unit) {
println("Voice recognition started")
        startVoiceRecognition(onResult)
    }

   private fun normalizeTranscript(text: String): String {
        return text.replace("-", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }


    fun stopVoiceInput() {
        stopVoiceRecognition()
    }
}
