package com.yourorg.paged3way

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile

object DirtyGateService {
  fun isTripleDirty(project: Project, files: List<VirtualFile>): Boolean {
    val clm = ChangeListManager.getInstance(project)
    val fdm = FileDocumentManager.getInstance()
    return files.any { vf ->
      clm.getChangesIn(vf).isNotEmpty() ||
      fdm.getCachedDocument(vf)?.let { fdm.isDocumentUnsaved(it) } == true
    }
  }
}
