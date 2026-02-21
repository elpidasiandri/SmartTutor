package org.example.project.jobs

import kotlinx.serialization.json.*

fun JsonElement.toFirestoreValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> when {
        isString -> content
        booleanOrNull != null -> boolean
        longOrNull != null -> long
        doubleOrNull != null -> double
        else -> content
    }
    is JsonObject -> this.mapValues { (_, v) -> v.toFirestoreValue() }
    is JsonArray -> this.map { it.toFirestoreValue() }
}
