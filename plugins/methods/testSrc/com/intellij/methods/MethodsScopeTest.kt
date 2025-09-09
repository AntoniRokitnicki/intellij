package com.intellij.methods

import com.intellij.methods.scope.FilesWithAnyMethodPackageSet
import com.intellij.openapi.roots.DependencyValidationManager
import com.intellij.testFramework.LightPlatformTestCase

/**
 * Basic tests for method-based scopes.
 */
class MethodsScopeTest : LightPlatformTestCase() {
  fun testFilesWithAnyMethodScope() {
    val psiFile = createFile("A.java", "class A { void foo(){} }")
    val vf = psiFile.virtualFile
    val scope = FilesWithAnyMethodPackageSet()
    val holder = DependencyValidationManager.getInstance(project)
    assertTrue(scope.contains(vf, project, holder))
  }
}
