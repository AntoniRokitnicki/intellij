package org.jetbrains.plugins.javatokotlin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.FileTypeIndex
import com.intellij.psi.PsiManager
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.module.ModuleManager
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.idea.actions.JavaToKotlinAction
import org.jetbrains.kotlin.j2k.ConverterSettings
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JCheckBox
import javax.swing.JComponent
import com.intellij.ui.layout.panel
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

/**
 * Converts all Java files in the current project to Kotlin using [JavaToKotlinAction].
 * The action shows a dialog allowing configuration of all [ConverterSettings] options
 * with short descriptions for safer conversion.
 */
class ConvertProjectToKotlinAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val module = ModuleManager.getInstance(project).modules.firstOrNull() ?: return

        val dialog = ConverterOptionsDialog(project)
        if (!dialog.showAndGet()) return

        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
            .mapNotNull { psiManager.findFile(it) as? PsiJavaFile }

        JavaToKotlinAction.Handler.convertFiles(
            files = javaFiles,
            project = project,
            module = module,
            enableExternalCodeProcessing = false,
            askExternalCodeProcessing = false,
            settings = dialog.toSettings()
        )
    }
}

private class ConverterOptionsDialog(project: Project) : DialogWrapper(project) {
    private val forceNotNull = JCheckBox("Force non-null types", true)
    private val specifyLocal = JCheckBox("Specify local variable types")
    private val specifyField = JCheckBox("Specify field types")
    private val openDefault = JCheckBox("Mark classes and methods as open")
    private val publicDefault = JCheckBox("Make members public")
    private val basicMode = JCheckBox("Basic mode (minimal processing)")

    init {
        title = "Java to Kotlin Conversion"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row { forceNotNull() }
        row { specifyLocal() }
        row { specifyField() }
        row { openDefault() }
        row { publicDefault() }
        row { basicMode() }
    }

    fun toSettings() = ConverterSettings(
        forceNotNullTypes = forceNotNull.isSelected,
        specifyLocalVariableTypeByDefault = specifyLocal.isSelected,
        specifyFieldTypeByDefault = specifyField.isSelected,
        openByDefault = openDefault.isSelected,
        publicByDefault = publicDefault.isSelected,
        basicMode = basicMode.isSelected
    )
}
