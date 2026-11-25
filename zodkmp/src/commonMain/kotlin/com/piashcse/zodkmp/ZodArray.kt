package com.piashcse.zodkmp

/**
 * Schema for validating arrays.
 */
class ZodArray<T> private constructor(
    val elementSchema: ZodSchema<T>,
    private val validations: List<(List<T>) -> ZodIssue?>
) : ZodSchema<List<T>> {
    companion object {
        fun <T> schema(elementSchema: ZodSchema<T>): ZodArray<T> = ZodArray(elementSchema, emptyList())
    }

    /**
     * Returns a copy with a different element schema, keeping array-level constraints.
     * Used by `deepPartial` so constraints such as `min`/`unique` are not lost.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <R> withElement(elementSchema: ZodSchema<R>): ZodArray<R> =
        ZodArray(elementSchema, validations as List<(List<R>) -> ZodIssue?>)

    private fun withValidation(validation: (List<T>) -> ZodIssue?): ZodArray<T> =
        ZodArray(elementSchema, validations + validation)

    fun min(minLength: Int, message: String = "Array must contain at least $minLength element(s)"): ZodArray<T> =
        withValidation { value ->
            if (value.size < minLength) issue(ZodIssueCode.TOO_SMALL, message) else null
        }

    fun max(maxLength: Int, message: String = "Array must contain at most $maxLength element(s)"): ZodArray<T> =
        withValidation { value ->
            if (value.size > maxLength) issue(ZodIssueCode.TOO_BIG, message) else null
        }

    fun length(exact: Int, message: String = "Array must contain exactly $exact element(s)"): ZodArray<T> =
        withValidation { value ->
            when {
                value.size < exact -> issue(ZodIssueCode.TOO_SMALL, message)
                value.size > exact -> issue(ZodIssueCode.TOO_BIG, "Array must contain exactly $exact element(s)")
                else -> null
            }
        }

    fun nonEmpty(message: String = "Array cannot be empty"): ZodArray<T> =
        withValidation { value -> if (value.isEmpty()) issue(ZodIssueCode.TOO_SMALL, message) else null }

    fun nonempty(message: String = "Array cannot be empty"): ZodArray<T> = nonEmpty(message)

    fun unique(message: String = "Array elements must be unique"): ZodArray<T> =
        withValidation { value ->
            if (value.toSet().size != value.size) issue(ZodIssueCode.CUSTOM, message) else null
        }

    /**
     * Adds a custom refinement while preserving the [ZodArray] type.
     */
    fun refine(predicate: (List<T>) -> Boolean, message: (List<T>) -> String = { "Invalid value: $it" }): ZodArray<T> =
        withValidation { value ->
            if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value))
        }

    override fun safeParse(input: Any?): ZodResult<List<T>> {
        val listInput = input.asListOrNull()
            ?: return ZodResult.Failure(typeError("array", input))

        val parsedElements = mutableListOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
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

    override suspend fun safeParseSuspend(input: Any?): ZodResult<List<T>> {
        val listInput = input.asListOrNull()
            ?: return ZodResult.Failure(typeError("array", input))

        val parsedElements = mutableListOf<T>()
        val issues = mutableListOf<ZodIssue>()

        for ((index, element) in listInput.withIndex()) {
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

    private fun applyValidations(elements: List<T>): ZodResult<List<T>> {
        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(elements)?.let { issues.add(it) }
        }
        return if (issues.isEmpty()) ZodResult.Success(elements) else ZodResult.Failure(ZodError(issues))
    }
}
