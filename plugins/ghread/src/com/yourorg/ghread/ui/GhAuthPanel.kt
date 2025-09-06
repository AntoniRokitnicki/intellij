package com.yourorg.ghread.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextArea
import com.yourorg.ghread.core.GhClient
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar

class GhAuthPanel(project: Project) : JPanel(BorderLayout()) {
  private val gh = GhClient(project)
  private val out = JBTextArea().apply { isEditable = false }
  private val bar = JToolBar().apply { isFloatable = false }
  init {
    val check = JButton("Check gh & auth").apply { addActionListener { refresh() } }
    bar.add(check)
    add(bar, BorderLayout.NORTH)
    add(out, BorderLayout.CENTER)
    refresh()
  }
  private fun refresh() {
    val sb = StringBuilder()
    sb.append("gh available: ").append(gh.available()).append('\n')
    val status = gh.authStatus()
    sb.append("auth ok: ").append(status.ok).append('\n')
    sb.append(status.out.ifBlank { status.err })
    out.text = sb.toString()
  }
}
