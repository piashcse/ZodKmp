package com.piashcse.zodkmp

/**
 * Schema for validating booleans.
 *
 * By default only [Boolean] values are accepted. Use [Zod.coerce] to opt into
 * accepting the strings `"true"` / `"false"`.
 */
class ZodBoolean private constructor(
    private val validations: List<(Boolean) -> ZodError?>,
    private val coerce: Boolean
) : ZodSchema<Boolean> {
    companion object {
        fun schema(coerce: Boolean = false): ZodBoolean = ZodBoolean(emptyList(), coerce)
    }

    fun isEqual(value: Boolean, message: String = "Boolean must be equal to $value"): ZodBoolean {
        val validation: (Boolean) -> ZodError? = { bool ->
            if (bool != value) ZodError(message) else null
        }
        return ZodBoolean(validations + validation, coerce)
    }

    override fun parse(input: Any?): Boolean {
        val result = safeParse(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }

    override fun safeParse(input: Any?): ZodResult<Boolean> {
        val booleanInput = when (input) {
            is Boolean -> input
            is String -> if (coerce) {
                when (input.trim().lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }
            } else {
                null
            }
            else -> null
        }

        if (booleanInput == null) {
            val hint = if (input is String && !coerce) " (use Zod.coerce.boolean() to accept \"true\"/\"false\")" else ""
            return ZodResult.Failure(ZodError("Expected boolean, received ${input.typeName()}$hint"))
        }

        val errors = mutableListOf<String>()
        for (validation in validations) {
            validation(booleanInput)?.let { errors.addAll(it.errors) }
        }

        return if (errors.isEmpty()) {
            ZodResult.Success(booleanInput)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}
