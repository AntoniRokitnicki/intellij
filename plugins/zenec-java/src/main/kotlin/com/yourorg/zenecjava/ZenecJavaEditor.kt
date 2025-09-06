package com.yourorg.zenecjava

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.LanguageTextField
import com.intellij.util.ui.JBUI
import com.intellij.lang.java.JavaLanguage
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class ZenecJavaEditor(
  private val project: Project,
  private val jsonFile: VirtualFile
) : UserDataHolderBase(), FileEditor, Disposable {

  private val panel = JPanel(BorderLayout())
  private val javaView = LanguageTextField(JavaLanguage.INSTANCE, project, "", false)
  private val extractor = ZenecToJavaRenderer()

  private val watcher = JsonWatcher(project, jsonFile) {
    refreshNow()
  }

  init {
    panel.border = JBUI.Borders.empty()
    val editor = javaView.editor as? EditorEx
    editor?.isViewer = true
    editor?.settings?.isLineNumbersShown = true
    editor?.settings?.isFoldingOutlineShown = true
    panel.add(javaView, BorderLayout.CENTER)
    refreshNow()
  }

  private fun refreshNow() {
    val psi = PsiManager.getInstance(project).findFile(jsonFile)
    val text = runCatching { extractor.render(psi) }.getOrElse {
      "// Zenec preview error: ${'$'}{it.message}\n${ZenecToJavaRenderer.defaultSkeleton()}"
    }
    ApplicationManager.getApplication().invokeLater {
      javaView.text = text
    }
  }

  override fun getComponent(): JComponent = panel
  override fun getPreferredFocusedComponent(): JComponent = panel
  override fun getName(): String = "Dependencies as Java"
  override fun setState(state: FileEditorState) {}
  override fun isModified(): Boolean = false
  override fun isValid(): Boolean = jsonFile.isValid
  override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
  override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
  override fun getCurrentLocation(): FileEditorLocation? = null
  override fun dispose() { watcher.dispose() }
}
