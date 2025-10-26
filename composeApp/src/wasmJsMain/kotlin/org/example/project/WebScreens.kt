package org.example.project

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

sealed class Screen {
    object Registration : Screen()
    object Tutor : Screen()
}
class AppController {
    private val _screen = mutableStateOf<Screen>(Screen.Registration)
    val screen: State<Screen> = _screen

    fun goToTutor() {
        _screen.value = Screen.Tutor
    }
}
