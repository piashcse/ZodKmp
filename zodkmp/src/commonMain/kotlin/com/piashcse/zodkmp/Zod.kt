package com.piashcse.zodkmp

import kotlin.reflect.KClass

/**
 * Main entry point for ZodKmp validation schemas.
 */
public object Zod {
    public fun string(): ZodString = ZodString.schema()

    public fun number(): ZodNumber = ZodNumber.schema()

    public fun boolean(): ZodBoolean = ZodBoolean.schema()

    public fun date(): ZodDate = ZodDate.schema()

    public fun nullType(): ZodNull = ZodNull.schema()

    public fun `null`(): ZodNull = ZodNull.schema()

    public fun undefined(): ZodUndefined = ZodUndefined.schema()

    public fun any(): ZodAny = ZodAny.schema()

    public fun unknown(): ZodUnknown = ZodUnknown.schema()

    public fun never(): ZodNever = ZodNever.schema()

    public fun void(): ZodVoid = ZodVoid.schema()

    public fun long(): ZodLong = ZodLong.schema()

    public fun <T> objectSchema(
        shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
        parser: (Map<String, Any?>) -> T,
    ): ZodObjectSchema<T> = ZodObjectSchema.build(shapeBuilder, parser)

    public fun <T> literal(value: T): ZodLiteral<T> = ZodLiteral.schema(value)

    public fun <T> literal(vararg values: T): ZodSchema<T> = ZodLiteral.schema(*values)

    public fun <T> array(elementSchema: ZodSchema<T>): ZodArray<T> = ZodArray.schema(elementSchema)

    public fun <T> set(elementSchema: ZodSchema<T>): ZodSet<T> = ZodSet.schema(elementSchema)

    public fun <K, V> map(
        keySchema: ZodSchema<K>,
        valueSchema: ZodSchema<V>,
    ): ZodMap<K, V> = ZodMap.schema(keySchema, valueSchema)

    public inline fun <reified T : Any> instanceOf(): ZodInstanceOf<T> = ZodInstanceOf.schema()

    public fun <T> enum(vararg values: T): ZodEnum<T> = ZodEnum.schema(*values)

    public fun <T> enum(values: Collection<T>): ZodEnum<T> = ZodEnum.schema(values)

    public inline fun <reified T : Enum<T>> enum(clazz: KClass<T>): ZodEnum<T> = ZodEnum.schema(enumValues<T>().toList(), coerceByEnumName = true)

    public fun tuple(vararg schemas: ZodSchema<*>): ZodTuple = ZodTuple.schema(*schemas)

    public fun tuple(schemas: List<ZodSchema<*>>): ZodTuple = ZodTuple.schema(schemas)

    public fun <T> record(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord.schema(valueSchema)

    public fun <T> record(
        keySchema: ZodString,
        valueSchema: ZodSchema<T>,
    ): ZodRecord<T> = ZodRecord.schema(keySchema, valueSchema)

    public fun <T> looseRecord(valueSchema: ZodSchema<T>): ZodRecord<T> = ZodRecord.looseSchema(valueSchema)

    public fun <T> looseRecord(
        keySchema: ZodString,
        valueSchema: ZodSchema<T>,
    ): ZodRecord<T> = ZodRecord.looseSchema(keySchema, valueSchema)

    public fun <T> strictRecord(
        keySchema: ZodString,
        valueSchema: ZodSchema<T>,
    ): ZodRecord<T> = ZodRecord.strictSchema(keySchema, valueSchema)

    public fun union(vararg schemas: ZodSchema<*>): ZodUnion = ZodUnion.schema(*schemas)

    public fun union(schemas: List<ZodSchema<*>>): ZodUnion = ZodUnion.schema(schemas)

    public fun <T> discriminatedUnion(
        discriminator: String,
        vararg options: ZodObjectSchema<out T>,
    ): ZodDiscriminatedUnion<T> = ZodDiscriminatedUnion.schema(discriminator, options.toList())

    public fun <T> discriminatedUnion(
        discriminator: String,
        options: List<ZodObjectSchema<out T>>,
    ): ZodDiscriminatedUnion<T> = ZodDiscriminatedUnion.schema(discriminator, options)

    public fun <T, U> intersection(
        left: ZodSchema<T>,
        right: ZodSchema<U>,
    ): ZodIntersection<T, U> = ZodIntersection.schema(left, right)

    /**
     * Intersects two object schemas, returning a single object schema with the merged shape.
     * Later definitions override earlier ones for duplicate keys.
     */
    public fun intersection(
        left: ZodObjectSchema<*>,
        right: ZodObjectSchema<*>,
    ): ZodObjectSchema<Map<String, Any?>> = left.extend(right)

    public fun <I> preprocess(
        preprocessor: (Any?) -> Any?,
        schema: ZodSchema<I>,
    ): ZodPreprocess<I> = ZodPreprocess.schema(schema, preprocessor)

    public fun <T, R> pipe(
        first: ZodSchema<T>,
        second: ZodSchema<R>,
    ): ZodPipe<T, R> = ZodPipe.schema(first, second)

    /**
     * Coercing schema factories, mirroring Zod's `z.coerce.*`.
     */
    public object coerce {
        public fun number(): ZodNumber = ZodNumber.schema(coerce = true)

        public fun boolean(): ZodBoolean = ZodBoolean.schema(coerce = true)

        public fun long(): ZodLong = ZodLong.schema(coerce = true)

        public fun date(): ZodDate = ZodDate.schema(coerce = true)
    }
}
