package com.intellij.methods.scope;
import com.intellij.openapi.util.DumbAware;
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides named scopes based on method declarations.
 */
public final class MethodsScopeProvider extends CustomScopesProviderEx implements DumbAware {
  private static final NamedScope ANY_METHOD_SCOPE = new NamedScope(
    "FilesWithAnyMethod",
    () -> "FilesWithAnyMethod",
    null,
    new FilesWithAnyMethodPackageSet()
  );

  private static final NamedScope REGEX_SCOPE = new NamedScope(
    "FilesWithMethodNameRegex",
    () -> "FilesWithMethodNameRegex",
    null,
    new FilesWithMethodNameRegexPackageSet()
  );

  @Override
  public @NotNull List<NamedScope> getCustomScopes() {
    return List.of(ANY_METHOD_SCOPE, REGEX_SCOPE);
  }
}
