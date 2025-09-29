package com.piashcse.zodkmp

/**
 * Main entry point for ZodKmp validation schemas
 */
object Zod {
    fun string() = ZodString.schema()
    fun number() = ZodNumber.schema()
    fun boolean() = ZodBoolean.schema()
    fun nullType() = ZodNull.schema()
    fun undefined() = ZodUndefined.schema()
    
    inline fun <reified T> objectSchema(
        shapeBuilder: ZodObjectShapeBuilder.() -> Unit,
        noinline parser: (Map<String, Any?>) -> T
    ) = ZodObjectSchema.build(shapeBuilder, parser)
    
    fun <T> literal(value: T) = ZodLiteral.schema(value)
    
    fun <T> array(elementSchema: ZodSchema<T>) = ZodArray.schema(elementSchema)
    
    fun <T> enum(vararg values: T) = ZodEnum.schema(*values)
    fun <T> enum(values: Collection<T>) = ZodEnum.schema(values)
    
    fun tuple(vararg schemas: ZodSchema<*>) = ZodTuple.schema(*schemas)
    fun tuple(schemas: List<ZodSchema<*>>) = ZodTuple.schema(schemas)
    
    fun <T> record(valueSchema: ZodSchema<T>) = ZodRecord.schema(valueSchema)
    
    fun union(vararg schemas: ZodSchema<*>) = ZodUnion.schema(*schemas)
    fun union(schemas: List<ZodSchema<*>>) = ZodUnion.schema(schemas)
    
    fun <T, U> intersection(left: ZodSchema<T>, right: ZodSchema<U>) = ZodIntersection.schema(left, right)
}