package com.example.configdsl

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import org.jetbrains.kotlin.script.util.dependenciesFromCurrentContext
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.createScriptDefinitionFromTemplate
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.jvm
import org.jetbrains.kotlin.idea.core.script.shared.definition.BundledScriptDefinition

class ConfigScriptDefinitionSource(project: Project) : ScriptDefinitionsSource {
    override val definitions: Sequence<ScriptDefinition> = sequenceOf(createDefinition())

    private fun createDefinition(): ScriptDefinition {
        val (compilation, evaluation) = createScriptDefinitionFromTemplate(
            KotlinType(ConfigScript::class),
            defaultJvmScriptingHostConfiguration,
            compilation = {
                implicitReceivers(ConfigBuilder::class)
                jvm {
                    dependenciesFromCurrentContext(wholeClasspath = true)
                }
            },
            evaluation = {
                constructorArgs(ConfigBuilder())
            }
        )
        return BundledScriptDefinition(compilation, evaluation)
    }
}

