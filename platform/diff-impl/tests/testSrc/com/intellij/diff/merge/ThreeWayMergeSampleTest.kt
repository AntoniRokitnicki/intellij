package com.intellij.diff.merge

import com.intellij.diff.DiffContentFactoryImpl
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.HeavyDiffTestCase
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.LightVirtualFile

class ThreeWayMergeSampleTest : HeavyDiffTestCase() {
  fun `test editable documents`() {
    val project = project
    val factory = DiffContentFactoryImpl()
    val left = factory.create(project, LightVirtualFile("Left.txt", PlainTextFileType.INSTANCE, "left")) as DocumentContent
    val base = factory.create(project, LightVirtualFile("Base.txt", PlainTextFileType.INSTANCE, "base")) as DocumentContent
    val right = factory.create(project, LightVirtualFile("Right.txt", PlainTextFileType.INSTANCE, "right")) as DocumentContent
    val output = factory.create(project, LightVirtualFile("Result.txt", PlainTextFileType.INSTANCE, "")) as DocumentContent

    assertTrue(left.document.isWritable)
    assertTrue(base.document.isWritable)
    assertTrue(right.document.isWritable)
    assertTrue(output.document.isWritable)
  }
}
