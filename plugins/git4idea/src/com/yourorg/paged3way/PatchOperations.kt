package com.yourorg.paged3way

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

object PatchOperations {
  fun createPatch(project: Project, repo: GitRepository, files: List<VirtualFile>) {
    val paths = files.map { it.toNioPath().toString() }
    val handler = GitLineHandler(project, repo.root, GitCommand.DIFF).apply {
      addParameters("HEAD")
      addParameters("--")
      addParameters(*paths.toTypedArray())
      setSilent(true)
    }
    val result = Git.getInstance().runCommand(handler)
    if (!result.success()) {
      ApplicationManager.getApplication().invokeLater {
        JOptionPane.showMessageDialog(null, "git diff failed: ${result.errorOutputAsJoinedString}")
      }
      return
    }
    val chooser = JFileChooser()
    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      val file: File = chooser.selectedFile
      file.writeText(result.outputAsJoinedString)
    }
  }

  fun applyPatchFromClipboard(project: Project) {
    val action = com.intellij.openapi.vcs.changes.patch.ApplyPatchFromClipboardAction()
    val ctx: DataContext = SimpleDataContext.getProjectContext(project)
    val e = AnActionEvent.createFromAnAction(action, null, "PagedThreeWay", ctx)
    action.actionPerformed(e)
  }
}
