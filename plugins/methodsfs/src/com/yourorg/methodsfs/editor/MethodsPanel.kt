package com.yourorg.methodsfs.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.NavigatablePsiElement
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.yourorg.methodsfs.index.MethodsIndexAccess
import com.yourorg.methodsfs.vfs.MethodsVirtualFile
import javax.swing.JPanel
import javax.swing.JScrollPane

internal class MethodsPanel(
  private val project: Project,
  private val vFile: VirtualFile
) : JPanel() {

  private val list = JBList<MethodRow>()

  init {
    layout = java.awt.BorderLayout()
    border = JBUI.Borders.empty(6)
    list.cellRenderer = MethodCellRenderer()
    list.addListSelectionListener {
      val item = list.selectedValue ?: return@addListSelectionListener
      ApplicationManager.getApplication().invokeLater { item.element.navigate(true) }
    }
    add(JScrollPane(list), java.awt.BorderLayout.CENTER)
  }

  fun refresh() {
    ApplicationManager.getApplication().executeOnPooledThread {
      val path = (vFile as MethodsVirtualFile).pathInScheme()
      val rows = ReadAction.compute<List<MethodRow>, RuntimeException> {
        val parsed = parseTarget(path)
        when (parsed) {
          is Target.ByFile -> MethodsIndexAccess.collectForFile(project, parsed.relPath)
          is Target.ByScope -> MethodsIndexAccess.collectForScope(project, parsed.scopeId)
        }
      }
      val text = rows.joinToString(separator = "\n") { it.display }
      (vFile as MethodsVirtualFile).update(text.toByteArray(), System.currentTimeMillis())
      ApplicationManager.getApplication().invokeLater { list.setListData(rows.toTypedArray()) }
    }
  }

  private sealed interface Target {
    data class ByFile(val relPath: String) : Target
    data class ByScope(val scopeId: String) : Target
  }

  private fun parseTarget(path: String): Target = when {
    path.startsWith("by-file/") -> Target.ByFile(path.removePrefix("by-file/"))
    path.startsWith("by-scope/") -> Target.ByScope(path.removePrefix("by-scope/"))
    else -> Target.ByFile(path)
  }
}

internal data class MethodRow(
  val name: String,
  val signature: String,
  val element: NavigatablePsiElement
) {
  val display: String get() = "$name  $signature"
}
