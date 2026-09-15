@file:JsModule("./firebaseWrapper.js")
@file:OptIn(ExperimentalWasmJsInterop::class)
package org.example.project.tutor

external fun isTutorialCompleted(
    userId: String
): Boolean

external fun completeTutorial(
    userId: String
)