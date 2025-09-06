package com.example.ignorestrings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(name = "IgnoredStringsSettings", storages = [Storage("ignored-strings.xml")])
@Service
class IgnoredStringsSettings : PersistentStateComponent<IgnoredStringsSettings.State> {
    data class State(var enabled: Boolean = true)

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun enabled(project: Project?): Boolean {
            val service = project?.getService(IgnoredStringsSettings::class.java)
                ?: com.intellij.openapi.application.ApplicationManager.getApplication().getService(IgnoredStringsSettings::class.java)
            return service.state.enabled
        }
    }
}
