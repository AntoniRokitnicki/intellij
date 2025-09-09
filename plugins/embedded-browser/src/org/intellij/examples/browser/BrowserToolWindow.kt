package org.intellij.examples.browser

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.openapi.vcs.ui.TextFieldAction
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.TestOnly
import javax.swing.JComponent

/**
 * Embedded browser panel shown inside a tool window. Designed to be easily
 * extractable into a standalone plugin.
 */
class BrowserToolWindow(private val project: Project) {
  private val browser: JBCefBrowser = JBCefBrowserBuilder().build().apply {
    loadURL(HOME_PAGE)
  }

  private val panel: SimpleToolWindowPanel = SimpleToolWindowPanel(true, true)

  init {
    val toolbar = createToolbar()
    panel.toolbar = toolbar.component
    panel.setContent(browser.component)
  }

  val component: JComponent get() = panel

  @TestOnly
  internal fun getBrowserForTesting(): JBCefBrowser = browser

  private fun createToolbar(): ActionToolbar {
    val group = DefaultActionGroup().apply {
      add(object : AnAction("Back", "Navigate Back", AllIcons.Actions.Back) {
        override fun actionPerformed(e: AnActionEvent) {
          browser.cefBrowser.goBack()
        }

        override fun update(e: AnActionEvent) {
          e.presentation.isEnabled = browser.cefBrowser.canGoBack()
        }
      })

      add(object : AnAction("Forward", "Navigate Forward", AllIcons.Actions.Forward) {
        override fun actionPerformed(e: AnActionEvent) {
          browser.cefBrowser.goForward()
        }

        override fun update(e: AnActionEvent) {
          e.presentation.isEnabled = browser.cefBrowser.canGoForward()
        }
      })

      add(object : AnAction("Reload", "Reload Page", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
          browser.cefBrowser.reload()
        }
      })

      add(object : AnAction("Home", "Go Home", AllIcons.General.Web) {
        override fun actionPerformed(e: AnActionEvent) {
          browser.loadURL(HOME_PAGE)
        }
      })

      add(UrlFieldAction())

      add(object : AnAction("DevTools", "Open Developer Tools", AllIcons.Debugger.Console) {
        override fun actionPerformed(e: AnActionEvent) {
          browser.openDevtools()
        }
      })

      add(object : AnAction("Run JS", "Execute custom JavaScript", AllIcons.Nodes.RunnableMark) {
        override fun actionPerformed(e: AnActionEvent) {
          @Language("JavaScript")
          val script = "alert('Hello from ${'$'}{project.name}!')"
          browser.cefBrowser.executeJavaScript(script, browser.cefBrowser.url, 0)
        }
      })
    }

    return ActionManager.getInstance().createActionToolbar("EmbeddedBrowser", group, true)
  }

  private inner class UrlFieldAction : TextFieldAction("", "Enter URL", AllIcons.General.Web, 30) {
    override fun perform() {
      var url = myField.text.trim()
      if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "https://$url"
      }
      browser.loadURL(url)
    }
  }

  companion object {
    private const val HOME_PAGE: String = "https://www.jetbrains.com"
  }
}
