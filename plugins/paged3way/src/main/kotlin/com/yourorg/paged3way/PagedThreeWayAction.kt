package com.yourorg.paged3way

import com.intellij.diff.DiffRequestFactory
import com.intellij.diff.merge.MergeRequest
import com.intellij.diff.merge.MergeRequestProcessor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class PagedThreeWayAction : AnAction() {
  override fun update(e: AnActionEvent) {
    val project = e.project
    val vf = e.getData(CommonDataKeys.VIRTUAL_FILE)
    e.presentation.isEnabled = project != null && vf != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val repo = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(vf) ?: return
    val settings = PagedSettings.defaultFive()
    PagedDialog(project, repo, vf, settings).show()
  }
}

data class PagedSettings(
  val branches: List<String>,
  val overlap: Int = 2,
  val pinMiddle: String? = null
) {
  companion object {
    fun defaultFive(): PagedSettings =
      PagedSettings(branches = listOf("branchA", "branchB", "branchC", "branchD", "branchE"))
  }
}

private class PagedDialog(
  private val project: Project,
  private val repo: GitRepository,
  private val file: VirtualFile,
  private val settings: PagedSettings
) : DialogWrapper(project, true) {

  private val controller = PagedController(project, repo, file, settings)
  private val mergeHolder = JPanel(BorderLayout())
  private var mergeProcessor: MergeRequestProcessor? = null
  private val statusLabel = JLabel("")

  private val prevBtn = JButton("Prev")
  private val nextBtn = JButton("Next")
  private val commitBtn = JButton("Commit")
  private val revertBtn = JButton("Revert")
  private val pinCheck = JCheckBox("Pin middle", settings.pinMiddle != null)
  private val overlapCombo = JComboBox(arrayOf("Overlap 2", "Overlap 1"))

  init {
    title = "Paged Three Way Merge"
    init()
    loadAndShow(0)
  }

  override fun createCenterPanel(): JComponent {
    val panel = JPanel(BorderLayout())
    panel.border = JBUI.Borders.empty(6)

    val top = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
    top.add(prevBtn)
    top.add(nextBtn)
    top.add(commitBtn)
    top.add(revertBtn)
    top.add(pinCheck)
    top.add(overlapCombo)
    top.add(statusLabel)
    panel.add(top, BorderLayout.NORTH)
    panel.add(mergeHolder, BorderLayout.CENTER)

    prevBtn.addActionListener { move(-1) }
    nextBtn.addActionListener { move(1) }
    commitBtn.addActionListener { commitCurrentTriple() }
    revertBtn.addActionListener { revertCurrentTriple() }
    pinCheck.addActionListener {
      controller.pinMiddle = if (pinCheck.isSelected) controller.currentMiddle() else null
      refresh()
    }
    overlapCombo.selectedIndex = 0
    overlapCombo.addActionListener {
      controller.overlap = if (overlapCombo.selectedIndex == 0) 2 else 1
      controller.index = 0
      refresh()
    }

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
    val triple = controller.computeTripleBranches(controller.index)
    statusLabel.text = "File: ${file.name}    ${triple.first}  ${triple.second}  ${triple.third}"
    ApplicationManager.getApplication().executeOnPooledThread {
      val req = controller.buildRequest(controller.index)
      SwingUtilities.invokeLater {
        mergeProcessor?.let { Disposer.dispose(it) }
        val processor = object : MergeRequestProcessor(project) {}
        processor.init(req)
        Disposer.register(disposable, processor)
        mergeHolder.removeAll()
        mergeHolder.add(processor.component, BorderLayout.CENTER)
        mergeHolder.revalidate()
        mergeHolder.repaint()
        mergeProcessor = processor
      }
      controller.prefetchNeighbors()
    }
  }

  private fun updateButtons() {
    val count = controller.groupCount()
    prevBtn.isEnabled = controller.index > 0
    nextBtn.isEnabled = controller.index < count - 1
    val dirty = DirtyGateService.isTripleDirty(project, controller.currentWorkingFiles())
    val color = if (dirty) JBColor.RED else JBColor.GRAY
    statusLabel.foreground = color
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
    val message = "PagedThreeWay commit"
    ApplicationManager.getApplication().executeOnPooledThread {
      val ok = GitLight.commitFiles(project, repo, files, message)
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
}

private class PagedController(
  private val project: Project,
  val repo: GitRepository,
  val file: VirtualFile,
  settings: PagedSettings
) {
  private val provider = GitBranchContentProvider(project)

  var branches: List<String> = settings.branches
  var overlap: Int = settings.overlap
  var pinMiddle: String? = settings.pinMiddle
  var index: Int = 0

  fun groupCount(): Int {
    val n = branches.size
    return if (n < 3) 0 else n - 2
  }

  fun currentMiddle(): String? {
    val triple = computeTripleBranches(index)
    return triple.second
  }

  fun computeTripleBranches(i: Int): Triple<String, String, String> {
    if (pinMiddle == null) {
      return Triple(branches[i], branches[i + 1], branches[i + 2])
    }
    val midIdx = branches.indexOf(pinMiddle).takeIf { it >= 0 } ?: 1
    val leftIdx = (midIdx - 1).coerceAtLeast(0)
    val rightIdx = (midIdx + 1).coerceAtMost(branches.lastIndex)
    return Triple(branches[leftIdx], branches[midIdx], branches[rightIdx])
  }

  fun currentWorkingFiles(): List<VirtualFile> = listOf(file)

  fun buildRequest(i: Int): MergeRequest {
    val (l, m, r) = computeTripleBranches(i)
    val leftBytes = provider.loadFileAtBranch(repo, l, file).toByteArray()
    val baseBytes = provider.loadFileAtBranch(repo, m, file).toByteArray()
    val rightBytes = provider.loadFileAtBranch(repo, r, file).toByteArray()
    val title = "${file.name}   $l   $m   $r"
    return DiffRequestFactory.getInstance()
      .createMergeRequest(project, file, listOf(leftBytes, baseBytes, rightBytes), title, listOf(l, m, r))
  }

  fun prefetchNeighbors() {
    val neighbors = listOf(index - 1, index + 1).filter { it in 0 until groupCount() }
    neighbors.forEach { idx ->
      ApplicationManager.getApplication().executeOnPooledThread {
        try {
          val (l, m, r) = computeTripleBranches(idx)
          provider.loadFileAtBranch(repo, l, file)
          provider.loadFileAtBranch(repo, m, file)
          provider.loadFileAtBranch(repo, r, file)
        } catch (_: Throwable) {}
      }
    }
  }
}
