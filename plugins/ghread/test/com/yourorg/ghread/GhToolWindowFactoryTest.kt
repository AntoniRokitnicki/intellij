package com.yourorg.ghread

import com.yourorg.ghread.ui.GhToolWindowFactory
import com.yourorg.ghread.settings.GhSettings
import com.intellij.testFramework.LightPlatformTestCase

class GhToolWindowFactoryTest : LightPlatformTestCase() {
  fun `test tool window availability depends on settings`() {
    val factory = GhToolWindowFactory()
    val settings = GhSettings.getInstance()
    settings.enabled = false
    assertFalse(factory.shouldBeAvailable(project))
    settings.enabled = true
    assertTrue(factory.shouldBeAvailable(project))
    settings.enabled = false
  }
}
