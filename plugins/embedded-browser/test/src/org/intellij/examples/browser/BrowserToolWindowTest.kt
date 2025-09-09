package org.intellij.examples.browser

import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.ui.jcef.JBCefApp
import org.junit.Assume.assumeTrue
import kotlin.test.assertNotNull

class BrowserToolWindowTest : LightPlatformTestCase() {
  fun testBrowserComponentCreated() {
    assumeTrue(JBCefApp.isSupported())
    val window = BrowserToolWindow(project)
    val browser = window.getBrowserForTesting()
    assertNotNull(browser.cefBrowser)
  }
}
