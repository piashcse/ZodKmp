package com.piashcse.zodkmp

import kotlin.reflect.KClass

/**
 * Main entry point for ZodKmp validation schemas.
 */
object Zod {
    fun string() = ZodString.schema()
    fun number() = ZodNumber.schema()
    fun boolean() = ZodBoolean.schema()
    fun date() = ZodDate.schema()
    fun nullType() = ZodNull.schema()

    fun `null`() = ZodNull.schema()

    fun undefined() = ZodUndefined.schema()

    fun any() = ZodAny.schema()
    fun unknown() = ZodUnknown.schema()
    fun never() = ZodNever.schema()
    fun void() = ZodVoid.schema()

    fun long() = ZodLong.schema()

    fun <T> objectSchema(
        shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
        parser: (Map<String, Any?>) -> T
    ) = ZodObjectSchema.build(shapeBuilder, parser)

    fun <T> literal(value: T) = ZodLiteral.schema(value)

    fun <T> literal(vararg values: T): ZodSchema<T> = ZodLiteral.schema(*values)

    fun <T> array(elementSchema: ZodSchema<T>) = ZodArray.schema(elementSchema)

    fun <T> set(elementSchema: ZodSchema<T>) = ZodSet.schema(elementSchema)

    fun <K, V> map(keySchema: ZodSchema<K>, valueSchema: ZodSchema<V>) = ZodMap.schema(keySchema, valueSchema)

    inline fun <reified T : Any> instanceOf(): ZodInstanceOf<T> = ZodInstanceOf.schema()

    fun <T> enum(vararg values: T) = ZodEnum.schema(*values)
    fun <T> enum(values: Collection<T>) = ZodEnum.schema(values)
    inline fun <reified T : Enum<T>> enum(clazz: KClass<T>): ZodEnum<T> =
        ZodEnum.schema(enumValues<T>().toList(), coerceByEnumName = true)

    fun tuple(vararg schemas: ZodSchema<*>) = ZodTuple.schema(*schemas)
    fun tuple(schemas: List<ZodSchema<*>>) = ZodTuple.schema(schemas)

    fun <T> record(valueSchema: ZodSchema<T>) = ZodRecord.schema(valueSchema)
    fun <T> record(keySchema: ZodString, valueSchema: ZodSchema<T>) = ZodRecord.schema(keySchema, valueSchema)
    fun <T> looseRecord(valueSchema: ZodSchema<T>) = ZodRecord.looseSchema(valueSchema)
    fun <T> looseRecord(keySchema: ZodString, valueSchema: ZodSchema<T>) = ZodRecord.looseSchema(keySchema, valueSchema)
    fun <T> strictRecord(keySchema: ZodString, valueSchema: ZodSchema<T>) = ZodRecord.strictSchema(keySchema, valueSchema)

    fun union(vararg schemas: ZodSchema<*>) = ZodUnion.schema(*schemas)
    fun union(schemas: List<ZodSchema<*>>) = ZodUnion.schema(schemas)

    fun <T> discriminatedUnion(
        discriminator: String,
        vararg options: ZodObjectSchema<out T>
    ): ZodDiscriminatedUnion<T> = ZodDiscriminatedUnion.schema(discriminator, options.toList())

    fun <T> discriminatedUnion(
        discriminator: String,
        options: List<ZodObjectSchema<out T>>
    ): ZodDiscriminatedUnion<T> = ZodDiscriminatedUnion.schema(discriminator, options)

    fun <T, U> intersection(left: ZodSchema<T>, right: ZodSchema<U>) = ZodIntersection.schema(left, right)

    /**
     * Intersects two object schemas, returning a single object schema with the merged shape.
     * Later definitions override earlier ones for duplicate keys.
     */
    fun intersection(
        left: ZodObjectSchema<*>,
        right: ZodObjectSchema<*>
    ): ZodObjectSchema<Map<String, Any?>> = left.extend(right)

    fun <I> preprocess(preprocessor: (Any?) -> Any?, schema: ZodSchema<I>): ZodPreprocess<I> =
        ZodPreprocess.schema(schema, preprocessor)

    fun <T, R> pipe(first: ZodSchema<T>, second: ZodSchema<R>): ZodPipe<T, R> = ZodPipe.schema(first, second)

    /**
     * Coercing schema factories, mirroring Zod's `z.coerce.*`.
     */
    object coerce {
        fun number() = ZodNumber.schema(coerce = true)
        fun boolean() = ZodBoolean.schema(coerce = true)
        fun long() = ZodLong.schema(coerce = true)
        fun date() = ZodDate.schema(coerce = true)
    }
}
