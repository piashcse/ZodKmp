package com.piashcse.zodkmp

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Schema for validating arrays.
 */
public class ZodArray<T> private constructor(
    public val elementSchema: ZodSchema<T>,
    private val validations: List<(List<T>) -> ZodIssue?>,
    private val hints: Map<String, JsonElement> = emptyMap(),
) : ZodSchema<List<T>> {
    public companion object {
        public fun <T> schema(elementSchema: ZodSchema<T>): ZodArray<T> = ZodArray(elementSchema, emptyList(), emptyMap())
    }

    /**
     * Returns a copy with a different element schema, keeping array-level constraints.
     * Used by `deepPartial` so constraints such as `min`/`unique` are not lost.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <R> withElement(elementSchema: ZodSchema<R>): ZodArray<R> = ZodArray(elementSchema, validations as List<(List<R>) -> ZodIssue?>, hints)

    private fun withValidation(newHints: Map<String, JsonElement> = emptyMap(), validation: (List<T>) -> ZodIssue?): ZodArray<T> =
        ZodArray(elementSchema, validations + validation, hints + newHints)

    internal fun jsonHints(): Map<String, JsonElement> = hints

    public fun min(minLength: Int, message: String = "Array must contain at least $minLength element(s)"): ZodArray<T> =
        withValidation(mapOf("minItems" to JsonPrimitive(minLength))) { value ->
            if (value.size < minLength) issue(ZodIssueCode.TOO_SMALL, message) else null
        }

    public fun max(maxLength: Int, message: String = "Array must contain at most $maxLength element(s)"): ZodArray<T> =
        withValidation(mapOf("maxItems" to JsonPrimitive(maxLength))) { value ->
            if (value.size > maxLength) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    public fun length(exact: Int, message: String = "Array must contain exactly $exact element(s)"): ZodArray<T> =
        withValidation(mapOf("minItems" to JsonPrimitive(exact), "maxItems" to JsonPrimitive(exact))) { value ->
            when {
                value.size < exact -> issue(ZodIssueCode.TOO_SMALL, message)
                value.size > exact -> issue(ZodIssueCode.TOO_BIG, "Array must contain exactly $exact element(s)")
                else -> null
            }
        }

    public fun nonEmpty(message: String = "Array cannot be empty"): ZodArray<T> =
        withValidation(mapOf("minItems" to JsonPrimitive(1))) { value -> if (value.isEmpty()) issue(ZodIssueCode.TOO_SMALL, message) else null }

    public fun nonempty(message: String = "Array cannot be empty"): ZodArray<T> = nonEmpty(message)

    public fun unique(message: String = "Array elements must be unique"): ZodArray<T> = withValidation(mapOf("uniqueItems" to JsonPrimitive(true))) { value ->
        if (value.toSet().size != value.size) issue(ZodIssueCode.CUSTOM, message) else null
    }

    /**
     * Adds a custom refinement while preserving the [ZodArray] type.
     */
    public fun refine(predicate: (List<T>) -> Boolean, message: (List<T>) -> String = { "Invalid value: $it" }): ZodArray<T> = withValidation { value ->
        if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value))
    }

    override fun safeParse(input: Any?): ZodResult<List<T>> {
        val listInput =
            input.asListOrNull()
                ?: return ZodResult.Failure(typeError("array", input))

        val parsedElements = mutableListOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
            when (val elementResult = elementSchema.safeParse(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure ->
                    issues.addAll(
                        elementResult.error.issues.prepend(ZodPathSegment.Index(index)),
                    )
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedElements)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<List<T>> {
        val listInput =
            input.asListOrNull()
                ?: return ZodResult.Failure(typeError("array", input))

        val parsedElements = mutableListOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
            when (val elementResult = elementSchema.safeParseSuspend(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure ->
                    issues.addAll(
                        elementResult.error.issues.prepend(ZodPathSegment.Index(index)),
                    )
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedElements)
    }

    private fun applyValidations(elements: List<T>): ZodResult<List<T>> {
        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(elements)?.let { issues.add(it) }
        }
        return if (issues.isEmpty()) ZodResult.Success(elements) else ZodResult.Failure(ZodError(issues))
    }
}
