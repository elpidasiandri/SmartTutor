package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.registration.di.registrationModule
import org.example.project.registration.screen.LoginSignUpActivity
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
        LoginSignUpActivity.newInstance(this)
        finish()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}