package org.jetbrains.plugins.gradle.ui.dependencies

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.Alarm
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class DependenciesTableEditor(private val project: Project, private val file: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val columns = arrayOf("Configuration", "Group", "Artifact", "Version", "Type", "Extras")
    private val tableModel = object : DefaultTableModel(columns, 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val table = JBTable(tableModel)
    private val panel = JPanel(BorderLayout())
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private val changeSupport = PropertyChangeSupport(this)

    private var rows: List<DepRow> = emptyList()

    init {
        panel.add(JBScrollPane(table), BorderLayout.CENTER)
        table.selectionModel.addListSelectionListener { navigateToSelected() }

        val document: Document? = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file)
        document?.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                scheduleRefresh()
            }
        }, this)

        scheduleRefresh()
    }

    private fun scheduleRefresh() {
        alarm.cancelAllRequests()
        alarm.addRequest({ refresh() }, 250)
    }

    private fun refresh() {
        if (project.isDisposed) return
        ApplicationManager.getApplication().runReadAction {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@runReadAction
            val extracted = GradleGroovyDepsExtractor().extract(psiFile)
            rows = extracted
            val data = extracted.sortedWith(compareBy({ it.configuration }, { it.group }, { it.artifact }))
            ApplicationManager.getApplication().invokeLater {
                tableModel.setRowCount(0)
                for (row in data) {
                    tableModel.addRow(arrayOf(row.configuration, row.group ?: "", row.artifact ?: "", row.version ?: "", row.type, row.extras))
                }
            }
        }
    }

    private fun navigateToSelected() {
        val idx = table.selectedRow
        if (idx < 0 || idx >= rows.size) return
        val dep = rows[idx]
        OpenFileDescriptor(project, file, dep.line, 0).navigate(true)
    }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent = table

    override fun getName(): String = "Dependencies"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = file.isValid

    override fun selectNotify() {
        scheduleRefresh()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun getFile(): VirtualFile = file

    override fun dispose() {}
}
