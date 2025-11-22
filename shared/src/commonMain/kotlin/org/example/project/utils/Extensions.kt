package org.example.project.utils

val String?.nonNull: String get() = this ?: ""

val Int?.nonNull: Int get() = this ?: -1
val Long?.nonNull: Long get() = this ?: -1

val <T> List<T>?.nonNull: List<T>
    get() = this ?: emptyList()

val Boolean?.nonNull: Boolean
    get() = this ?: false


