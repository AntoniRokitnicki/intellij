package com.example.configdsl

class ConfigBuilder {
    private val data = mutableMapOf<String, Any?>()

    infix fun String.to(value: Any?) {
        data[this] = value
    }

    fun section(name: String, block: ConfigBuilder.() -> Unit) {
        val nested = ConfigBuilder().apply(block)
        data[name] = nested.build()
    }

    fun build(): Map<String, Any?> = data
}

