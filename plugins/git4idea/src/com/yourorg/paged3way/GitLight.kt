package com.yourorg.paged3way

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

object GitLight {
  fun commitFiles(project: Project, repo: GitRepository, files: List<VirtualFile>, message: String): Boolean {
    if (!addFiles(project, repo, files)) return false
    val commit = GitLineHandler(project, repo.root, GitCommand.COMMIT).apply {
      addParameters("-m", message)
      setSilent(true)
    }
    val res = Git.getInstance().runCommand(commit)
    return res.success()
  }

  fun revertFiles(project: Project, repo: GitRepository, files: List<VirtualFile>): Boolean {
    val paths = files.map { it.toNioPath().toString() }
    val restore = GitLineHandler(project, repo.root, GitCommand.RESTORE).apply {
      addParameters("--source", "HEAD")
      addParameters(*paths.toTypedArray())
      setSilent(true)
    }
    val res = Git.getInstance().runCommand(restore)
    if (res.success()) return true

    val checkout = GitLineHandler(project, repo.root, GitCommand.CHECKOUT).apply {
      addParameters("--")
      addParameters(*paths.toTypedArray())
      setSilent(true)
    }
    val res2 = Git.getInstance().runCommand(checkout)
    return res2.success()
  }

  private fun addFiles(project: Project, repo: GitRepository, files: List<VirtualFile>): Boolean {
    val paths = files.map { it.toNioPath().toString() }
    val add = GitLineHandler(project, repo.root, GitCommand.ADD).apply {
      addParameters(*paths.toTypedArray())
      setSilent(true)
    }
    val res = Git.getInstance().runCommand(add)
    return res.success()
  }
}

