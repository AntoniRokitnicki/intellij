package com.example.augmentedmain

import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class AugmentedMainAugmentProviderTest : LightJavaCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()
        PsiAugmentProvider.EP_NAME.point.registerExtension(AugmentedMainAugmentProvider(), testRootDisposable)
    }

    fun testAugmentsMissingMain() {
        val psiClass = myFixture.addClass("class Foo {}")
        val methods = psiClass.findMethodsByName("main", false)
        assertEquals(1, methods.size)
    }

    fun testExistingMainUnchanged() {
        val psiClass = myFixture.addClass("class Bar { public static void main(String[] args) {} }")
        val methods = psiClass.findMethodsByName("main", false)
        assertEquals(1, methods.size)
    }
}
