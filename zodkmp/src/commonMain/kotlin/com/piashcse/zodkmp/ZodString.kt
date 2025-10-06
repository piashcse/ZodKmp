package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Base interface for all Zod schemas
 */
interface ZodSchema<T> {
    fun parse(input: Any?): T
    fun safeParse(input: Any?): ZodResult<T>
}

/**
 * Result class for safe parsing operations
 */
sealed class ZodResult<out T> {
    data class Success<T>(val data: T) : ZodResult<T>()
    data class Failure(val error: ZodError) : ZodResult<Nothing>()
}

/**
 * Error class containing validation errors
 */
data class ZodError(
    val errors: List<String> = emptyList()
) {
    constructor(error: String) : this(listOf(error))
    
    fun addError(error: String): ZodError = ZodError(errors + error)
    fun addErrors(errors: List<String>): ZodError = ZodError(this.errors + errors)
}

/**
 * Schema for validating strings
 */
@JvmInline
value class ZodString private constructor(private val validations: List<(String) -> ZodError?>) : ZodSchema<String> {
    companion object {
        fun schema(): ZodString = ZodString(emptyList())
    }
    
    fun min(length: Int, message: String = "String must be at least $length characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length < length) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun max(length: Int, message: String = "String must be at most $length characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length > length) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun length(exact: Int, message: String = "String must be exactly $exact characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length != exact) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun email(message: String = "Invalid email format"): ZodString {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        val validation: (String) -> ZodError? = { value ->
            if (!emailRegex.matches(value)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun url(message: String = "Invalid URL format"): ZodString {
        val urlRegex = Regex("^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$")
        val validation: (String) -> ZodError? = { value ->
            if (!urlRegex.matches(value)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun regex(pattern: Regex, message: String = "String does not match required pattern"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!pattern.matches(value)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun startsWith(prefix: String, message: String = "String must start with '$prefix'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.startsWith(prefix)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun endsWith(suffix: String, message: String = "String must end with '$suffix'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.endsWith(suffix)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }
    
    fun includes(substring: String, message: String = "String must contain '$substring'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.contains(substring)) ZodError(message) else null
        }
        return ZodString(validations + validation)
    }

    override fun parse(input: Any?): String {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<String> {
        val stringInput = input as? String
        if (stringInput == null) {
            return ZodResult.Failure(ZodError("Expected string, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(stringInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(stringInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}