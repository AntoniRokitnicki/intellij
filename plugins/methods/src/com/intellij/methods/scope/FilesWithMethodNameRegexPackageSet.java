package com.intellij.methods.scope;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSetBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Package set true for files containing a method matching a default regex.
 */
final class FilesWithMethodNameRegexPackageSet extends PackageSetBase {
  private static final Pattern REGEX = Pattern.compile(".*");

  FilesWithMethodNameRegexPackageSet() {
    super(null);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file, @NotNull Project project, NamedScopesHolder holder) {
    if (DumbService.isDumb(project)) return false;
    PsiFile psi = PsiManager.getInstance(project).findFile(file);
    if (psi == null) return false;
    List<String> names = CachedValuesManager.getManager(project).getCachedValue(psi, () -> {
      List<String> list = new ArrayList<>();
      psi.accept(new JavaRecursiveElementVisitor() {
        @Override
        public void visitMethod(@NotNull PsiMethod method) {
          list.add(method.getName());
        }
      });
      return CachedValueProvider.Result.create(list, PsiModificationTracker.getInstance(project));
    });
    for (String name : names) {
      if (REGEX.matcher(name).matches()) return true;
    }
    return false;
  }
}
