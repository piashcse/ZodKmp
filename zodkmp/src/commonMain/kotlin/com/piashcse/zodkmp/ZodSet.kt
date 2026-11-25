package com.piashcse.zodkmp

/**
 * Schema for validating sets.
 *
 * Only [Set] values are accepted; lists are not silently converted (use
 * `Zod.array(...)` or convert explicitly).
 */
class ZodSet<T> private constructor(
    val elementSchema: ZodSchema<T>,
    private val validations: List<(Set<T>) -> ZodIssue?>
) : ZodSchema<Set<T>> {
    companion object {
        fun <T> schema(elementSchema: ZodSchema<T>): ZodSet<T> = ZodSet(elementSchema, emptyList())
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <R> withElement(elementSchema: ZodSchema<R>): ZodSet<R> =
        ZodSet(elementSchema, validations as List<(Set<R>) -> ZodIssue?>)

    private fun withValidation(validation: (Set<T>) -> ZodIssue?): ZodSet<T> =
        ZodSet(elementSchema, validations + validation)

    fun min(minSize: Int, message: String = "Set must contain at least $minSize element(s)"): ZodSet<T> =
        withValidation { value -> if (value.size < minSize) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun max(maxSize: Int, message: String = "Set must contain at most $maxSize element(s)"): ZodSet<T> =
        withValidation { value -> if (value.size > maxSize) issue(ZodIssueCode.TOO_BIG, message) else null }

    fun nonEmpty(message: String = "Set cannot be empty"): ZodSet<T> =
        withValidation { value -> if (value.isEmpty()) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun refine(predicate: (Set<T>) -> Boolean, message: (Set<T>) -> String = { "Invalid value: $it" }): ZodSet<T> =
        withValidation { value -> if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value)) }

    override fun safeParse(input: Any?): ZodResult<Set<T>> {
        val setInput = input as? Set<*>
            ?: return ZodResult.Failure(typeError("set", input))

        val parsedElements = mutableSetOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in setInput.withIndex()) {
            when (val elementResult = elementSchema.safeParse(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> issues.addAll(
                    elementResult.error.issues.prepend(ZodPathSegment.Index(index))
                )
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedElements)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Set<T>> {
        val setInput = input as? Set<*>
            ?: return ZodResult.Failure(typeError("set", input))

        val parsedElements = mutableSetOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in setInput.withIndex()) {
            when (val elementResult = elementSchema.safeParseSuspend(element)) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> issues.addAll(
                    elementResult.error.issues.prepend(ZodPathSegment.Index(index))
                )
            }
        }

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        return applyValidations(parsedElements)
    }

    private fun applyValidations(elements: Set<T>): ZodResult<Set<T>> {
        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(elements)?.let { issues.add(it) }
        }
        return if (issues.isEmpty()) ZodResult.Success(elements) else ZodResult.Failure(ZodError(issues))
    }
}
