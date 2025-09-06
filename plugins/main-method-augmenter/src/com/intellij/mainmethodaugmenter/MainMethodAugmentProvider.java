package com.intellij.mainmethodaugmenter;

import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Adds a synthetic {@code main} method to every Java class that doesn't already define one.
 */
public final class MainMethodAugmentProvider extends PsiAugmentProvider {
  @Override
  public @NotNull <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element,
                                                                 @NotNull Class<Psi> type,
                                                                 @Nullable String nameHint) {
    if (type != PsiMethod.class || !(element instanceof PsiClass psiClass)) {
      return Collections.emptyList();
    }
    if (psiClass.isInterface() || psiClass.isAnnotationType()) {
      return Collections.emptyList();
    }
    if (psiClass.findMethodsByName("main", false).length > 0) {
      return Collections.emptyList();
    }
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiMethod mainMethod = factory.createMethodFromText(
      "public static void main(String[] args) {}",
      psiClass
    );
    @SuppressWarnings("unchecked")
    List<Psi> result = (List<Psi>)Collections.singletonList(mainMethod);
    return result;
  }
}
