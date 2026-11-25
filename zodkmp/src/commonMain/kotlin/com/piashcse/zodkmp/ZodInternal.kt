package com.piashcse.zodkmp

/**
 * Internal sentinel used to distinguish an omitted object property from an explicit `null`.
 *
 * It never escapes the public API: schemas that accept "missing" input ([ZodOptional],
 * [ZodDefault], [ZodAny], [ZodUnknown], [ZodUndefined]) translate it before returning.
 */
internal object ZodAbsent

internal fun Any?.typeName(): String =
    when {
        this === ZodAbsent -> "undefined"
        this == null -> "null"
        else -> this::class.simpleName ?: "unknown"
    }

internal fun zodEquals(
    left: Any?,
    right: Any?,
): Boolean {
    if (left === right) return true
    if (left is Number && right is Number) {
        return left.toDouble() == right.toDouble()
    }
    return left == right
}

internal fun Any?.asListOrNull(): List<*>? =
    when (this) {
        is List<*> -> this
        is Array<*> -> toList()
        is IntArray -> toList()
        is LongArray -> toList()
        is DoubleArray -> toList()
        is FloatArray -> toList()
        is ShortArray -> toList()
        is ByteArray -> toList()
        is BooleanArray -> toList()
        is CharArray -> toList()
        else -> null
    }
