package com.yourorg.withmebrowser.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.JButton

class WithMeBrowserPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {
  private val tabs = JBTabbedPane()
  private val devtoolsSplitter = JBSplitter(false, 0.82f, 0.2f, 0.95f)

  init {
    border = JBUI.Borders.empty()
    devtoolsSplitter.firstComponent = tabs
    add(devtoolsSplitter, BorderLayout.CENTER)

    // przycisk nowej karty jako przykład
    val newTab = JButton(AllIcons.General.Add).apply { addActionListener { openUrl("https://www.jetbrains.com") } }
    val bar = javax.swing.JToolBar().apply { isFloatable = false; add(newTab) }
    add(bar, BorderLayout.NORTH)
  }

  fun openHome() = openUrl("https://example.org")

  fun openUrl(url: String) {
    val tab = WithMeSingleTab(project, url) { dev ->
      devtoolsSplitter.secondComponent = dev
    }
    tabs.addTab(tab.title(), tab.icon(), tab.component(), tab.tooltip())
    tabs.selectedIndex = tabs.tabCount - 1
  }

  override fun dispose() { /* tabs dispose is handled in tab objects */ }
}
