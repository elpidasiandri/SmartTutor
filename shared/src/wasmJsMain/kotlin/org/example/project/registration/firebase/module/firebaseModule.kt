package org.example.project.registration.firebase.module

import org.example.project.registration.firebase.FirebaseContext
import org.example.project.registration.firebase.initFirebase
import org.koin.dsl.module

val firebaseModule = module {

    single<FirebaseContext> {
        initFirebase()
    }
}