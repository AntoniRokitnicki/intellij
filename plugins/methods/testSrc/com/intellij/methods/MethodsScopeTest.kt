package com.intellij.methods

import com.intellij.methods.scope.FilesWithAnyMethodPackageSet
import com.intellij.methods.scope.MethodsSearchScopeProvider
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.roots.DependencyValidationManager
import com.intellij.psi.search.SearchScopeProvider
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

  fun testSearchScopesProvider() {
    val provider = SearchScopeProvider.EP_NAME.extensionList.filterIsInstance<MethodsSearchScopeProvider>().single()
    val scopes = provider.getSearchScopes(project, SimpleDataContext.getProjectContext(project))
    assertEquals(2, scopes.size)
  }
}
