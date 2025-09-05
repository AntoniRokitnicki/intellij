package com.yourorg.paged3way

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import java.io.File


data class MaterializedSet(
  val rootDir: VirtualFile,
  val files: List<BranchFile> // aligned to branch order
)

data class BranchFile(
  val branch: String,
  val vf: VirtualFile
)

internal class FileMaterializer(private val project: Project) {

  fun materialize(repo: GitRepository, sourceFile: VirtualFile, branches: List<String>): MaterializedSet {
    val projectBase = project.basePath ?: error("Project basePath missing")
    val targetDirIo = File(projectBase, ".paged3way")
    if (!targetDirIo.exists()) targetDirIo.mkdirs()
    val lfs = LocalFileSystem.getInstance()
    val targetRoot = lfs.refreshAndFindFileByIoFile(targetDirIo) ?: error("Cannot create .paged3way")

    val rel = com.intellij.openapi.vfs.VfsUtilCore.getRelativePath(sourceFile, repo.root)
      ?: error("File outside repository: ${'$'}{sourceFile.path}")

    val outFiles = mutableListOf<BranchFile>()

    branches.forEachIndexed { idx, branch ->
      val content = gitShow(project, repo, branch, rel)
      val safeName = sanitize(sourceFile.name)
      val numbered = String.format("%02d_%s_%s", idx + 1, branch, safeName)
      val ioFile = File(targetDirIo, numbered)

      ioFile.parentFile.mkdirs()
      ioFile.writeText(content)

      val vfile = lfs.refreshAndFindFileByIoFile(ioFile) ?: error("Cannot materialize ${'$'}numbered")
      outFiles += BranchFile(branch, vfile)
    }

    ApplicationManager.getApplication().invokeAndWait { targetRoot.refresh(true, true) }
    return MaterializedSet(targetRoot, outFiles)
  }

  private fun gitShow(project: Project, repo: GitRepository, branch: String, relPath: String): String {
    val handler = GitLineHandler(project, repo.root, GitCommand.SHOW).apply {
      addParameters("${'$'}branch:${'$'}relPath")
      setSilent(true)
    }
    val result = Git.getInstance().runCommand(handler)
    if (!result.success()) {
      return "// file not found in ${'$'}branch\n"
    }
    return result.outputAsJoinedString
  }

  private fun sanitize(name: String): String =
    name.replace('/', '_').replace('\\', '_').replace(' ', '_')
}
