package com.yourorg.paged3way

import com.intellij.diff.DiffManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

class PagedThreeWayAction : AnAction() {
  override fun update(e: AnActionEvent) {
    val project = e.project
    val vf = e.getData(CommonDataKeys.VIRTUAL_FILE)
    e.presentation.isEnabled = project != null && vf != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val repo: GitRepository = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(vf) ?: return

    val settings = PagedSettings.defaultFive()
    val dialog = PagedDialog(project, repo, vf, settings)
    dialog.show()
  }
}

