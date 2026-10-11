package org.example.project.experiment


object ExperimentConfig {
    const val EXPERIMENT_DOCUMENT_ID = "duth_java_experiment"

    const val IS_EXPERIMENT_ACTIVE = true

    private const val DUTH_EMAIL_DOMAIN = "@cs.duth.gr"

    fun isDuthStudent(email: String?): Boolean {
        return email
            ?.trim()
            ?.lowercase()
            ?.endsWith(DUTH_EMAIL_DOMAIN) == true
    }

    fun isSignupAllowed(email: String?): Boolean {
        return !IS_EXPERIMENT_ACTIVE || isDuthStudent(email)
    }


    fun shouldUseExperimentDocument(
        email: String?
    ): Boolean {
        return IS_EXPERIMENT_ACTIVE && isDuthStudent(email)
    }
}