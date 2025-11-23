package org.example.project.utils

import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val String?.nonNull: String get() = this ?: ""

val Int?.nonNull: Int get() = this ?: -1
val Long?.nonNull: Long get() = this ?: -1

val <T> List<T>?.nonNull: List<T>
    get() = this ?: emptyList()

val Boolean?.nonNull: Boolean
    get() = this ?: false

@OptIn(ExperimentalTime::class)
fun Instant.convertToLong(numberMill :Long) : Long{
    return (Clock.System.now() - numberMill.milliseconds).toEpochMilliseconds()
}
