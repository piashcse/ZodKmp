package com.piashcse.zodkmp

/**
 * Schema for validating tuples (fixed-length arrays with specific types for each position)
 */
class ZodTuple private constructor(
    private val elementSchemas: List<ZodSchema<*>>,
    private val validations: List<(List<Any?>) -> ZodError?>
) : ZodSchema<List<Any?>> {
    companion object {
        fun schema(vararg schemas: ZodSchema<*>): ZodTuple = ZodTuple(schemas.toList(), emptyList())
        fun schema(schemas: List<ZodSchema<*>>): ZodTuple = ZodTuple(schemas, emptyList())
    }
    
    override fun parse(input: Any?): List<Any?> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<List<Any?>> {
        val listInput = when (input) {
            is List<*> -> input
            is Array<*> -> input.toList()
            else -> null
        }
        
        if (listInput == null) {
            return ZodResult.Failure(ZodError("Expected tuple (array), received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        if (listInput.size != elementSchemas.size) {
            return ZodResult.Failure(ZodError("Tuple must have exactly ${elementSchemas.size} element(s), received ${listInput.size}"))
        }
        
        val parsedElements = mutableListOf<Any?>()
        val errors = mutableListOf<String>()
        
        for ((index, element) in listInput.withIndex()) {
            val schema = elementSchemas[index]
            val elementResult = schema.safeParse(element)
            when (elementResult) {
                is ZodResult.Success -> parsedElements.add(elementResult.data)
                is ZodResult.Failure -> {
                    elementResult.error.errors.forEach { error ->
                        errors.add("[$index]: $error")
                    }
                }
            }
        }
        
        if (errors.isNotEmpty()) {
            return ZodResult.Failure(ZodError(errors))
        }
        
        // Apply tuple-level validations
        for (validation in validations) {
            val error = validation(parsedElements)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(parsedElements)
    }
}