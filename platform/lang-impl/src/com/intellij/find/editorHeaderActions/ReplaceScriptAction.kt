package com.intellij.find.editorHeaderActions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import com.intellij.find.editorHeaderActions.ReplaceScriptBundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Opens a dialog allowing user to execute a Kotlin script that transforms each selected line.
 */
class ReplaceScriptAction : AnAction(ReplaceScriptBundle.message("replace.script.button"), null, AllIcons.Actions.RunAll), DumbAware {
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = e.getData(CommonDataKeys.EDITOR) != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return
    ReplaceScriptDialog(project, editor).show()
  }
}

private class ReplaceScriptDialog(private val project: Project, private val editor: Editor) : DialogWrapper(project) {
  private val scriptField = EditorTextField("", project, com.intellij.lang.Language.findLanguageByID("kotlin")?.associatedFileType)

  init {
    title = ReplaceScriptBundle.message("replace.script.dialog.title")
    setOKButtonText(ReplaceScriptBundle.message("replace.script.execute"))
    init()
  }

  override fun createCenterPanel(): JComponent {
    val panel = JPanel(BorderLayout())
    panel.add(scriptField, BorderLayout.CENTER)
    return panel
  }

  override fun doOKAction() {
    val script = scriptField.text
    ReplaceScriptHandler.execute(project, editor, script)
    super.doOKAction()
  }
}
