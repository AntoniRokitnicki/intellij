package com.intellij.find.editorHeaderActions

import com.intellij.find.FindBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Opens a dialog allowing user to execute a Kotlin script that transforms each selected line.
 */
class ReplaceScriptAction : AnAction(FindBundle.message("find.replace.script.button"), null, AllIcons.Actions.RunAll), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return
    ReplaceScriptDialog(project, editor).show()
  }
}

private class ReplaceScriptDialog(private val project: Project, private val editor: Editor) : DialogWrapper(project) {
  private val scriptField = EditorTextField("", project, com.intellij.lang.Language.findLanguageByID("kotlin")?.associatedFileType)

  init {
    title = FindBundle.message("find.replace.script.dialog.title")
    setOKButtonText(FindBundle.message("find.replace.script.execute"))
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

/**
 * Executes a user-provided Kotlin script for every selected line in the given editor.
 *
 * This implementation relies on the Kotlin JSR-223 scripting engine, which is provided by the Kotlin plugin.
 * When extracting into a standalone plugin, declare a dependency on the Kotlin plugin to ensure the engine is available.
 */
internal object ReplaceScriptHandler {
  fun execute(project: Project, editor: Editor, script: String) {
    val engine = ScriptEngineManager().getEngineByExtension("kts") ?: return
    if (engine !is Invocable || engine !is Compilable) return
    try {
      val invocable = engine as Invocable
      val compiled = (engine as Compilable).compile(script)
      compiled.eval()
      val selectionModel = editor.selectionModel
      if (!selectionModel.hasSelection()) return
      val startLine = selectionModel.selectionStartPosition?.line ?: return
      val endLine = selectionModel.selectionEndPosition?.line ?: startLine
      val document = editor.document
      WriteCommandAction.runWriteCommandAction(project) {
        for (line in startLine..endLine) {
          val start = document.getLineStartOffset(line)
          val end = document.getLineEndOffset(line)
          val text = document.getText(TextRange(start, end))
          val result = invocable.invokeFunction("process", text) as? String ?: return@runWriteCommandAction
          document.replaceString(start, end, result)
        }
      }
    }
    catch (ex: ScriptException) {
      Messages.showErrorDialog(project, ex.message ?: "", FindBundle.message("find.replace.script.dialog.title"))
    }
    catch (ex: Exception) {
      Messages.showErrorDialog(project, ex.message ?: "", FindBundle.message("find.replace.script.dialog.title"))
    }
  }
}
