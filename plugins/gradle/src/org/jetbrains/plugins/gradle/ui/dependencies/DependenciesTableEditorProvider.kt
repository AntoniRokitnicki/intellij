package org.jetbrains.plugins.gradle.ui.dependencies

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.groovy.GroovyFileType

class DependenciesTableEditorProvider : FileEditorProvider {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name == "build.gradle" && file.fileType == GroovyFileType.GROOVY_FILE_TYPE
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return DependenciesTableEditor(project, file)
    }

    override fun getEditorTypeId(): String = "GradleDependenciesTable"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT
}
