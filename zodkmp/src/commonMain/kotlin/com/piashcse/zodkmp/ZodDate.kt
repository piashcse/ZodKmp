package com.piashcse.zodkmp

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

/**
 * Schema for validating [LocalDateTime] values.
 *
 * By default only [LocalDateTime] (and [LocalDate], normalized to midnight) values are
 * accepted. Use [Zod.coerce] to also accept ISO-8601 strings.
 */
class ZodDate private constructor(
    private val validations: List<(LocalDateTime) -> ZodError?>,
    private val coerce: Boolean
) : ZodSchema<LocalDateTime> {
    companion object {
        fun schema(coerce: Boolean = false): ZodDate = ZodDate(emptyList(), coerce)

        private fun parseString(value: String): LocalDateTime? {
            if (value.contains('T') || value.contains(' ')) {
                val normalized = value.replace(' ', 'T')
                val withoutOffset = when {
                    normalized.endsWith("Z") -> normalized.dropLast(1)
                    else -> {
                        val plus = normalized.lastIndexOf('+')
                        val minus = normalized.lastIndexOf('-')
                        val sign = maxOf(plus, minus)
                        if (sign > normalized.indexOf('T')) normalized.substring(0, sign) else normalized
                    }
                }
                return runCatching { LocalDateTime.parse(withoutOffset) }.getOrNull()
            }
            return runCatching { LocalDate.parse(value).atTime(0, 0, 0) }.getOrNull()
        }
    }

    private fun withValidation(validation: (LocalDateTime) -> ZodError?): ZodDate =
        ZodDate(validations + validation, coerce)

    fun min(minDate: LocalDateTime, message: String = "Date must be greater than or equal to $minDate"): ZodDate =
        withValidation { value -> if (value < minDate) ZodError(message) else null }

    fun max(maxDate: LocalDateTime, message: String = "Date must be less than or equal to $maxDate"): ZodDate =
        withValidation { value -> if (value > maxDate) ZodError(message) else null }

    override fun parse(input: Any?): LocalDateTime {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<LocalDateTime> {
        val dateInput = when (input) {
            is LocalDateTime -> input
            is LocalDate -> input.atTime(0, 0, 0)
            is String -> if (coerce) parseString(input) else null
            else -> null
        }

        if (dateInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.date() to accept ISO-8601 strings)" else ""
            return ZodResult.Failure(ZodError("Expected date, received ${input.typeName()}$hint"))
        }

        val errors = mutableListOf<String>()
        for (validation in validations) {
            validation(dateInput)?.let { errors.addAll(it.errors) }
        }

        return if (errors.isEmpty()) {
            ZodResult.Success(dateInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}
