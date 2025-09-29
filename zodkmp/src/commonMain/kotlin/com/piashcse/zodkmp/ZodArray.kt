package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for validating arrays
 */
class ZodArray<T> private constructor(
    private val elementSchema: ZodSchema<T>,
    private val validations: List<(List<T>) -> ZodError?>
) : ZodSchema<List<T>> {
    companion object {
        fun <T> schema(elementSchema: ZodSchema<T>): ZodArray<T> = ZodArray(elementSchema, emptyList())
    }
    
    fun min(minLength: Int, message: String = "Array must contain at least $minLength element(s)"): ZodArray<T> {
        val validation: (List<T>) -> ZodError? = { value ->
            if (value.size < minLength) ZodError(message) else null
        }
        return ZodArray(elementSchema, validations + validation)
    }
    
    fun max(maxLength: Int, message: String = "Array must contain at most $maxLength element(s)"): ZodArray<T> {
        val validation: (List<T>) -> ZodError? = { value ->
            if (value.size > maxLength) ZodError(message) else null
        }
        return ZodArray(elementSchema, validations + validation)
    }
    
    fun length(exact: Int, message: String = "Array must contain exactly $exact element(s)"): ZodArray<T> {
        val validation: (List<T>) -> ZodError? = { value ->
            if (value.size != exact) ZodError(message) else null
        }
        return ZodArray(elementSchema, validations + validation)
    }
    
    override fun parse(input: Any?): List<T> {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<List<T>> {
        val listInput = when (input) {
            is List<*> -> input
            is Array<*> -> input.toList()
            else -> null
        }
        
        if (listInput == null) {
            return ZodResult.Failure(ZodError("Expected array, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        // Validate each element in the array
        val parsedElements = mutableListOf<T>()
        val errors = mutableListOf<String>()
        
        for ((index, element) in listInput.withIndex()) {
            val elementResult = elementSchema.safeParse(element)
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
        
        // Apply array-level validations
        for (validation in validations) {
            val error = validation(parsedElements)
            if (error != null) {
                return ZodResult.Failure(error)
            }
        }
        
        return ZodResult.Success(parsedElements)
    }
}