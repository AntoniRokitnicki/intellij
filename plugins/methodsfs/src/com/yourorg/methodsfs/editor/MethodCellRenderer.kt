package com.yourorg.methodsfs.editor

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList
import javax.swing.ListCellRenderer

internal class MethodCellRenderer : ListCellRenderer<MethodRow> {
  private val comp = SimpleColoredComponent()
  override fun getListCellRendererComponent(
    list: JList<out MethodRow>,
    value: MethodRow,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): java.awt.Component {
    comp.clear()
    comp.append(value.name)
    comp.append("  ${value.signature}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    comp.isOpaque = true
    comp.background = if (isSelected) list.selectionBackground else list.background
    comp.foreground = if (isSelected) list.selectionForeground else list.foreground
    return comp
  }
}
