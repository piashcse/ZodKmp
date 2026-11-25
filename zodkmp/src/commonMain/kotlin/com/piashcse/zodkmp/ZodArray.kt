package com.piashcse.zodkmp

/**
 * Schema for validating arrays.
 */
class ZodArray<T> private constructor(
    val elementSchema: ZodSchema<T>,
    private val validations: List<(List<T>) -> ZodError?>
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
        ZodArray(elementSchema, validations as List<(List<R>) -> ZodError?>)

    private fun withValidation(validation: (List<T>) -> ZodError?): ZodArray<T> =
        ZodArray(elementSchema, validations + validation)

    fun min(minLength: Int, message: String = "Array must contain at least $minLength element(s)"): ZodArray<T> =
        withValidation { value -> if (value.size < minLength) ZodError(message) else null }

    fun max(maxLength: Int, message: String = "Array must contain at most $maxLength element(s)"): ZodArray<T> =
        withValidation { value -> if (value.size > maxLength) ZodError(message) else null }

    fun length(exact: Int, message: String = "Array must contain exactly $exact element(s)"): ZodArray<T> =
        withValidation { value -> if (value.size != exact) ZodError(message) else null }

    fun nonEmpty(message: String = "Array cannot be empty"): ZodArray<T> =
        withValidation { value -> if (value.isEmpty()) ZodError(message) else null }

    fun nonempty(message: String = "Array cannot be empty"): ZodArray<T> = nonEmpty(message)

    fun unique(message: String = "Array elements must be unique"): ZodArray<T> =
        withValidation { value -> if (value.toSet().size != value.size) ZodError(message) else null }

    override fun parse(input: Any?): List<T> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<List<T>> {
        val listInput = input.asListOrNull()
            ?: return ZodResult.Failure(ZodError("Expected array, received ${input.typeName()}"))

        val parsedElements = mutableListOf<T>()
        val errors = mutableListOf<String>()

        for ((index, element) in listInput.withIndex()) {
            val elementResult = elementSchema.safeParse(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> {
                    elementResult.error.errors.forEach { error -> errors.add("[$index]: $error") }
                }
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

    override suspend fun safeParseSuspend(input: Any?): ZodResult<List<T>> {
        val listInput = input.asListOrNull()
            ?: return ZodResult.Failure(ZodError("Expected array, received ${input.typeName()}"))

        val parsedElements = mutableListOf<T>()
        val errors = mutableListOf<String>()

        for ((index, element) in listInput.withIndex()) {
            val elementResult = elementSchema.safeParseSuspend(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> {
                    elementResult.error.errors.forEach { error -> errors.add("[$index]: $error") }
                }
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
