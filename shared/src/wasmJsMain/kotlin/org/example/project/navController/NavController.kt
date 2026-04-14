package org.example.project.navController

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.w3c.dom.Screen

class NavController {
    private val _screen = MutableStateFlow<WebScreen>(WebScreen.Registration)
    val screen: StateFlow<WebScreen> get() = _screen

    fun navigate(screen: WebScreen) {
        _screen.value = screen
    }
}
sealed class WebScreen {
    object Registration : WebScreen()
    object MyProgress : WebScreen()
    object DeleteAccountSettings : WebScreen()
    object ChangePasswordSettings : WebScreen()
    object Tutor : WebScreen()
    object Settings : WebScreen()
}