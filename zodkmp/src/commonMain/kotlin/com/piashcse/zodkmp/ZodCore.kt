package com.piashcse.zodkmp

/**
 * Base interface for all Zod schemas.
 *
 * [parse] and [parseSuspend] are implemented in terms of the safe variants so every schema
 * throws the same [ZodValidationException] and no implementation has to duplicate boilerplate.
 */
interface ZodSchema<T> {
    fun safeParse(input: Any?): ZodResult<T>

    fun parse(input: Any?): T = when (val result = safeParse(input)) {
        is ZodResult.Success -> result.data
        is ZodResult.Failure -> throw ZodValidationException(result.error)
    }

    /**
     * Suspended version of [safeParse]. Defaults to [safeParse] for schemas without
     * suspend refinements; schemas containing suspend refinements override this.
     */
    suspend fun safeParseSuspend(input: Any?): ZodResult<T> = safeParse(input)

    suspend fun parseSuspend(input: Any?): T = when (val result = safeParseSuspend(input)) {
        is ZodResult.Success -> result.data
        is ZodResult.Failure -> throw ZodValidationException(result.error)
    }
}

/**
 * Result of a safe parse.
 */
sealed class ZodResult<out T> {
    data class Success<T>(val data: T) : ZodResult<T>()
    data class Failure(val error: ZodError) : ZodResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw ZodValidationException(error)
    }

    inline fun <R> map(transform: (T) -> R): ZodResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    inline fun <R> fold(onSuccess: (T) -> R, onFailure: (ZodError) -> R): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }
}

/**
 * Thrown by [ZodSchema.parse] and [ZodSchema.parseSuspend].
 * Carries the structured [ZodError] instead of only a formatted message.
 */
class ZodValidationException(val error: ZodError) :
    IllegalArgumentException(error.errors.joinToString(", ")) {
    fun formatted(): String = error.errors.joinToString("\n")
}

enum class ZodIssueCode {
    INVALID_TYPE,
    INVALID_LITERAL,
    INVALID_FORMAT,
    INVALID_UNION,
    INVALID_INTERSECTION,
    INVALID_ENUM_VALUE,
    INVALID_KEY,
    UNRECOGNIZED_KEYS,
    TOO_SMALL,
    TOO_BIG,
    NOT_MULTIPLE_OF,
    NOT_FINITE,
    REQUIRED,
    CUSTOM
}

sealed interface ZodPathSegment {
    data class Property(val name: String) : ZodPathSegment
    data class Index(val index: Int) : ZodPathSegment
}

fun List<ZodPathSegment>.renderPath(): String {
    val builder = StringBuilder()
    for (segment in this) {
        when (segment) {
            is ZodPathSegment.Property -> {
                if (builder.isNotEmpty()) builder.append('.')
                builder.append(segment.name)
            }
            is ZodPathSegment.Index -> builder.append('[').append(segment.index).append(']')
        }
    }
    return builder.toString()
}

/**
 * A single validation issue, mirroring Zod's `ZodIssue`.
 */
data class ZodIssue(
    val code: ZodIssueCode,
    val message: String,
    val path: List<ZodPathSegment> = emptyList(),
    val expected: String? = null,
    val received: String? = null,
    val cause: Throwable? = null
) {
    fun formattedMessage(): String =
        if (path.isEmpty()) message else "${path.renderPath()}: $message"
}

/**
 * Structured validation error.
 *
 * [issues] is the structured representation; [errors] keeps the historical formatted-string
 * view (now prefixed with the issue path) for source compatibility.
 */
class ZodError(val issues: List<ZodIssue>) {

    constructor(error: String) : this(listOf(ZodIssue(ZodIssueCode.CUSTOM, error)))
    constructor(issue: ZodIssue) : this(listOf(issue))

    val errors: List<String> get() = issues.map { it.formattedMessage() }

    fun addError(error: String): ZodError = ZodError(issues + ZodIssue(ZodIssueCode.CUSTOM, error))

    fun addErrors(errors: List<String>): ZodError =
        ZodError(issues + errors.map { ZodIssue(ZodIssueCode.CUSTOM, it) })

    fun prepend(segment: ZodPathSegment): ZodError = prepend(listOf(segment))

    fun prepend(segments: List<ZodPathSegment>): ZodError {
        if (segments.isEmpty()) return this
        return ZodError(issues.map { it.copy(path = segments + it.path) })
    }

    /**
     * Flattens issues into top-level field errors, similar to Zod's `flatten()`.
     */
    fun flatten(): Map<String, Any> {
        val fieldErrors = mutableMapOf<String, MutableList<String>>()
        val formErrors = mutableListOf<String>()
        for (issue in issues) {
            val property = issue.path.firstOrNull()
            if (property is ZodPathSegment.Property) {
                fieldErrors.getOrPut(property.name) { mutableListOf() }.add(issue.formattedMessage())
            } else {
                formErrors.add(issue.formattedMessage())
            }
        }
        return mapOf("formErrors" to formErrors, "fieldErrors" to fieldErrors)
    }

    override fun toString(): String = "ZodError(issues=$issues)"
}

internal fun typeError(expected: String, received: Any?): ZodError {
    val receivedName = received.typeName()
    return ZodError(
        ZodIssue(
            code = ZodIssueCode.INVALID_TYPE,
            message = "Expected $expected, received $receivedName",
            expected = expected,
            received = receivedName
        )
    )
}

internal fun issue(code: ZodIssueCode, message: String): ZodIssue = ZodIssue(code, message)

/**
 * Compatibility factory for the historical `List<String>` error shape.
 */
internal fun zodError(errors: List<String>): ZodError =
    ZodError(errors.map { ZodIssue(ZodIssueCode.CUSTOM, it) })

internal fun List<ZodIssue>.prepend(segment: ZodPathSegment): List<ZodIssue> = prepend(listOf(segment))

internal fun List<ZodIssue>.prepend(segments: List<ZodPathSegment>): List<ZodIssue> {
    if (segments.isEmpty()) return this
    return map { it.copy(path = segments + it.path) }
}
