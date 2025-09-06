package com.example.ignorestrings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.DumbAware

class ToggleIgnoreStringFieldsAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(IgnoredStringsSettings::class.java)
        service.state = IgnoredStringsSettings.State(!service.state.enabled)

        EditorFactory.getInstance().allEditors.forEach { editor ->
            editor.foldingModel.runBatchFoldingOperation { }
        }
        DaemonCodeAnalyzer.getInstance(project).restart()
    }
}
