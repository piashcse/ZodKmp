package com.piashcse.zodkmp

/**
 * Schema for validating sets. Accepts [Set] (or any [Iterable]) and returns a [Set].
 */
class ZodSet<T> private constructor(
    val elementSchema: ZodSchema<T>,
    private val validations: List<(Set<T>) -> ZodError?>
) : ZodSchema<Set<T>> {
    companion object {
        fun <T> schema(elementSchema: ZodSchema<T>): ZodSet<T> = ZodSet(elementSchema, emptyList())
    }
    
    fun min(minSize: Int, message: String = "Set must contain at least $minSize element(s)"): ZodSet<T> {
        val validation: (Set<T>) -> ZodError? = { value ->
            if (value.size < minSize) ZodError(message) else null
        }
        return ZodSet(elementSchema, validations + validation)
    }
    
    fun max(maxSize: Int, message: String = "Set must contain at most $maxSize element(s)"): ZodSet<T> {
        val validation: (Set<T>) -> ZodError? = { value ->
            if (value.size > maxSize) ZodError(message) else null
        }
        return ZodSet(elementSchema, validations + validation)
    }
    
    fun nonEmpty(message: String = "Set cannot be empty"): ZodSet<T> {
        val validation: (Set<T>) -> ZodError? = { value ->
            if (value.isEmpty()) ZodError(message) else null
        }
        return ZodSet(elementSchema, validations + validation)
    }
    
    override fun parse(input: Any?): Set<T> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<Set<T>> {
        val iterableInput = when (input) {
            is Set<*> -> input
            is Iterable<*> -> input
            is Array<*> -> input.toList()
            else -> null
        }
        
        if (iterableInput == null) {
            return ZodResult.Failure(ZodError("Expected set, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val parsedElements = mutableSetOf<T>()
        val errors = mutableListOf<String>()
        
        for (element in iterableInput) {
            val elementResult = elementSchema.safeParse(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> {
                    elementResult.error.errors.forEach { error -> errors.add(error) }
                }
            }
        }
        
        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }
        
        for (validation in validations) {
            val error = validation(parsedElements)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(parsedElements)
    }
}
