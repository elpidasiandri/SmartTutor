@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.example.project.registration.RegistrationComposable
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.FirebaseOptions
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.initializeApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
//    val auth = initFirebase()
    ComposeViewport("tutorApp") {
        RegistrationComposable(
            showCustomMessage = false,
            message = "test",
            onLogin = {email,password-> },
            onSignUp = {email,password-> },
            onMessageDismiss = {}
        )
    }
}
private val firebaseConfig: FirebaseOptions = js("({})")
//TODO ELPIDA
//fun initFirebase(): FirebaseAuth {
//    firebaseConfig.apiKey = "AIzaSyXXX..."
//    firebaseConfig.authDomain = "your-project.firebaseapp.com"
//    firebaseConfig.projectId = "your-project-id"
//
//    val app = initializeApp(firebaseConfig.unsafeCast<FirebaseOptions>())
//    return getAuth(app)
//}

//@OptIn(ExperimentalComposeUiApi::class)
//fun main() {
    // Firebase config
//    val firebaseConfig = js("""({
//        apiKey: "${FirebaseKeys.apiKey}",
//        authDomain: "${FirebaseKeys.authDomain}",
//        projectId: "${FirebaseKeys.projectId}",
//        storageBucket: "${FirebaseKeys.storageBucket}",
//        messagingSenderId: "${FirebaseKeys.messagingSenderId}",
//        appId: "${FirebaseKeys.appId}"
//    })""")

//
//    val app = initializeApp(firebaseConfig)
//    val authRepository = WebAuthRepositoryImpl(app)
//    val controller = WebRegistrationController(authRepository)
//
//    ComposeViewport(content = {
//            val state by controller.state.collectAsState()
//
//            RegistrationComposable(
//                showCustomMessage = state.showCustomMessage,
//                message = state.message,
//                onLogin = controller::login,
//                onSignUp = controller::signUp,
//                onMessageDismiss = controller::dismissMessage
//            )
//        })
//}