package org.example.project.tutor.repo.analytics

interface IAnalyticsTracker {
    fun sendEvent(name: String, params: Map<String, String> = emptyMap())
}