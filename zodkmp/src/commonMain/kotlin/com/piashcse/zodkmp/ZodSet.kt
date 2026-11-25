package com.piashcse.zodkmp

/**
 * Schema for validating sets.
 *
 * Only [Set] values are accepted; lists are not silently converted (use
 * `Zod.array(...)` or convert explicitly).
 */
class ZodSet<T> private constructor(
    val elementSchema: ZodSchema<T>,
    private val validations: List<(Set<T>) -> ZodError?>
) : ZodSchema<Set<T>> {
    companion object {
        fun <T> schema(elementSchema: ZodSchema<T>): ZodSet<T> = ZodSet(elementSchema, emptyList())
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <R> withElement(elementSchema: ZodSchema<R>): ZodSet<R> =
        ZodSet(elementSchema, validations as List<(Set<R>) -> ZodError?>)

    private fun withValidation(validation: (Set<T>) -> ZodError?): ZodSet<T> =
        ZodSet(elementSchema, validations + validation)

    fun min(minSize: Int, message: String = "Set must contain at least $minSize element(s)"): ZodSet<T> =
        withValidation { value -> if (value.size < minSize) ZodError(message) else null }

    fun max(maxSize: Int, message: String = "Set must contain at most $maxSize element(s)"): ZodSet<T> =
        withValidation { value -> if (value.size > maxSize) ZodError(message) else null }

    fun nonEmpty(message: String = "Set cannot be empty"): ZodSet<T> =
        withValidation { value -> if (value.isEmpty()) ZodError(message) else null }

    override fun parse(input: Any?): Set<T> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<Set<T>> {
        val setInput = input as? Set<*>
            ?: return ZodResult.Failure(ZodError("Expected set, received ${input.typeName()}"))

        val parsedElements = mutableSetOf<T>()
        val errors = mutableListOf<String>()

        for (element in setInput) {
            val elementResult = elementSchema.safeParse(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> errors.addAll(elementResult.error.errors)
            }
        }

        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }

        for (validation in validations) {
            validation(parsedElements)?.let { return ZodResult.Failure(it) }
        }

        return ZodResult.Success(parsedElements)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<Set<T>> {
        val setInput = input as? Set<*>
            ?: return ZodResult.Failure(ZodError("Expected set, received ${input.typeName()}"))

        val parsedElements = mutableSetOf<T>()
        val errors = mutableListOf<String>()

        for (element in setInput) {
            val elementResult = elementSchema.safeParseSuspend(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> errors.addAll(elementResult.error.errors)
            }
        }

        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }

        for (validation in validations) {
            validation(parsedElements)?.let { return ZodResult.Failure(it) }
        }

        return ZodResult.Success(parsedElements)
    }
}
