package org.example.project.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.screens.login.LoginComposable
import org.example.project.screens.signUp.SignupComposable

@Composable
fun RegistrationComposable(){
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Login", "Signup")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when (selectedTab) {
            0 -> LoginComposable()
            1 -> SignupComposable()
        }
    }
}
