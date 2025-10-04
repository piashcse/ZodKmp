package com.piashcse.zodkmp

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

/**
 * Schema for validating date values
 */
class ZodDate private constructor(
    private val validations: List<(LocalDateTime) -> ZodError?>
) : ZodSchema<LocalDateTime> {
    companion object {
        fun schema(): ZodDate = ZodDate(emptyList())
    }
    
    fun min(minDate: LocalDateTime, message: String = "Date must be greater than or equal to ${minDate}"): ZodDate {
        val validation: (LocalDateTime) -> ZodError? = { value ->
            if (value < minDate) ZodError(message) else null
        }
        return ZodDate(validations + validation)
    }
    
    fun max(maxDate: LocalDateTime, message: String = "Date must be less than or equal to ${maxDate}"): ZodDate {
        val validation: (LocalDateTime) -> ZodError? = { value ->
            if (value > maxDate) ZodError(message) else null
        }
        return ZodDate(validations + validation)
    }
    
    override fun parse(input: Any?): LocalDateTime {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<LocalDateTime> {
        val dateInput = when (input) {
            is LocalDateTime -> input
            is String -> {
                try {
                    // Try to parse standard date formats
                    if (input.contains("T")) {
                        // ISO format with time: 2023-01-01T12:00:00
                        LocalDateTime.parse(input)
                    } else {
                        // Date only: 2023-01-01
                        val localDate = LocalDate.parse(input)
                        localDate.atTime(0, 0, 0)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
        
        if (dateInput == null) {
            return ZodResult.Failure(ZodError("Expected date, received ${input?.let { it::class.simpleName } ?: "null"}"))
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(dateInput)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(dateInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}