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
