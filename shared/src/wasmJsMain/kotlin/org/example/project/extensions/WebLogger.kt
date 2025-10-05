package org.example.project.extensions

@OptIn(ExperimentalWasmJsInterop::class)
fun logD(msg: String): Unit = js("console.log(msg)")

@OptIn(ExperimentalWasmJsInterop::class)
fun logE(msg: String): Unit = js("console.error(msg)")

@OptIn(ExperimentalWasmJsInterop::class)
fun logE(msg: String, t: String): Unit = js("console.error(msg, t)")


