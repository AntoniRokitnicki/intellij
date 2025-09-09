package com.intellij.methods.vfs

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Ensures the methods:// file system is registered early.
 */
class MethodsVfsInitializer : ProjectActivity, DumbAware {
  override suspend fun execute(project: Project) {
    MethodsVirtualFileSystem.ensureRegistered()
  }
}
