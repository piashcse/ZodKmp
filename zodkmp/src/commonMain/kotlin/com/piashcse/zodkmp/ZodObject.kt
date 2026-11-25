package com.piashcse.zodkmp

/**
 * Schema for validating objects.
 *
 * Extra keys are stripped by default; use [strict] to reject them or [passthrough] to keep
 * them. The most recent call wins.
 */
public class ZodObjectSchema<T> private constructor(
    public val shape: Map<String, ZodSchema<*>>,
    private val parser: (Map<String, Any?>) -> T,
    private val strict: Boolean,
    private val passthrough: Boolean,
    private val validations: List<(T) -> ZodIssue?>,
) : ZodSchema<T> {
    internal fun isStrict(): Boolean = strict

    internal fun isPassthrough(): Boolean = passthrough

    public fun strict(): ZodObjectSchema<T> = ZodObjectSchema(shape, parser, strict = true, passthrough = false, validations)

    public fun strip(): ZodObjectSchema<T> = ZodObjectSchema(shape, parser, strict = false, passthrough = false, validations)

    public fun passthrough(): ZodObjectSchema<T> = ZodObjectSchema(shape, parser, strict = false, passthrough = true, validations)

    public fun pick(vararg keys: String): ZodObjectSchema<Map<String, Any?>> {
        val selected = shape.filterKeys { it in keys }
        return ZodObjectSchema(selected, { it }, strict, passthrough, emptyList())
    }

    public fun omit(vararg keys: String): ZodObjectSchema<Map<String, Any?>> {
        val remaining = shape.filterKeys { it !in keys }
        return ZodObjectSchema(remaining, { it }, strict, passthrough, emptyList())
    }

    public fun partial(): ZodObjectSchema<Map<String, Any?>> {
        val newShape = shape.mapValues { (_, schema) -> schema.optional() as ZodSchema<*> }
        return ZodObjectSchema(newShape, { it }, strict, passthrough, emptyList())
    }

    public fun deepPartial(): ZodObjectSchema<Map<String, Any?>> {
        val newShape = shape.mapValues { (_, schema) -> deepPartialProperty(schema) }
        return ZodObjectSchema(newShape, { it }, strict, passthrough, emptyList())
    }

    /**
     * Merges [other] into this schema. Later definitions win for duplicate keys,
     * matching Zod's `merge`/`extend` behaviour.
     */
    public fun merge(other: ZodObjectSchema<*>): ZodObjectSchema<Map<String, Any?>> {
        val merged = LinkedHashMap(shape)
        merged.putAll(other.shape)
        val mergedStrict = strict || other.strict
        val mergedPassthrough = !mergedStrict && (passthrough || other.passthrough)
        return ZodObjectSchema(merged, { it }, mergedStrict, mergedPassthrough, emptyList())
    }

    public fun extend(other: ZodObjectSchema<*>): ZodObjectSchema<Map<String, Any?>> {
        val merged = LinkedHashMap(shape)
        merged.putAll(other.shape)
        return ZodObjectSchema(merged, { it }, strict, passthrough, emptyList())
    }

    /**
     * Adds a custom refinement while preserving the object schema type.
     */
    public fun refine(predicate: (T) -> Boolean, message: (T) -> String = { "Invalid value: $it" }): ZodObjectSchema<T> = ZodObjectSchema(
        shape,
        parser,
        strict,
        passthrough,
        validations + { value -> if (predicate(value)) null else issue(ZodIssueCode.CUSTOM, message(value)) },
    )

    override fun safeParse(input: Any?): ZodResult<T> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(typeError("object", input))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Expected object with string keys, received key of type ${nonStringKey.typeName()}",
                        received = nonStringKey.typeName(),
                    ),
                ),
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>

        val issues = mutableListOf<ZodIssue>()
        val parsedValues = mutableMapOf<String, Any?>()

        for ((key, schema) in shape) {
            val present = stringKeyMap.containsKey(key)
            val value: Any? = if (present) stringKeyMap[key] else ZodAbsent
            when (val result = schema.safeParse(value)) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    if (present) {
                        issues.addAll(result.error.issues.prepend(ZodPathSegment.Property(key)))
                    } else {
                        issues.add(
                            ZodIssue(
                                ZodIssueCode.REQUIRED,
                                "Required",
                                path = listOf(ZodPathSegment.Property(key)),
                            ),
                        )
                    }
                }
            }
        }

        issues.addAll(extraKeyIssues(stringKeyMap, parsedValues))

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        val value =
            try {
                parser(parsedValues as Map<String, Any?>)
            } catch (e: Exception) {
                return ZodResult.Failure(
                    ZodError(
                        ZodIssue(
                            ZodIssueCode.CUSTOM,
                            "Failed to construct object: ${e::class.simpleName}: ${e.message}",
                            cause = e,
                        ),
                    ),
                )
            }
        return applyValidations(value)
    }

    override suspend fun safeParseSuspend(input: Any?): ZodResult<T> {
        val mapInput =
            input as? Map<*, *>
                ?: return ZodResult.Failure(typeError("object", input))

        val nonStringKey = mapInput.keys.firstOrNull { it !is String }
        if (nonStringKey != null) {
            return ZodResult.Failure(
                ZodError(
                    ZodIssue(
                        ZodIssueCode.INVALID_KEY,
                        "Expected object with string keys, received key of type ${nonStringKey.typeName()}",
                        received = nonStringKey.typeName(),
                    ),
                ),
            )
        }

        @Suppress("UNCHECKED_CAST")
        val stringKeyMap = mapInput as Map<String, Any?>

        val issues = mutableListOf<ZodIssue>()
        val parsedValues = mutableMapOf<String, Any?>()

        for ((key, schema) in shape) {
            val present = stringKeyMap.containsKey(key)
            val value: Any? = if (present) stringKeyMap[key] else ZodAbsent
            when (val result = schema.safeParseSuspend(value)) {
                is ZodResult.Success -> parsedValues[key] = result.data
                is ZodResult.Failure -> {
                    if (present) {
                        issues.addAll(result.error.issues.prepend(ZodPathSegment.Property(key)))
                    } else {
                        issues.add(
                            ZodIssue(
                                ZodIssueCode.REQUIRED,
                                "Required",
                                path = listOf(ZodPathSegment.Property(key)),
                            ),
                        )
                    }
                }
            }
        }

        issues.addAll(extraKeyIssues(stringKeyMap, parsedValues))

        if (issues.isNotEmpty()) {
            return ZodResult.Failure(ZodError(issues))
        }

        val value =
            try {
                parser(parsedValues as Map<String, Any?>)
            } catch (e: Exception) {
                return ZodResult.Failure(
                    ZodError(
                        ZodIssue(
                            ZodIssueCode.CUSTOM,
                            "Failed to construct object: ${e::class.simpleName}: ${e.message}",
                            cause = e,
                        ),
                    ),
                )
            }
        return applyValidations(value)
    }

    private fun extraKeyIssues(stringKeyMap: Map<String, Any?>, parsedValues: MutableMap<String, Any?>): List<ZodIssue> {
        if (strict) {
            val extraKeys = stringKeyMap.keys - shape.keys
            if (extraKeys.isNotEmpty()) {
                return listOf(
                    ZodIssue(
                        ZodIssueCode.UNRECOGNIZED_KEYS,
                        "Unrecognized key(s) in object: ${extraKeys.joinToString(", ")}",
                    ),
                )
            }
        } else if (passthrough) {
            stringKeyMap.filterKeys { it !in shape.keys }.forEach { (key, value) ->
                parsedValues[key] = value
            }
        }
        return emptyList()
    }

    private fun applyValidations(value: T): ZodResult<T> {
        val issues = mutableListOf<ZodIssue>()
        for (validation in validations) {
            validation(value)?.let { issues.add(it) }
        }
        return if (issues.isEmpty()) ZodResult.Success(value) else ZodResult.Failure(ZodError(issues))
    }

    public companion object {
        public fun <T> create(shape: Map<String, ZodSchema<*>>, parser: (Map<String, Any?>) -> T, strict: Boolean = false): ZodObjectSchema<T> =
            ZodObjectSchema(shape, parser, strict, false, emptyList())

        public fun <T> create(shape: Map<String, ZodSchema<*>>, parser: (Map<String, Any?>) -> T, strict: Boolean, passthrough: Boolean): ZodObjectSchema<T> =
            ZodObjectSchema(shape, parser, strict, passthrough, emptyList())

        public fun <T> build(shapeBuilder: ZodObjectShapeBuilder.() -> Unit, parser: (Map<String, Any?>) -> T): ZodObjectSchema<T> {
            val builder = ZodObjectShapeBuilder()
            builder.shapeBuilder()
            return ZodObjectSchema(builder.shape, parser, false, false, emptyList())
        }
    }
}

/**
 * Makes a property optional; nested object properties are made optional too, while
 * collection constraints (`min`, `max`, `unique`, ...) are preserved.
 */
private fun deepPartialProperty(schema: ZodSchema<*>): ZodSchema<*> = when (schema) {
    is ZodObjectSchema<*> -> schema.deepPartial().optional()
    is ZodArray<*> -> schema.withElement(deepPartialElement(schema.elementSchema)).optional()
    is ZodSet<*> -> schema.withElement(deepPartialElement(schema.elementSchema)).optional()
    else -> schema.optional()
}

private fun deepPartialElement(schema: ZodSchema<*>): ZodSchema<*> = when (schema) {
    is ZodObjectSchema<*> -> schema.deepPartial()
    is ZodArray<*> -> schema.withElement(deepPartialElement(schema.elementSchema))
    is ZodSet<*> -> schema.withElement(deepPartialElement(schema.elementSchema))
    else -> schema
}

public infix fun <T, U> ZodObjectSchema<T>.and(other: ZodObjectSchema<U>): ZodObjectSchema<Map<String, Any?>> = merge(other)

public class ZodObjectShapeBuilder {
    public val shape: MutableMap<String, ZodSchema<*>> = mutableMapOf()

    public fun string(name: String, schema: ZodString = ZodString.schema()) {
        shape[name] = schema
    }

    public fun number(name: String, schema: ZodNumber = ZodNumber.schema()) {
        shape[name] = schema
    }

    public fun boolean(name: String, schema: ZodBoolean = ZodBoolean.schema()) {
        shape[name] = schema
    }

    public fun <T> field(name: String, schema: ZodSchema<T>) {
        shape[name] = schema
    }
}

public fun ZodObjectShapeBuilder.optionalString(name: String, schema: ZodString = ZodString.schema()) {
    shape[name] = schema.optional()
}

public fun ZodObjectShapeBuilder.optionalNumber(name: String, schema: ZodNumber = ZodNumber.schema()) {
    shape[name] = schema.optional()
}

public fun ZodObjectShapeBuilder.optionalBoolean(name: String, schema: ZodBoolean = ZodBoolean.schema()) {
    shape[name] = schema.optional()
}

public fun <T> ZodObjectShapeBuilder.optionalField(name: String, schema: ZodSchema<T>) {
    shape[name] = schema.optional()
}
