package com.intellij.diff.actions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.merge.TextMergeRequest
import com.intellij.diff.contents.DocumentContent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile

/**
 * Sample action demonstrating a simple editable three way merge between three virtual files.
 * This code is intended to be easily extractable into a standalone plugin in the future.
 */
class ThreeWayMergeSampleAction : AnAction("Sample 3-Way Merge"), DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project: Project = e.project ?: return
    val factory = DiffContentFactory.getInstance()

    // Create editable contents backed by light virtual files
    val leftFile = LightVirtualFile("Left.txt", PlainTextFileType.INSTANCE, "Left\n")
    val baseFile = LightVirtualFile("Base.txt", PlainTextFileType.INSTANCE, "Base\n")
    val rightFile = LightVirtualFile("Right.txt", PlainTextFileType.INSTANCE, "Right\n")
    val resultFile = LightVirtualFile("Result.txt", PlainTextFileType.INSTANCE, "")

    val left = factory.create(project, leftFile) as DocumentContent
    val base = factory.create(project, baseFile) as DocumentContent
    val right = factory.create(project, rightFile) as DocumentContent
    val output = factory.create(project, resultFile) as DocumentContent

    val request = object : TextMergeRequest() {
      override fun getContents(): List<DocumentContent> = listOf(left, base, right)
      override fun getOutputContent(): DocumentContent = output
      override fun getContentTitles(): List<String> = listOf("Left", "Base", "Right")
    }

    DiffManager.getInstance().showMerge(project, request)
  }
}

