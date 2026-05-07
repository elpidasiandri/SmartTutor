package org.example.project.registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.project.registration.firebase.FirebaseUser

object AuthUserHolder {
//    var currentUser: FirebaseUser? = null
    var currentUser by mutableStateOf<FirebaseUser?>(null)

}
