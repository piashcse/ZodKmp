package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

/**
 * Schema for validating numbers.
 *
 * By default only numeric values are accepted. Use [Zod.coerce] to opt into
 * accepting numeric strings.
 */
public class ZodNumber private constructor(
    private val validations: List<(Double) -> ZodIssue?>,
    private val coerce: Boolean,
    private val hints: Map<String, JsonElement> = emptyMap(),
) : ZodSchema<Double> {
    public companion object {
        public fun schema(coerce: Boolean = false): ZodNumber = ZodNumber(emptyList(), coerce, emptyMap())
    }

    private fun withValidation(newHints: Map<String, JsonElement> = emptyMap(), validation: (Double) -> ZodIssue?): ZodNumber =
        ZodNumber(validations + validation, coerce, hints + newHints)

    internal fun jsonHints(): Map<String, JsonElement> = hints

    public fun min(value: Double, message: String = "Number must be greater than or equal to $value"): ZodNumber = gte(value, message)

    public fun max(value: Double, message: String = "Number must be less than or equal to $value"): ZodNumber = lte(value, message)

    public fun gt(value: Double, message: String = "Number must be greater than $value"): ZodNumber =
        withValidation(mapOf("exclusiveMinimum" to JsonPrimitive(value))) { number ->
            if (number <= value) issue(ZodIssueCode.TOO_SMALL, message) else null
        }

    public fun gte(value: Double, message: String = "Number must be greater than or equal to $value"): ZodNumber =
        withValidation(mapOf("minimum" to JsonPrimitive(value))) { number ->
            if (number < value) issue(ZodIssueCode.TOO_SMALL, message) else null
        }

    public fun lt(value: Double, message: String = "Number must be less than $value"): ZodNumber =
        withValidation(mapOf("exclusiveMaximum" to JsonPrimitive(value))) { number ->
            if (number >= value) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    public fun lte(value: Double, message: String = "Number must be less than or equal to $value"): ZodNumber =
        withValidation(mapOf("maximum" to JsonPrimitive(value))) { number ->
            if (number > value) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    /**
     * Validates that the number is an integer multiple of [value].
     *
     * Uses a tolerance-based check so decimal steps such as `0.1` work with binary
     * floating point (`0.3` is accepted). A zero step is a programming error.
     */
    public fun multipleOf(value: Double, message: String = "Number must be a multiple of $value"): ZodNumber {
        require(value != 0.0) { "multipleOf value must not be zero" }
        return withValidation(mapOf("multipleOf" to JsonPrimitive(value))) { number ->
            val quotient = number / value
            val rounded = round(quotient)
            val tolerance = 1e-9 * maxOf(1.0, abs(quotient))
            val valid = quotient.isFinite() && abs(quotient - rounded) <= tolerance
            if (valid) null else issue(ZodIssueCode.NOT_MULTIPLE_OF, message)
        }
    }

    public fun int(message: String = "Number must be an integer"): ZodNumber = withValidation(mapOf("integer" to JsonPrimitive(true))) { number ->
        if (!number.isFinite() || number != floor(number)) issue(ZodIssueCode.CUSTOM, message) else null
    }

    public fun positive(message: String = "Number must be positive"): ZodNumber = withValidation(mapOf("exclusiveMinimum" to JsonPrimitive(0.0))) { number ->
        if (number <= 0) issue(ZodIssueCode.TOO_SMALL, message) else null
    }

    public fun negative(message: String = "Number must be negative"): ZodNumber = withValidation(mapOf("exclusiveMaximum" to JsonPrimitive(0.0))) { number ->
        if (number >= 0) issue(ZodIssueCode.TOO_BIG, message) else null
    }

    public fun nonPositive(message: String = "Number must be non-positive (less than or equal to zero)"): ZodNumber =
        withValidation(mapOf("maximum" to JsonPrimitive(0.0))) { number ->
            if (number > 0) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    public fun nonNegative(message: String = "Number must be non-negative"): ZodNumber = withValidation(mapOf("minimum" to JsonPrimitive(0.0))) { number ->
        if (number < 0) issue(ZodIssueCode.TOO_SMALL, message) else null
    }

    public fun range(min: Double, max: Double, message: String = "Number must be between $min and $max"): ZodNumber = withValidation(
        mapOf("minimum" to JsonPrimitive(min), "maximum" to JsonPrimitive(max)),
    ) { number ->
        if (number < min || number > max) issue(ZodIssueCode.CUSTOM, message) else null
    }

    public fun finite(message: String = "Number must be finite"): ZodNumber =
        withValidation { number -> if (!number.isFinite()) issue(ZodIssueCode.NOT_FINITE, message) else null }

    public fun refine(predicate: (Double) -> Boolean, message: (Double) -> String = { "Invalid value: $it" }): ZodNumber = withValidation { value ->
        if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value))
    }

    override fun safeParse(input: Any?): ZodResult<Double> {
        val numberInput =
            when (input) {
                is Number -> input.toDouble()
                is String -> if (coerce) input.trim().toDoubleOrNull() else null
                else -> null
            }

        if (numberInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.number() to accept numeric strings)" else ""
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_TYPE,
                        "Expected number, received ${input.typeName()}$hint",
                        expected = "number",
                        received = input.typeName(),
                    ),
                ),
            )
        }

        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(numberInput)?.let { issues.add(it) }
        }

        return if (issues.isEmpty()) ZodResult.Success(numberInput) else ZodResult.Failure(ZodError(issues))
    }
}
