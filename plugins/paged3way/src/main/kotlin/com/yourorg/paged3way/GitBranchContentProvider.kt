package com.yourorg.paged3way

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import git4idea.GitLocalBranch
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

class GitBranchContentProvider(private val project: Project) {
  fun loadFileAtBranch(repo: GitRepository, branch: String, vf: com.intellij.openapi.vfs.VirtualFile): String {
    val rel = VfsUtilCore.getRelativePath(vf, repo.root) ?: error("File outside repo: ${vf.path}")
    val handler = GitLineHandler(project, repo.root, GitCommand.SHOW).apply {
      addParameters("$branch:$rel")
      setSilent(true)
    }
    val result = Git.getInstance().runCommand(handler)
    if (!result.success()) {
      return "// file not found in $branch\n"
    }
    return result.outputAsJoinedString
  }

  fun currentBranchName(repo: GitRepository): String? {
    val b = repo.currentBranch
    return when (b) {
      is GitLocalBranch -> b.name
      else -> null
    }
  }
}
