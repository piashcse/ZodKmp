package com.piashcse.zodkmp

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Schema for validating strings.
 *
 * Checks are stored and executed in declaration order, so transforms and validations
 * compose exactly as they are written: `Zod.string().min(4).trim()` validates the raw
 * value and then trims it, while `Zod.string().trim().min(4)` trims first.
 */
public class ZodString private constructor(
    private val operations: List<Operation>,
    private val hints: Map<String, JsonElement> = emptyMap(),
) : ZodSchema<String> {
    private sealed interface Operation {
        class Transform(
            val transform: (String) -> String,
        ) : Operation

        class Validate(
            val validate: (String) -> ZodIssue?,
        ) : Operation
    }

    public companion object {
        public fun schema(): ZodString = ZodString(emptyList())

        private val EMAIL_REGEX =
            Regex(
                "^[A-Za-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
                    "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+\$",
            )
        private val SCHEME_REGEX = Regex("^[A-Za-z][A-Za-z0-9+.-]*$")
        private val HOST_REGEX = Regex("^[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?$")
        private val IPV4_REGEX = Regex("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$")
        private val IPV6_REGEX =
            Regex(
                "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$",
            )
        private val CUID_REGEX = Regex("^c[^\\s-]{8,}$")
        private val ULID_REGEX = Regex("^[0-9A-HJKMNP-TV-Z]{26}$")
        private val NANOID_REGEX = Regex("^[A-Za-z0-9_-]+$")
        private val BASE64_REGEX = Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")
        private val BASE64URL_REGEX = Regex("^(?:[A-Za-z0-9_-]{4})*(?:[A-Za-z0-9_-]{2}==|[A-Za-z0-9_-]{3}=)?$")

        private const val MAX_PORT = 65535

        private fun isValidUrl(value: String): Boolean {
            if (value.isEmpty() || value.any { it.isWhitespace() }) return false
            val schemeEnd = value.indexOf("://")
            if (schemeEnd <= 0) return false
            if (!SCHEME_REGEX.matches(value.substring(0, schemeEnd))) return false

            val rest = value.substring(schemeEnd + 3)
            if (rest.isEmpty()) return false

            val authorityEnd = rest.indexOfFirst { it == '/' || it == '?' || it == '#' }
            val authority = if (authorityEnd == -1) rest else rest.substring(0, authorityEnd)
            if (authority.isEmpty()) return false

            val hostPort = authority.substringAfterLast('@')
            if (hostPort.isEmpty()) return false

            if (hostPort.startsWith("[")) {
                val closing = hostPort.indexOf(']')
                if (closing <= 1) return false
                if (!IPV6_REGEX.matches(hostPort.substring(1, closing))) return false
                val suffix = hostPort.substring(closing + 1)
                if (suffix.isEmpty()) return true
                if (!suffix.startsWith(":")) return false
                val port = suffix.substring(1).toIntOrNull() ?: return false
                return port in 0..MAX_PORT
            }

            val colon = hostPort.lastIndexOf(':')
            val host = if (colon >= 0) hostPort.substring(0, colon) else hostPort
            if (host.isEmpty() || !HOST_REGEX.matches(host)) return false

            if (colon >= 0) {
                val port = hostPort.substring(colon + 1).toIntOrNull() ?: return false
                if (port !in 0..MAX_PORT) return false
            }
            return true
        }

        private fun isValidDateTime(value: String): Boolean {
            if (!value.contains('T')) return false
            if (runCatching { LocalDateTime.parse(value) }.isSuccess) return true

            val withoutOffset =
                when {
                    value.endsWith("Z") -> value.dropLast(1)
                    else -> {
                        val plus = value.lastIndexOf('+')
                        val minus = value.lastIndexOf('-')
                        val sign = maxOf(plus, minus)
                        if (sign > value.indexOf('T')) value.substring(0, sign) else value
                    }
                }
            return withoutOffset != value && runCatching { LocalDateTime.parse(withoutOffset) }.isSuccess
        }
    }

    private fun withOperation(
        operation: Operation,
        newHints: Map<String, JsonElement> = emptyMap(),
    ): ZodString = ZodString(operations + operation, hints + newHints)

    private fun transform(transform: (String) -> String): ZodString = withOperation(Operation.Transform(transform))

    private fun validate(
        code: ZodIssueCode = ZodIssueCode.INVALID_FORMAT,
        newHints: Map<String, JsonElement> = emptyMap(),
        validate: (String) -> String?,
    ): ZodString = withOperation(Operation.Validate { value -> validate(value)?.let { issue(code, it) } }, newHints)

    internal fun jsonHints(): Map<String, JsonElement> = hints

    public fun min(
        length: Int,
        message: String = "String must be at least $length characters long",
    ): ZodString =
        validate(ZodIssueCode.TOO_SMALL, mapOf("minLength" to JsonPrimitive(length))) {
            if (it.codePointLength() < length) message else null
        }

    public fun max(
        length: Int,
        message: String = "String must be at most $length characters long",
    ): ZodString =
        validate(ZodIssueCode.TOO_BIG, mapOf("maxLength" to JsonPrimitive(length))) {
            if (it.codePointLength() > length) message else null
        }

    public fun length(
        exact: Int,
        message: String = "String must be exactly $exact characters long",
    ): ZodString =
        validate(
            ZodIssueCode.TOO_SMALL,
            mapOf("minLength" to JsonPrimitive(exact), "maxLength" to JsonPrimitive(exact)),
        ) {
            when {
                it.codePointLength() < exact -> message
                it.codePointLength() > exact -> "String must be exactly $exact characters long"
                else -> null
            }
        }

    public fun email(message: String = "Invalid email format"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("email"))) {
            if (EMAIL_REGEX.matches(it)) null else message
        }

    public fun url(message: String = "Invalid URL format"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("uri"))) {
            if (isValidUrl(it)) null else message
        }

    public fun regex(
        pattern: Regex,
        message: String = "String does not match required pattern",
    ): ZodString =
        validate(newHints = mapOf("pattern" to JsonPrimitive(pattern.pattern))) {
            if (pattern.containsMatchIn(it)) null else message
        }

    public fun startsWith(
        prefix: String,
        message: String = "String must start with '$prefix'",
    ): ZodString = validate { if (it.startsWith(prefix)) null else message }

    public fun endsWith(
        suffix: String,
        message: String = "String must end with '$suffix'",
    ): ZodString = validate { if (it.endsWith(suffix)) null else message }

    public fun includes(
        substring: String,
        message: String = "String must contain '$substring'",
    ): ZodString = validate { if (it.contains(substring)) null else message }

    public fun uuid(message: String = "Invalid UUID format"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("uuid"))) {
            val valid =
                it.length == 36 &&
                    it[8] == '-' &&
                    it[13] == '-' &&
                    it[18] == '-' &&
                    it[23] == '-' &&
                    it.withIndex().all { (index, char) ->
                        index == 8 || index == 13 || index == 18 || index == 23 || char.isHexDigit()
                    }
            if (valid) null else message
        }

    public fun ipv4(message: String = "Invalid IPv4 address"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("ipv4"))) {
            if (IPV4_REGEX.matches(it)) null else message
        }

    public fun ipv6(message: String = "Invalid IPv6 address"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("ipv6"))) {
            if (IPV6_REGEX.matches(it)) null else message
        }

    public fun ip(message: String = "Invalid IP address"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("ip"))) {
            if (IPV4_REGEX.matches(it) || IPV6_REGEX.matches(it)) null else message
        }

    public fun emoji(message: String = "Invalid emoji"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("emoji"))) {
            if (it.isEmojiString()) null else message
        }

    public fun cuid(message: String = "Invalid CUID"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("cuid"))) {
            if (CUID_REGEX.matches(it)) null else message
        }

    public fun cuid2(
        length: Int = 24,
        message: String = "Invalid CUID2",
    ): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("cuid2"))) {
            val valid =
                it.length == length &&
                    it.isNotEmpty() &&
                    it[0] in 'a'..'z' &&
                    it.all { char -> char in 'a'..'z' || char in '0'..'9' }
            if (valid) null else message
        }

    public fun ulid(message: String = "Invalid ULID"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("ulid"))) {
            if (ULID_REGEX.matches(it)) null else message
        }

    public fun nanoid(
        length: Int = 21,
        message: String = "Invalid nanoid",
    ): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("nanoid"))) {
            if (it.length == length && NANOID_REGEX.matches(it)) null else message
        }

    public fun base64(message: String = "Invalid base64 string"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("base64"))) {
            if (it.length % 4 == 0 && BASE64_REGEX.matches(it)) null else message
        }

    public fun base64url(message: String = "Invalid base64url string"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("base64url"))) {
            if (it.length % 4 == 0 && BASE64URL_REGEX.matches(it)) null else message
        }

    public fun datetime(message: String = "Invalid datetime format"): ZodString =
        validate(newHints = mapOf("format" to JsonPrimitive("date-time"))) {
            if (isValidDateTime(it)) null else message
        }

    /**
     * Adds a custom refinement while preserving the [ZodString] type, so further string
     * checks can be chained after it.
     */
    public fun refine(
        predicate: (String) -> Boolean,
        message: (String) -> String = { "Invalid value: $it" },
    ): ZodString =
        withOperation(
            Operation.Validate { value ->
                if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value))
            },
        )

    public fun toLowerCase(): ZodString = transform { it.lowercase() }

    public fun toUpperCase(): ZodString = transform { it.uppercase() }

    public fun trim(): ZodString = transform { it.trim() }

    override fun safeParse(input: Any?): ZodResult<String> {
        val stringInput =
            input as? String
                ?: return ZodResult.Failure(typeError("string", input))

        var value = stringInput
        val issues = mutableListOf<ZodIssue>()

        for (operation in operations) {
            when (operation) {
                is Operation.Transform -> value = operation.transform(value)
                is Operation.Validate -> operation.validate(value)?.let { issues.add(it) }
            }
        }

        return if (issues.isEmpty()) {
            ZodResult.Success(value)
        } else {
            ZodResult.Failure(ZodError(issues))
        }
    }
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

private fun String.codePointLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        val char = this[index]
        index += if (char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) 2 else 1
        count++
    }
    return count
}

private fun Char.isHighSurrogate(): Boolean = code in 0xD800..0xDBFF

private fun Char.isLowSurrogate(): Boolean = code in 0xDC00..0xDFFF

private fun String.codePointAt(index: Int): Int {
    val char = this[index]
    return if (char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) {
        ((char.code - 0xD800) shl 10) + (this[index + 1].code - 0xDC00) + 0x10000
    } else {
        char.code
    }
}

private fun String.isEmojiString(): Boolean {
    if (isEmpty()) return false
    var index = 0
    while (index < length) {
        val codePoint = codePointAt(index)
        val isKeycapBase = codePoint == '#'.code || codePoint == '*'.code || codePoint in '0'.code..'9'.code
        if (isKeycapBase) {
            var next = index + 1
            if (next < length && this[next].code == 0xFE0F) next++
            if (next < length && this[next].code == 0x20E3) {
                index = next + 1
                continue
            }
            if (!isEmojiCodePoint(codePoint)) return false
        }
        if (!isEmojiCodePoint(codePoint)) return false
        index += if (codePoint > 0xFFFF) 2 else 1
    }
    return true
}

private fun isEmojiCodePoint(codePoint: Int): Boolean =
    when {
        codePoint in 0x1F000..0x1FAFF -> true
        codePoint in 0x2600..0x27BF -> true
        codePoint == 0x200D -> true
        codePoint in 0xFE00..0xFE0F -> true
        codePoint == 0x20E3 -> true
        codePoint == 0x00A9 -> true
        codePoint == 0x00AE -> true
        codePoint == 0x203C -> true
        codePoint == 0x2049 -> true
        codePoint == 0x2122 -> true
        codePoint == 0x2139 -> true
        codePoint in 0x2194..0x21AA -> true
        codePoint in 0x231A..0x231B -> true
        codePoint == 0x2328 -> true
        codePoint == 0x23CF -> true
        codePoint in 0x23E9..0x23FA -> true
        codePoint == 0x24C2 -> true
        codePoint in 0x25AA..0x25AB -> true
        codePoint == 0x25B6 -> true
        codePoint == 0x25C0 -> true
        codePoint in 0x25FB..0x25FE -> true
        codePoint in 0x2934..0x2935 -> true
        codePoint in 0x2B05..0x2B07 -> true
        codePoint in 0x2B1B..0x2B1C -> true
        codePoint == 0x2B50 -> true
        codePoint == 0x2B55 -> true
        codePoint == 0x3030 -> true
        codePoint == 0x303D -> true
        codePoint == 0x3297 -> true
        codePoint == 0x3299 -> true
        else -> false
    }
