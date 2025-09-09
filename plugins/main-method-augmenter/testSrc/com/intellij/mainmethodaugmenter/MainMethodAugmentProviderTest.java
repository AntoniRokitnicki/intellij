package com.intellij.mainmethodaugmenter;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

/**
 * Tests for {@link MainMethodAugmentProvider} ensuring that synthetic main methods are supplied.
 */
public class MainMethodAugmentProviderTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return ""; // no external test data
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    PsiAugmentProvider.EP_NAME.getPoint().registerExtension(new MainMethodAugmentProvider(), myFixture.getTestRootDisposable());
  }

  public void testSyntheticMainIsProvided() {
    PsiClass psiClass = myFixture.addClass("class A {}");
    PsiMethod[] methods = psiClass.findMethodsByName("main", false);
    assertEquals(1, methods.length);
    assertTrue(PsiMethodUtil.isMainMethod(methods[0]));
  }

  public void testExistingMainIsNotDuplicated() {
    PsiClass psiClass = myFixture.addClass("class A { public static void main(String[] args){} }");
    PsiMethod[] methods = psiClass.findMethodsByName("main", false);
    assertEquals(1, methods.length);
  }

  public void testEnumAndAbstractNotAugmented() {
    PsiClass e = myFixture.addClass("enum E {}");
    assertEquals(0, e.findMethodsByName("main", false).length);

    PsiClass abs = myFixture.addClass("abstract class B {}");
    assertEquals(0, abs.findMethodsByName("main", false).length);
  }
}

