package org.jetbrains.plugins.bulkj2k

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.actions.JavaToKotlinAction

/**
 * Action that converts all Java files in the project to Kotlin using the built-in J2K converter.
 * It exposes a dialog with available J2K options and performs the conversion in batches.
 *
 * This class lives in the community repo for now but is intended to be extracted into a
 * standalone plugin.  It relies on the Kotlin plugin's internal [JavaToKotlinAction.Handler]
 * API; consumers extracting this module should provide an alternative if the API changes.
 */
class BulkJavaToKotlinAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val module = e.getData(LangDataKeys.MODULE)
            ?: ModuleManager.getInstance(project).modules.firstOrNull()
            ?: return

        val dumbService = DumbService.getInstance(project)
        dumbService.runWhenSmart {
            val files = collectJavaFiles(project)
            if (files.isEmpty()) return@runWhenSmart

            val options = J2kOption.available()
            val dialog = BulkJ2KSettingsDialog(project, options)
            if (!dialog.showAndGet()) return@runWhenSmart

            val settings = dialog.buildSettings()

            val psiFiles = runReadAction {
                files.mapNotNull { PsiManager.getInstance(project).findFile(it) as? PsiJavaFile }
            }

            val chunkSize = 20
            psiFiles.chunked(chunkSize).forEach { chunk ->
                ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    {
                        JavaToKotlinAction.Handler.convertFiles(chunk, project, module, settings = settings)
                    },
                    "Converting Java to Kotlin", true, project
                )
                FileDocumentManager.getInstance().saveAllDocuments()
            }
        }
    }

    private fun collectJavaFiles(project: Project): List<com.intellij.openapi.vfs.VirtualFile> {
        val scope = GlobalSearchScope.projectScope(project)
        val allFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, scope)
        val index = ProjectFileIndex.getInstance(project)
        return allFiles.filter { vf ->
            index.isInSourceContent(vf) &&
            !index.isInLibraryClasses(vf) &&
            !vf.path.contains("/build/") &&
            !vf.path.contains("/out/")
        }.toList()
    }
}
