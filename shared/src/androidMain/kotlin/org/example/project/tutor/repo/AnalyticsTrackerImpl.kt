package org.example.project.tutor.repo

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.example.project.tutor.repo.analytics.IAnalyticsTracker

class AnalyticsTrackerImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
) : IAnalyticsTracker {

    override fun sendEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                putString(key, value)
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }
}