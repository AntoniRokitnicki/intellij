package com.yourorg.ghread.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "GhReadSettings", storages = [Storage("ghReadSettings.xml")])
class GhSettings : PersistentStateComponent<GhSettings.State> {
  companion object {
    fun getInstance(): GhSettings = ApplicationManager.getApplication().getService(GhSettings::class.java)
  }

  data class State(var enabled: Boolean = false)

  private var state = State()

  override fun getState(): State = state

  override fun loadState(state: State) {
    this.state = state
  }

  var enabled: Boolean
    get() = state.enabled
    set(value) {
      state.enabled = value
    }
}
