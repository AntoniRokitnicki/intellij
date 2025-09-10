package com.intellij.find.editorHeaderActions

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.find.editorHeaderActions.ReplaceScriptBundle
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.ScriptException

/**
 * Executes a user-provided Kotlin script for every selected line in the given editor.
 *
 * This implementation relies on the Kotlin JSR-223 scripting engine, which is provided by the Kotlin plugin.
 * When extracting this feature into a standalone plugin, declare a dependency on the Kotlin plugin to ensure the engine is available.
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
      Messages.showErrorDialog(project, ex.message ?: "", ReplaceScriptBundle.message("replace.script.dialog.title"))
    }
    catch (ex: Exception) {
      Messages.showErrorDialog(project, ex.message ?: "", ReplaceScriptBundle.message("replace.script.dialog.title"))
    }
  }
}

