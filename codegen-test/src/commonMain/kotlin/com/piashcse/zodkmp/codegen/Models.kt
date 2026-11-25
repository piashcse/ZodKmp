package com.piashcse.zodkmp.codegen

import com.piashcse.zodkmp.ZodModel

public enum class Role { ADMIN, USER }

@ZodModel
public data class Address(
    public val city: String,
)

@ZodModel(strict = true)
public data class User(
    public val name: String,
    public val age: Int,
    public val score: Double,
    public val role: Role,
    public val nickname: String?,
    public val tags: List<String>,
    public val address: Address?,
)

@ZodModel
public data class Misc(
    public val code: Short,
    public val marker: Char,
)

@ZodModel
public data class Types(
    public val longValue: Long,
    public val floatValue: Float,
    public val enabled: Boolean,
    public val ages: List<Int>,
    public val roles: List<Role>,
    public val addresses: List<Address>,
    public val maybeCount: Int?,
    public val maybeLong: Long?,
    public val maybeFloat: Float?,
    public val maybeTags: List<String>?,
    public val external: com.piashcse.zodkmp.codegen.other.External?,
)
