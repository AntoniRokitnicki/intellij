package com.intellij.methods.scope

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.search.DefaultSearchScopeProviders
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.SearchScopeProvider

/**
 * Adds method-based scopes to search dialogs by wrapping the named scopes
 * provided by [MethodsScopeProvider].
 */
internal class MethodsSearchScopeProvider : SearchScopeProvider {
  override fun getDisplayName(): String = "Methods"

  override fun getSearchScopes(project: Project, dataContext: DataContext): List<SearchScope> {
    val scopes = MethodsScopeProvider().customScopes
    return scopes.map { DefaultSearchScopeProviders.wrapNamedScope(project, it, false) }
  }
}
