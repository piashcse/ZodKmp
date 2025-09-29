package com.piashcse.zodkmp

import kotlin.jvm.JvmInline

/**
 * Schema for providing default values when input is null/undefined
 */
class ZodDefault<T> internal constructor(
    private val schema: ZodSchema<T>,
    private val defaultValue: () -> T
) : ZodSchema<T> {
    companion object {
        fun <T> schema(baseSchema: ZodSchema<T>, defaultValue: () -> T): ZodDefault<T> 
            = ZodDefault(baseSchema, defaultValue)
    }
    
    override fun parse(input: Any?): T {
        return if (input == null) {
            defaultValue()
        } else {
            schema.parse(input)
        }
    }
    
    override fun safeParse(input: Any?): ZodResult<T> {
        return if (input == null) {
            ZodResult.Success(defaultValue())
        } else {
            schema.safeParse(input)
        }
    }
}

fun <T> ZodSchema<T>.default(defaultValue: () -> T): ZodDefault<T> = ZodDefault(this, defaultValue)
fun <T> ZodSchema<T>.default(defaultValue: T): ZodDefault<T> = ZodDefault(this, { defaultValue })