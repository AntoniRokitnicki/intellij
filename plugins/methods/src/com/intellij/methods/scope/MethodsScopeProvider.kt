package com.intellij.methods.scope

import com.intellij.openapi.project.DumbAware
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx
import com.intellij.psi.search.scope.packageSet.NamedScope

/**
 * Provides named scopes based on method declarations.
 * Suitable for extraction into a plugin module later.
 */
class MethodsScopeProvider : CustomScopesProviderEx(), DumbAware {
  companion object {
    private val ANY_METHOD_SCOPE = NamedScope(
      "FilesWithAnyMethod",
      { "FilesWithAnyMethod" },
      null,
      FilesWithAnyMethodPackageSet()
    )

    private val REGEX_SCOPE = NamedScope(
      "FilesWithMethodNameRegex",
      { "FilesWithMethodNameRegex" },
      null,
      FilesWithMethodNameRegexPackageSet()
    )
  }

  override fun getCustomScopes(): List<NamedScope> = listOf(ANY_METHOD_SCOPE, REGEX_SCOPE)
}
