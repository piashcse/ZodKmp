package com.piashcse.zodkmp

/**
 * Schema that validates that the input is an instance of a specific type.
 * Created via `Zod.instanceOf<T>()` (reified, no reflection).
 */
class ZodInstanceOf<T : Any> @PublishedApi internal constructor(
    private val isInstance: (Any?) -> Boolean,
    private val validations: List<(T) -> ZodError?>
) : ZodSchema<T> {
    companion object {
        inline fun <reified T : Any> schema(): ZodInstanceOf<T> {
            return ZodInstanceOf({ it is T }, emptyList())
        }
    }
    
    override fun parse(input: Any?): T {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        if (!isInstance(input)) {
            return ZodResult.Failure(ZodError("Expected instance of type, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        @Suppress("UNCHECKED_CAST")
        val typedInput = input as T
        
        val errors = mutableListOf<String>()
        for (validation in validations) {
            val error = validation(typedInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) ZodResult.Success(typedInput) else ZodResult.Failure(ZodError(errors))
    }
}
