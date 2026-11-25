package com.piashcse.zodkmp

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

/**
 * Schema for validating numbers.
 *
 * By default only numeric values are accepted. Use [Zod.coerce] to opt into
 * accepting numeric strings.
 */
class ZodNumber private constructor(
    private val validations: List<(Double) -> ZodError?>,
    private val coerce: Boolean
) : ZodSchema<Double> {
    companion object {
        fun schema(coerce: Boolean = false): ZodNumber = ZodNumber(emptyList(), coerce)
    }

    private fun withValidation(validation: (Double) -> ZodError?): ZodNumber =
        ZodNumber(validations + validation, coerce)

    fun min(value: Double, message: String = "Number must be greater than or equal to $value"): ZodNumber =
        gte(value, message)

    fun max(value: Double, message: String = "Number must be less than or equal to $value"): ZodNumber =
        lte(value, message)

    fun gt(value: Double, message: String = "Number must be greater than $value"): ZodNumber =
        withValidation { number -> if (number <= value) ZodError(message) else null }

    fun gte(value: Double, message: String = "Number must be greater than or equal to $value"): ZodNumber =
        withValidation { number -> if (number < value) ZodError(message) else null }

    fun lt(value: Double, message: String = "Number must be less than $value"): ZodNumber =
        withValidation { number -> if (number >= value) ZodError(message) else null }

    fun lte(value: Double, message: String = "Number must be less than or equal to $value"): ZodNumber =
        withValidation { number -> if (number > value) ZodError(message) else null }

    /**
     * Validates that the number is an integer multiple of [value].
     *
     * Uses a tolerance-based check so decimal steps such as `0.1` work with binary
     * floating point (`0.3` is accepted). A zero step is a programming error.
     */
    fun multipleOf(value: Double, message: String = "Number must be a multiple of $value"): ZodNumber {
        require(value != 0.0) { "multipleOf value must not be zero" }
        return withValidation { number ->
            val quotient = number / value
            val rounded = round(quotient)
            val tolerance = 1e-9 * maxOf(1.0, abs(quotient))
            val valid = quotient.isFinite() && abs(quotient - rounded) <= tolerance
            if (valid) null else ZodError(message)
        }
    }

    fun int(message: String = "Number must be an integer"): ZodNumber =
        withValidation { number ->
            if (!number.isFinite() || number != floor(number)) ZodError(message) else null
        }

    fun positive(message: String = "Number must be positive"): ZodNumber =
        withValidation { number -> if (number <= 0) ZodError(message) else null }

    fun negative(message: String = "Number must be negative"): ZodNumber =
        withValidation { number -> if (number >= 0) ZodError(message) else null }

    fun nonPositive(message: String = "Number must be non-positive (less than or equal to zero)"): ZodNumber =
        withValidation { number -> if (number > 0) ZodError(message) else null }

    fun nonNegative(message: String = "Number must be non-negative"): ZodNumber =
        withValidation { number -> if (number < 0) ZodError(message) else null }

    fun range(min: Double, max: Double, message: String = "Number must be between $min and $max"): ZodNumber =
        withValidation { number -> if (number < min || number > max) ZodError(message) else null }

    fun finite(message: String = "Number must be finite"): ZodNumber =
        withValidation { number -> if (!number.isFinite()) ZodError(message) else null }

    override fun parse(input: Any?): Double {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<Double> {
        val numberInput = when (input) {
            is Number -> input.toDouble()
            is String -> if (coerce) input.trim().toDoubleOrNull() else null
            else -> null
        }

        if (numberInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.number() to accept numeric strings)" else ""
            return ZodResult.Failure(ZodError("Expected number, received ${input.typeName()}$hint"))
        }

        val errors = mutableListOf<String>()
        for (validation in validations) {
            validation(numberInput)?.let { errors.addAll(it.errors) }
        }

        return if (errors.isEmpty()) {
            ZodResult.Success(numberInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}
