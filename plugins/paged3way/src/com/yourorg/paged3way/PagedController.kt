package com.yourorg.paged3way

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.requests.SimpleThreesideDiffRequest
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository

internal class PagedController(
  private val project: Project,
  val repo: GitRepository,
  private val sourceFile: VirtualFile,
  settings: PagedSettings
) {
  var branches: List<String> = settings.branches
  var overlap: Int = settings.overlap
  var pinMiddle: String? = settings.pinMiddle
  var index: Int = 0

  private var materialized: MaterializedSet? = null
  private val df = DiffContentFactory.getInstance()

  fun attachMaterialized(set: MaterializedSet) {
    materialized = set
  }

  fun groupCount(): Int {
    val n = branches.size
    return if (n < 3) 0 else n - 2
  }

  fun currentMiddleName(): String? {
    val triple = computeTripleIndices(index)
    return branches[triple.second]
  }

  fun currentWorkingFiles(): List<VirtualFile> {
    val set = materialized ?: return emptyList()
    val idx = computeTripleIndices(index)
    return listOf(set.files[idx.first].vf, set.files[idx.second].vf, set.files[idx.third].vf)
  }

  fun buildRequest(i: Int): SimpleThreesideDiffRequest {
    val set = materialized ?: error("Materialized set not attached")
    val tripleIdx = computeTripleIndices(i)
    val leftVf = set.files[tripleIdx.first].vf
    val midVf = set.files[tripleIdx.second].vf
    val rightVf = set.files[tripleIdx.third].vf

    val ftLeft = FileTypeRegistry.getInstance().getFileTypeByFileName(leftVf.name)
    val ftMid = FileTypeRegistry.getInstance().getFileTypeByFileName(midVf.name)
    val ftRight = FileTypeRegistry.getInstance().getFileTypeByFileName(rightVf.name)

    val left = df.create(project, leftVf)
    val middle = df.create(project, midVf)
    val right = df.create(project, rightVf)

    val title = "${'$'}{sourceFile.name}    ${'$'}{leftVf.name}    ${'$'}{midVf.name}    ${'$'}{rightVf.name}"
    return SimpleThreesideDiffRequest(title, left, middle, right)
  }

  fun prefetchNeighbors() {
    // nothing needed now since files are local
  }

  private fun computeTripleIndices(i: Int): Triple<Int, Int, Int> {
    if (pinMiddle == null) {
      return Triple(i, i + 1, i + 2)
    }
    val midIdx = branches.indexOf(pinMiddle).takeIf { it >= 0 } ?: 1
    val leftIdx = (midIdx - 1).coerceAtLeast(0)
    val rightIdx = (midIdx + 1).coerceAtMost(branches.lastIndex)
    return Triple(leftIdx, midIdx, rightIdx)
  }
}
