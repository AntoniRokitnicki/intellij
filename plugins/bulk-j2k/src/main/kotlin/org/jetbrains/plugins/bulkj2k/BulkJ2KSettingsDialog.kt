package org.jetbrains.plugins.bulkj2k

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import org.jetbrains.kotlin.j2k.ConverterSettings
import java.util.LinkedHashMap
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/** Dialog displaying available J2K options. */
class BulkJ2KSettingsDialog(
    project: Project,
    private val options: List<J2kOption>
) : DialogWrapper(project) {

    private val checkBoxes = LinkedHashMap<J2kOption, JBCheckBox>()

    init {
        title = "Java to Kotlin Converter Options"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        for (option in options) {
            val box = JBCheckBox(option.title, option.defaultValue)
            box.toolTipText = option.description
            checkBoxes[option] = box
            panel.add(box)
        }
        return panel
    }

    /** Builds [ConverterSettings] based on user selections and available options. */
    fun buildSettings(): ConverterSettings {
        val selected = checkBoxes.mapValues { it.value.isSelected }
        val settings = ConverterSettings.defaultSettings.copy()
        J2kOption.apply(settings, selected)
        return settings
    }
}
