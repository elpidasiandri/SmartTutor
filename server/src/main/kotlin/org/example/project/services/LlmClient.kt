package org.example.project.services


class LlmClient {
    suspend fun createLearningUnitsJson(language: String, text: String): String {
        return """
          {
            "prototype_title": "Uploaded PDF",
            "language": "$language",
            "total_units": 1,
            "learning_units": [
              {
                "id": "lu_1",
                "title": "Unit 1",
                "numbering": 1,
                "basetext": "",
                "text": "Mini summary εδώ...",
                "domain_specific_knowledge": [],
                "images": [],
                "quotations": [],
                "exercises": []
              }
            ]
          }
        """.trimIndent()
    }
}
