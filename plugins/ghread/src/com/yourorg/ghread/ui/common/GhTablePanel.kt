package com.yourorg.ghread.ui.common

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

open class GhTablePanel : JPanel(BorderLayout()) {
  protected val model = DefaultTableModel()
  protected val table = JBTable(model).apply {
    setShowGrid(false)
    autoCreateRowSorter = true
  }
  init {
    add(JBScrollPane(table), BorderLayout.CENTER)
  }
  protected fun setColumns(vararg names: String) {
    model.setColumnCount(0)
    names.forEach { model.addColumn(it) }
  }
  protected fun clearRows() { model.setRowCount(0) }
  protected fun addRow(vararg cells: Any?) { model.addRow(cells) }
}
