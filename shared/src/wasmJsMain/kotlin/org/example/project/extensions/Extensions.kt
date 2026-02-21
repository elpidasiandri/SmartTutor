package org.example.project.extensions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
@OptIn(ExperimentalWasmJsInterop::class)
external interface JsPromise<T : JsAny> : JsAny {
    fun <S : JsAny> then(onFulfilled: (T) -> S): JsPromise<S>
    fun <S : JsAny> catch(onRejected: (JsAny) -> S): JsPromise<S>

}

@OptIn(ExperimentalWasmJsInterop::class)
external interface JsString : JsAny

@OptIn(ExperimentalWasmJsInterop::class)
suspend fun <T : JsAny> JsPromise<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        this.then(
            onFulfilled = { value ->
                cont.resume(value)
                value
            }
        ).catch { error ->
            val msg = try { error.toString() } catch (_: Throwable) { null }
            cont.resumeWithException(Throwable(msg ?: "FirebaseError(null)"))
            error
        }
    }

suspend fun JsPromise<JsVoid>.awaitSafe(): Boolean {
    return try {
        this.await()
        true
    } catch (e: Throwable) {
        true
    }
}
external interface JsVoid : JsAny
