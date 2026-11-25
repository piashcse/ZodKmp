package com.piashcse.zodkmp

/**
 * Schema for validating 64-bit integers (the KMP analog of Zod's `bigint`).
 *
 * By default accepts [Long] and integral [Number] values. Numeric strings are only
 * accepted through [Zod.coerce].
 */
class ZodLong private constructor(
    private val validations: List<(Long) -> ZodIssue?>,
    private val coerce: Boolean
) : ZodSchema<Long> {
    companion object {
        fun schema(coerce: Boolean = false): ZodLong = ZodLong(emptyList(), coerce)
    }

    private fun withValidation(validation: (Long) -> ZodIssue?): ZodLong =
        ZodLong(validations + validation, coerce)

    fun min(value: Long, message: String = "Number must be greater than or equal to $value"): ZodLong =
        withValidation { number -> if (number < value) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun max(value: Long, message: String = "Number must be less than or equal to $value"): ZodLong =
        withValidation { number -> if (number > value) issue(ZodIssueCode.TOO_BIG, message) else null }

    fun positive(message: String = "Number must be positive"): ZodLong =
        withValidation { number -> if (number <= 0) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun negative(message: String = "Number must be negative"): ZodLong =
        withValidation { number -> if (number >= 0) issue(ZodIssueCode.TOO_BIG, message) else null }

    fun nonNegative(message: String = "Number must be non-negative"): ZodLong =
        withValidation { number -> if (number < 0) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun nonPositive(message: String = "Number must be non-positive"): ZodLong =
        withValidation { number -> if (number > 0) issue(ZodIssueCode.TOO_BIG, message) else null }

    override fun safeParse(input: Any?): ZodResult<Long> {
        val longInput = when (input) {
            is Long -> input
            is Number -> {
                // On Kotlin/JS several numeric `is` checks can be true for the same value,
                // so rely on `is Number` plus integrality instead of per-type checks.
                val value = input.toDouble()
                if (value.isFinite() &&
                    value % 1.0 == 0.0 &&
                    value >= Long.MIN_VALUE.toDouble() &&
                    value <= Long.MAX_VALUE.toDouble()
                ) {
                    value.toLong()
                } else {
                    null
                }
            }
            is String -> if (coerce) input.trim().toLongOrNull() else null
            else -> null
        }

        if (longInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.long() to accept numeric strings)" else ""
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_TYPE,
                        "Expected long, received ${input.typeName()}$hint",
                        expected = "long",
                        received = input.typeName()
                    )
                )
            )
        }

        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(longInput)?.let { issues.add(it) }
        }

        return if (issues.isEmpty()) ZodResult.Success(longInput) else ZodResult.Failure(ZodError(issues))
    }
}
