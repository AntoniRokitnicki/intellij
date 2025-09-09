package com.intellij.mainmethodaugmenter;

import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.util.containers.ContainerUtil;
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
    if (nameHint != null && !"main".equals(nameHint)) {
      return Collections.emptyList();
    }
    if (psiClass.isInterface() || psiClass.isAnnotationType() || psiClass.isEnum() ||
        psiClass.isRecord() || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return Collections.emptyList();
    }
    if (psiClass instanceof PsiExtensibleClass extensible) {
      if (ContainerUtil.exists(extensible.getOwnMethods(), m -> "main".equals(m.getName()))) {
        return Collections.emptyList();
      }
    }
    else if (psiClass.findMethodsByName("main", false).length > 0) {
      // Fallback for non-extensible classes
      return Collections.emptyList();
    }
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiMethod mainMethod = factory.createMethodFromText(
      "public static void main(String[] args) {}",
      psiClass
    );
    return Collections.singletonList(type.cast(mainMethod));
  }
}
