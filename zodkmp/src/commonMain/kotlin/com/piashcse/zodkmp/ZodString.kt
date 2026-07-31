package com.piashcse.zodkmp

/**
 * Base interface for all Zod schemas
 */
interface ZodSchema<T> {
    fun parse(input: Any?): T
    fun safeParse(input: Any?): ZodResult<T>
    
    /**
     * Suspended version of [safeParse]. Defaults to [safeParse] for schemas without
     * suspend refinements; schemas with async refinements override this.
     */
    suspend fun safeParseSuspend(input: Any?): ZodResult<T> = safeParse(input)
    
    /**
     * Suspended version of [parse]. Defaults to [parse] for schemas without
     * suspend refinements; schemas with async refinements override this.
     */
    suspend fun parseSuspend(input: Any?): T {
        val result = safeParseSuspend(input)
        return when (result) {
            is ZodResult.Success -> result.data
            is ZodResult.Failure -> throw IllegalArgumentException("Validation failed: ${result.error.errors.joinToString(", ")}")
        }
    }
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
data class ZodString private constructor(
    private val validations: List<(String) -> ZodError?>,
    private val transforms: List<(String) -> String> = emptyList()
) : ZodSchema<String> {
    companion object {
        fun schema(): ZodString = ZodString(emptyList())
        
        private val IPV4_REGEX = Regex("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$")
        private val IPV6_REGEX = Regex(
            "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"
        )
        private val CUID_REGEX = Regex("^c[^\\s-]{8,}$")
        private val CUID2_REGEX = Regex("^[0-9a-z]+$")
        private val ULID_REGEX = Regex("^[0-9A-HJKMNP-TV-Z]{26}$")
        private val NANOID_REGEX = Regex("^[A-Za-z0-9_-]+$")
        private val BASE64_REGEX = Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")
        private val BASE64URL_REGEX = Regex("^(?:[A-Za-z0-9_-]{4})*(?:[A-Za-z0-9_-]{2}==|[A-Za-z0-9_-]{3}=)?$")
        
        private const val HIGH_SURROGATE_START = 0xD800
        private const val HIGH_SURROGATE_END = 0xDBFF
        private const val LOW_SURROGATE_START = 0xDC00
        private const val LOW_SURROGATE_END = 0xDFFF
    }
    
    fun min(length: Int, message: String = "String must be at least $length characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length < length) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun max(length: Int, message: String = "String must be at most $length characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length > length) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun length(exact: Int, message: String = "String must be exactly $exact characters long"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.length != exact) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun email(message: String = "Invalid email format"): ZodString {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        val validation: (String) -> ZodError? = { value ->
            if (!emailRegex.matches(value)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun url(message: String = "Invalid URL format"): ZodString {
        val urlRegex = Regex("^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$")
        val validation: (String) -> ZodError? = { value ->
            if (!urlRegex.matches(value)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun regex(pattern: Regex, message: String = "String does not match required pattern"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!pattern.matches(value)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun startsWith(prefix: String, message: String = "String must start with '$prefix'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.startsWith(prefix)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun endsWith(suffix: String, message: String = "String must end with '$suffix'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.endsWith(suffix)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun includes(substring: String, message: String = "String must contain '$substring'"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (!value.contains(substring)) ZodError(message) else null
        }
        return copy(validations = validations + validation)
    }
    
    fun uuid(message: String = "Invalid UUID format"): ZodString {
        val regex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        return format(regex, message)
    }
    
    fun ipv4(message: String = "Invalid IPv4 address"): ZodString {
        val regex = Regex("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$")
        return format(regex, message)
    }
    
    fun ipv6(message: String = "Invalid IPv6 address"): ZodString {
        val regex = Regex(
            "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"
        )
        return format(regex, message)
    }
    
    fun ip(message: String = "Invalid IP address"): ZodString {
        return copy(validations = validations + { value ->
            val valid = IPV4_REGEX.matches(value) || IPV6_REGEX.matches(value)
            if (valid) null else ZodError(message)
        })
    }
    
    fun emoji(message: String = "Invalid emoji"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (value.isEmojiString()) null else ZodError(message)
        }
        return copy(validations = validations + validation)
    }
    
    fun cuid(message: String = "Invalid CUID"): ZodString = format(CUID_REGEX, message)
    
    fun cuid2(message: String = "Invalid CUID2"): ZodString = format(CUID2_REGEX, message)
    
    fun ulid(message: String = "Invalid ULID"): ZodString = format(ULID_REGEX, message)
    
    fun nanoid(message: String = "Invalid nanoid"): ZodString = format(NANOID_REGEX, message)
    
    fun base64(message: String = "Invalid base64 string"): ZodString = format(BASE64_REGEX, message)
    
    fun base64url(message: String = "Invalid base64url string"): ZodString = format(BASE64URL_REGEX, message)
    
    fun datetime(message: String = "Invalid datetime format"): ZodString {
        val validation: (String) -> ZodError? = { value ->
            val valid = runCatching { kotlinx.datetime.Instant.parse(value) }.isSuccess ||
                runCatching { kotlinx.datetime.LocalDateTime.parse(value) }.isSuccess ||
                runCatching { kotlinx.datetime.LocalDate.parse(value) }.isSuccess
            if (valid) null else ZodError(message)
        }
        return copy(validations = validations + validation)
    }
    
    private fun format(regex: Regex, message: String): ZodString {
        val validation: (String) -> ZodError? = { value ->
            if (regex.matches(value)) null else ZodError(message)
        }
        return copy(validations = validations + validation)
    }
    
    private fun String.isEmojiString(): Boolean {
        if (isEmpty()) return false
        var i = 0
        while (i < length) {
            val c = this[i]
            val codePoint = if (c.code in HIGH_SURROGATE_START..HIGH_SURROGATE_END && i + 1 < length && this[i + 1].code in LOW_SURROGATE_START..LOW_SURROGATE_END) {
                ((c.code - HIGH_SURROGATE_START) shl 10) + (this[i + 1].code - LOW_SURROGATE_START) + 0x10000
            } else {
                c.code
            }
            if (!isEmojiCodePoint(codePoint)) return false
            i += if (codePoint > 0xFFFF) 2 else 1
        }
        return true
    }
    
    private fun isEmojiCodePoint(cp: Int): Boolean = when {
        cp in 0x1F000..0x1FAFF -> true
        cp in 0x2600..0x27BF -> true
        cp in 0x200D..0x200D -> true
        cp in 0xFE00..0xFE0F -> true
        cp in 0x20E3..0x20E3 -> true
        cp in 0x00A9..0x00A9 -> true
        cp in 0x00AE..0x00AE -> true
        cp in 0x203C..0x203C -> true
        cp in 0x2049..0x2049 -> true
        cp in 0x2122..0x2122 -> true
        cp in 0x2139..0x2139 -> true
        cp in 0x2194..0x21AA -> true
        cp in 0x231A..0x231B -> true
        cp in 0x2328..0x2328 -> true
        cp in 0x23CF..0x23CF -> true
        cp in 0x23E9..0x23FA -> true
        cp in 0x24C2..0x24C2 -> true
        cp in 0x25AA..0x25AB -> true
        cp in 0x25B6..0x25B6 -> true
        cp in 0x25C0..0x25C0 -> true
        cp in 0x25FB..0x25FE -> true
        cp in 0x2934..0x2935 -> true
        cp in 0x2B05..0x2B07 -> true
        cp in 0x2B1B..0x2B1C -> true
        cp in 0x2B50..0x2B50 -> true
        cp in 0x2B55..0x2B55 -> true
        cp in 0x3030..0x3030 -> true
        cp in 0x303D..0x303D -> true
        cp in 0x3297..0x3297 -> true
        cp in 0x3299..0x3299 -> true
        else -> false
    }
    
    fun toLowerCase(): ZodString = copy(transforms = transforms + { it.lowercase() })
    
    fun toUpperCase(): ZodString = copy(transforms = transforms + { it.uppercase() })
    
    fun trim(): ZodString = copy(transforms = transforms + { it.trim() })

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
        
        var transformed: String = stringInput
        for (transform in transforms) {
            transformed = transform(transformed)
        }
        
        val errors = mutableListOf<String>()
        
        for (validation in validations) {
            val error = validation(transformed)
            if (error != null) {
                errors.addAll(error.errors)
            }
        }
        
        return if (errors.isEmpty()) {
            ZodResult.Success(transformed)
        } else {
            ZodResult.Failure(ZodError(errors))
        }
    }
}