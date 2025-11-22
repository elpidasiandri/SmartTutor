package org.example.project.tutor.networkModel

data class ChatSessionStartResponse(
    val material: MaterialState?,
    val learningUnit: LearningUnitState?,
    val instance: LearningUnitInstanceState?,
    val chat: ChatState?,
)

data class MaterialState(
    val materialId: String?,
    val fileUploaded: Boolean?,
    val fileName: String?,
    val canUpload: Boolean?, // user CAN upload only if no LU for this material
    val totalUnits: Int?,
)

data class LearningUnitState(
    val luId: String?,
    val title: String?,
    val orderIndex: Int?,
    val quizAvailable: Boolean?,
    val analysisCompleted: Boolean?, // analysis before quiz
    val quizCompleted: Boolean?,
    val nextAction: NextAction?
)

enum class NextAction {
    CONTINUE_CHAT,     // LU is active, continue messages
    DO_ANALYSIS,       // Waiting for analysis by AI
    DO_QUIZ,           // Must complete quiz
    CHOOSE_NEXT_LU,    // analysis + quiz done → ready to move on
    FINISH_LU,         // user said "no more", LU becomes inactive
    None
}

enum class LUStatus { ACTIVE, COMPLETED, INACTIVE }

data class LearningUnitInstanceState(
    val instanceId: String?,
    val status: LUStatus?,
    val progressPercent: Int?,
    val startedAt: Long?,
    val lastUpdatedAt: Long?,
    val canContinue: Boolean?,   // true if ACTIVE
    val canRestart: Boolean?,    // true if COMPLETED
    val isLocked: Boolean?,      // true if FINISHED and cannot study again
)

data class ChatState(
    val chatSessionId: String?,
    val createNewSession: Boolean?,  // true if LU is completed or inactive
    val messages: List<ChatMessageState?>?,
)

data class ChatMessageState(
    val id: String?,
    val sender: String?,  // "USER", "AI"
    val content: String?,
    val timestamp: Long?,
    val type: String,    // "TEXT", "FILE", "MARKDOWN"
)

// ΠΑΡΑΔΕΙΓΜΑ RESPONSE JSON (ΤΕΛΕΙΟ ΓΙΑ ΚΛΙΕΝΤ)
//Case: Ο χρήστης μπαίνει σε ένα active LU με existing chat
//{
//  "material": {
//    "materialId": "mat_123",
//    "fileUploaded": true,
//    "fileName": "my_physics.pdf",
//    "canUpload": false,
//    "totalUnits": 8
//  },
//  "learningUnit": {
//    "luId": "lu_3",
//    "title": "Newton's Second Law",
//    "orderIndex": 2,
//    "quizAvailable": true,
//    "analysisCompleted": true,
//    "quizCompleted": false,
//    "nextAction": "DO_QUIZ"
//  },
//  "instance": {
//    "instanceId": "inst_455",
//    "status": "ACTIVE",
//    "progressPercent": 70,
//    "startedAt": 1710004000,
//    "lastUpdatedAt": 1710008000,
//    "canContinue": true,
//    "canRestart": false,
//    "isLocked": false
//  },
//  "chat": {
//    "chatSessionId": "chat_888",
//    "createNewSession": false,
//    "messages": [
//      {
//        "id": "m1",
//        "sender": "AI",
//        "content": "Let's start Newton's second law.",
//        "timestamp": 1710004001,
//        "type": "TEXT"
//      },
//      {
//        "id": "m2",
//        "sender": "USER",
//        "content": "Ok, explain it please",
//        "timestamp": 1710004002,
//        "type": "TEXT"
//      }
//    ]
//  }
//}

//Case: Ο χρήστης ανοίγει ένα COMPLETED LU → πρέπει να δημιουργηθεί ΝΕΟ chat
//{
//  "material": {
//    "materialId": "mat_123",
//    "fileUploaded": true,
//    "fileName": "my_physics.pdf",
//    "canUpload": false,
//    "totalUnits": 8
//  },
//  "learningUnit": {
//    "luId": "lu_3",
//    "title": "Newton's Second Law",
//    "orderIndex": 2,
//    "quizAvailable": true,
//    "analysisCompleted": true,
//    "quizCompleted": true,
//    "nextAction": "CHOOSE_NEXT_LU"
//  },
//  "instance": {
//    "instanceId": "inst_455",
//    "status": "COMPLETED",
//    "progressPercent": 100,
//    "startedAt": 1710004000,
//    "lastUpdatedAt": 1710009000,
//    "canContinue": false,
//    "canRestart": true,
//    "isLocked": false
//  },
//  "chat": {
//    "chatSessionId": null,
//    "createNewSession": true,
//    "messages": []
//  }
//}
//
//🎯 Case: Ο χρήστης είπε “όχι δεν θέλω άλλη ανάλυση” → LU becomes inactive
//{
//  "learningUnit": {
//    "nextAction": "FINISH_LU"
//  },
//  "instance": {
//    "status": "INACTIVE",
//    "isLocked": true
//  },
//  "chat": {
//    "createNewSession": false,
//    "messages": []
//  }
//}
