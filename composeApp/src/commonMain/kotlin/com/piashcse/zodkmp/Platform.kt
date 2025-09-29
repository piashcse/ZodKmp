package com.piashcse.zodkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform