@file:JsModule("firebase/app")
@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.example.project.registration.firebase
import kotlin.js.*
external fun initializeApp(options: FirebaseOptions): FirebaseApp
@OptIn(ExperimentalWasmJsInterop::class)
external interface FirebaseApp : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsName("FirebaseOptions")
external interface FirebaseOptions: JsAny {
    var apiKey: String
    var authDomain: String
    var projectId: String
    var storageBucket: String
    var messagingSenderId: String
    var appId: String
}

external interface ExerciseAnsweredParams : JsAny

@JsFun(
    """(
        chatId,
        unitIndex,
        unitTitle,
        exerciseIndex,
        exerciseType,
        selectedAnswer,
        correctAnswer,
        isCorrect,
        timeToAnswerMs,
        userId
    ) => ({
        chatId,
        unitIndex,
        unitTitle,
        exerciseIndex,
        exerciseType,
        selectedAnswer,
        correctAnswer,
        isCorrect,
        timeToAnswerMs,
        userId
    })"""
)
external fun createExerciseAnsweredParams(
    chatId: String,
    unitIndex: String,
    unitTitle: String,
    exerciseIndex: String,
    exerciseType: String,
    selectedAnswer: String,
    correctAnswer: String,
    isCorrect: String,
    timeToAnswerMs: String,
    userId: String,
): ExerciseAnsweredParams
@JsFun(
    """(apiKey, authDomain, projectId, storageBucket, messagingSenderId, appId) => ({
        apiKey,
        authDomain,
        projectId,
        storageBucket,
        messagingSenderId,
        appId
    })"""
)
external fun createFirebaseOptions(
    apiKey: String,
    authDomain: String,
    projectId: String,
    storageBucket: String,
    messagingSenderId: String,
    appId: String,
): FirebaseOptions

external interface MessageSentParams : JsAny

@JsFun(
    """(chatId, unitIndex, tab, messageLength, userId) => ({
        chatId,
        unitIndex,
        tab,
        messageLength,
        userId
    })"""
)
external fun createMessageSentParams(
    chatId: String,
    unitIndex: String,
    tab: String,
    messageLength: String,
    userId: String,
): MessageSentParams

external interface TabChangedParams : JsAny

@JsFun(
    """(chatId, tab, unitTitle, userId) => ({
        chatId,
        tab,
        unitTitle,
        userId
    })"""
)
external fun createTabChangedParams(
    chatId: String,
    tab: String,
    unitTitle: String,
    userId: String,
): TabChangedParams

external interface PdfUploadedParams : JsAny

@JsFun(
    """(chatId, documentTitle, unitsCount, language, mode, userId) => ({
        chatId,
        documentTitle,
        unitsCount,
        language,
        mode,
        userId
    })"""
)
external fun createPdfUploadedParams(
    chatId: String,
    documentTitle: String,
    unitsCount: String,
    language: String,
    mode: String,
    userId: String,
): PdfUploadedParams
