@file:Suppress("UnsafeCastFromDynamic")

package org.example.project.tutor.repo

import org.example.project.registration.firebase.initializeApp
import org.example.project.registration.firebase.createFirebaseOptions
import org.example.project.registration.firebase.createMessageSentParams
import org.example.project.registration.firebase.createPdfUploadedParams
import org.example.project.registration.firebase.createTabChangedParams
import org.example.project.shared.FirebaseConfig
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.registration.firebase.getAnalytics
import org.example.project.registration.firebase.logEvent
import org.example.project.tutor.models.AnalyticsEvents.EXERCISE_ANSWERED
import org.example.project.tutor.models.AnalyticsEvents.MESSAGE_SENT
import org.example.project.tutor.models.AnalyticsEvents.PDF_UPLOADED
import org.example.project.tutor.models.AnalyticsEvents.TAB_CHANGED

class WebAnalyticsTrackerImpl : IAnalyticsTracker {

    private val analytics by lazy {
        val options = createFirebaseOptions(
            apiKey = FirebaseConfig.WEB_API_KEY,
            authDomain = FirebaseConfig.WEB_AUTH_DOMAIN,
            projectId = FirebaseConfig.PROJECT_ID,
            storageBucket = FirebaseConfig.WEB_STORAGE_BUCKET,
            messagingSenderId = FirebaseConfig.SENDER_ID,
            appId = FirebaseConfig.APP_ID
        )

        val app = initializeApp(options)
        getAnalytics(app)
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun sendEvent(name: String, params: Map<String, String>) {
        runCatching {
            when (name) {
                MESSAGE_SENT -> {
                    logEvent(
                        analytics = analytics,
                        eventName = name,
                        eventParams = createMessageSentParams(
                            chatId = params["chatId"].orEmpty(),
                            unitIndex = params["unitIndex"].orEmpty(),
                            tab = params["tab"].orEmpty(),
                            messageLength = params["messageLength"].orEmpty(),
                            userId = params["userId"].orEmpty()
                        )
                    )
                }

                PDF_UPLOADED -> {
                    logEvent(
                        analytics,
                        name,
                        createPdfUploadedParams(
                            chatId = params["chatId"].orEmpty(),
                            documentTitle = params["documentTitle"].orEmpty(),
                            unitsCount = params["unitsCount"].orEmpty(),
                            language = params["language"].orEmpty(),
                            mode = params["mode"].orEmpty(),
                            userId = params["userId"].orEmpty()
                        )
                    )
                }

                TAB_CHANGED -> {
                    logEvent(
                        analytics,
                        name,
                        createTabChangedParams(
                            chatId = params["chatId"].orEmpty(),
                            tab = params["tab"].orEmpty(),
                            unitTitle = params["unitTitle"].orEmpty(),
                            userId = params["userId"].orEmpty()
                        )
                    )
                }
                EXERCISE_ANSWERED -> {
                    logEvent(
                        analytics,
                        name,
                        createExerciseAnsweredParams(
                            chatId = params["chatId"].orEmpty(),
                            unitIndex = params["unitIndex"].orEmpty(),
                            unitTitle = params["unitTitle"].orEmpty(),
                            exerciseIndex = params["exerciseIndex"].orEmpty(),
                            exerciseType = params["exerciseType"].orEmpty(),
                            selectedAnswer = params["selectedAnswer"].orEmpty(),
                            correctAnswer = params["correctAnswer"].orEmpty(),
                            isCorrect = params["isCorrect"].orEmpty(),
                            timeToAnswerMs = params["timeToAnswerMs"].orEmpty(),
                            userId = params["userId"].orEmpty()
                        )
                    )
                }
                else -> {
                    logEvent(analytics, name)
                }
            }
        }
    }
}

