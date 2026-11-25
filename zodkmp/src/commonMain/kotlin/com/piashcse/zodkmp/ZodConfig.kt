package com.piashcse.zodkmp

import kotlin.concurrent.Volatile

/**
 * Process-wide validation configuration, mirroring Zod's `z.config({ errorMap })`.
 *
 * The error map receives every [ZodIssue] and may return a localized message. Returning
 * `null` keeps the built-in English message.
 *
 * ```kotlin
 * ZodConfig.errorMap = { issue ->
 *     when (issue.code) {
 *         ZodIssueCode.REQUIRED -> "Campo obligatorio"
 *         ZodIssueCode.INVALID_TYPE -> "Tipo no válido"
 *         else -> null
 *     }
 * }
 * ```
 *
 * The map is global; set it once during application start-up. Use [reset] in tests.
 */
public object ZodConfig {
    @Volatile
    public var errorMap: ((ZodIssue) -> String?)? = null

    public fun message(issue: ZodIssue): String = errorMap?.invoke(issue) ?: issue.message

    public fun reset() {
        errorMap = null
    }
}
