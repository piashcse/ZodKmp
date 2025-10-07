package com.piashcse.zodkmp

class WasmJsPlatform: Platform {
    override val name: String = "Web with WASM"
}

actual fun getPlatform(): Platform = WasmJsPlatform()