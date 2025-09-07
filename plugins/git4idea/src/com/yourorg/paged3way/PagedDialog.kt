package com.yourorg.paged3way

import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleThreesideDiffRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import git4idea.repo.GitRepository
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

internal class PagedDialog(
  private val project: Project,
  private val repo: GitRepository,
  private val sourceFile: VirtualFile,
  private val settings: PagedSettings
) : DialogWrapper(project, true) {

  private val diffPanel = DiffManager.getInstance().createRequestPanel(project, disposable, null)
  private val statusLabel = JLabel("")
  private val prevBtn = JButton("Prev")
  private val nextBtn = JButton("Next")
  private val commitBtn = JButton("Commit")
  private val revertBtn = JButton("Revert")
  private val patchBtn = JButton("Create Patch")
  private val applyPatchBtn = JButton("Apply Patch")
  private val pinCheck = JCheckBox("Pin middle", settings.pinMiddle != null)
  private val overlapCombo = JComboBox(arrayOf("Overlap 2", "Overlap 1"))

  private val controller = PagedController(project, repo, sourceFile, settings)
  private lateinit var materialized: MaterializedSet

  init {
    title = "Paged Three Way Merge"
    init()
    materializeFive()
    loadAndShow(0)
  }

  private fun materializeFive() {
    materialized = FileMaterializer(project).materialize(repo, sourceFile, settings.branches)
    controller.attachMaterialized(materialized)
  }

  override fun createCenterPanel(): JComponent {
    val panel = JPanel(BorderLayout())
    panel.border = JBUI.Borders.empty(6)

    val top = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
    top.add(prevBtn)
    top.add(nextBtn)
    top.add(commitBtn)
    top.add(revertBtn)
    top.add(patchBtn)
    top.add(applyPatchBtn)
    top.add(pinCheck)
    top.add(overlapCombo)
    top.add(statusLabel)

    prevBtn.addActionListener { move(-1) }
    nextBtn.addActionListener { move(1) }
    commitBtn.addActionListener { commitCurrentTriple() }
    revertBtn.addActionListener { revertCurrentTriple() }
    patchBtn.addActionListener { createPatch() }
    applyPatchBtn.addActionListener { applyPatch() }

    pinCheck.addActionListener {
      controller.pinMiddle = if (pinCheck.isSelected) controller.currentMiddleName() else null
      refresh()
    }
    overlapCombo.selectedIndex = 0
    overlapCombo.addActionListener {
      controller.overlap = if (overlapCombo.selectedIndex == 0) 2 else 1
      controller.index = 0
      refresh()
    }

    panel.add(top, BorderLayout.NORTH)
    panel.add(diffPanel.component, BorderLayout.CENTER)
    return panel
  }

  private fun move(delta: Int) {
    if (!gateDirty()) return
    controller.index += delta
    refresh()
  }

  private fun loadAndShow(index: Int) {
    controller.index = index
    refresh()
  }

  private fun refresh() {
    updateButtons()
    ApplicationManager.getApplication().executeOnPooledThread {
      val req: SimpleThreesideDiffRequest = controller.buildRequest(controller.index)
      SwingUtilities.invokeLater { diffPanel.setRequest(req) }
      controller.prefetchNeighbors()
    }
  }

  private fun updateButtons() {
    val count = controller.groupCount()
    prevBtn.isEnabled = controller.index > 0
    nextBtn.isEnabled = controller.index < count - 1
    val dirty = DirtyGateService.isTripleDirty(project, controller.currentWorkingFiles())
    statusLabel.foreground = if (dirty) JBColor.RED else JBColor.GRAY
    statusLabel.text = if (dirty) "Dirty: commit or revert required" else "Clean"
  }

  private fun gateDirty(): Boolean {
    FileDocumentManager.getInstance().saveAllDocuments()
    val dirty = DirtyGateService.isTripleDirty(project, controller.currentWorkingFiles())
    if (dirty) {
      JOptionPane.showMessageDialog(contentPanel, "Save and Commit or Revert to continue")
      return false
    }
    return true
  }

  private fun commitCurrentTriple() {
    FileDocumentManager.getInstance().saveAllDocuments()
    val files = controller.currentWorkingFiles()
    ApplicationManager.getApplication().executeOnPooledThread {
      val ok = GitLight.commitFiles(project, repo, files, "PagedThreeWay commit")
      SwingUtilities.invokeLater { if (ok) refresh() }
    }
  }

  private fun revertCurrentTriple() {
    FileDocumentManager.getInstance().saveAllDocuments()
    val files = controller.currentWorkingFiles()
    ApplicationManager.getApplication().executeOnPooledThread {
      val ok = GitLight.revertFiles(project, repo, files)
      SwingUtilities.invokeLater { if (ok) refresh() }
    }
  }

  private fun createPatch() {
    FileDocumentManager.getInstance().saveAllDocuments()
    val files = controller.currentWorkingFiles()
    ApplicationManager.getApplication().executeOnPooledThread {
      PatchOperations.createPatch(project, repo, files)
    }
  }

  private fun applyPatch() {
    FileDocumentManager.getInstance().saveAllDocuments()
    ApplicationManager.getApplication().invokeLater {
      PatchOperations.applyPatchFromClipboard(project)
      refresh()
    }
  }
}

