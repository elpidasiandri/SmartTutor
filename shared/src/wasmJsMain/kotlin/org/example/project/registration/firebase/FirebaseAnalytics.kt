@file:JsModule("firebase/analytics")
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.example.project.registration.firebase

import kotlin.js.JsAny

external interface Analytics : JsAny

external fun getAnalytics(app: FirebaseApp): Analytics

external fun logEvent(
    analytics: Analytics,
    eventName: String,
    eventParams: JsAny,
)

external fun logEvent(
    analytics: Analytics,
    eventName: String,
)