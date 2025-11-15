@file:JsModule("./webHelperVoice.js")
@file:OptIn(ExperimentalWasmJsInterop::class)
package org.example.project.tutor

@JsName("startVoiceRecognition")
external fun startVoiceRecognition(onResult: (String) -> Unit)

@JsName("stopVoiceRecognition")
external fun stopVoiceRecognition()

@JsName("speakText")
external fun speakText(text: String)
