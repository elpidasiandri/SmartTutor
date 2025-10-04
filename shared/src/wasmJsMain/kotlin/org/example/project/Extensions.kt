package org.example.project
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
external interface JsVoid : JsAny
