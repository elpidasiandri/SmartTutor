package org.example.project.settings.logOut

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.example.project.registration.firebase.FirebaseApp
import org.example.project.registration.firebase.FirebaseAuth
import org.example.project.registration.firebase.getAuth
import org.example.project.registration.firebase.signOut
import org.example.project.settings.logout.repo.ILogoutRepo
import org.example.project.storage.WebStorageHelper.clearToken

class LogoutRepoImpl(private val app: FirebaseApp) : ILogoutRepo {
    private val auth: FirebaseAuth = getAuth(app)

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun logout(onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                onResult(true, null)
                clearToken()
                signOut(auth).await()
            } catch (e: Throwable) {
                onResult(false, e.message)
            }
        }
    }
}