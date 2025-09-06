package com.yourorg.zenecjava

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeChangeAdapter
import com.intellij.psi.util.PsiTreeChangeEvent
import com.intellij.util.Alarm

class JsonWatcher(
  project: Project,
  private val file: VirtualFile,
  private val onChange: () -> Unit
) : Disposable {

  private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
  private val psiManager = PsiManager.getInstance(project)

  private val listener = object : PsiTreeChangeAdapter() {
    override fun childrenChanged(event: PsiTreeChangeEvent) {
      if (event.file?.virtualFile == file) debounce()
    }
  }

  init {
    psiManager.addPsiTreeChangeListener(listener, this)
  }

  private fun debounce() {
    alarm.cancelAllRequests()
    alarm.addRequest({ onChange() }, 250)
  }

  override fun dispose() {}
}
