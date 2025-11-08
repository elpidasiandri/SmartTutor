package org.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import org.example.project.authToken.StorageHelper
import org.example.project.registration.di.registrationModule
import org.example.project.registrationScreen.RegistrationActivity
import org.example.project.tutorScreen.TutorActivity
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(this@MainActivity)
            modules(registrationModule)
        }
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null && StorageHelper.isTokenValid(this)) {
            startActivity(Intent(this, TutorActivity::class.java))
        } else {
            RegistrationActivity.newInstance(this)
        }
        finish()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}