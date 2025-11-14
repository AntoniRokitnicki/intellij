package com.example.configdsl

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "conf.kts",
    displayName = "Config Script"
)
abstract class ConfigScript(val builder: ConfigBuilder)

