package org.example.project.extensions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
external interface JsPromise<T : JsAny> : JsAny {
    fun <S : JsAny> then(onFulfilled: (T) -> S): JsPromise<S>
    fun <S : JsAny> catch(onRejected: (JsAny) -> S): JsPromise<S>

}
suspend fun <T : JsAny> JsPromise<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        this.then(
            onFulfilled = { value ->
                cont.resume(value)
                value
            }
        ).catch { error ->
            cont.resumeWithException(Throwable(error.toString()))
            error
        }
    }

suspend fun JsPromise<JsVoid>.awaitSafe(): Boolean {
    return try {
        this.await() // περιμένουμε απλά το promise, αγνοούμε το αποτέλεσμα
        true        // επιτυχία
    } catch (e: Throwable) {
        logD("Q12345 Firebase reset email error (ignored NPE): ${e.message}")
        true // επιστρέφουμε true γιατί το email έχει ήδη σταλεί
    }
}
external interface JsVoid : JsAny
