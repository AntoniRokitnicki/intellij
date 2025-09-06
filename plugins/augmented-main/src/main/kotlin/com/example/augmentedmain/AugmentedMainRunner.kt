package com.example.augmentedmain

import com.intellij.execution.ExecutionManager
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.ui.TextConsoleBuilderFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope

class AugmentedMainRunner : ProgramRunner<RunnerSettings> {
    override fun getRunnerId(): String = "AugmentedMainRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (executorId != DefaultRunExecutor.EXECUTOR_ID) return false
        if (profile !is ApplicationConfiguration) return false
        val className = profile.mainClassName ?: return false
        val psiClass = JavaPsiFacade.getInstance(profile.project)
            .findClass(className, GlobalSearchScope.allScope(profile.project)) ?: return false
        return psiClass.findMethodsByName("main", false).isEmpty()
    }

    override fun execute(environment: ExecutionEnvironment, callback: ProgramRunner.Callback?) {
        val configuration = environment.runProfile as ApplicationConfiguration
        val className = configuration.mainClassName ?: "Unknown"
        val project = environment.project

        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = NopProcessHandler()
        console.attachToProcess(handler)
        console.print("Running augmented main for $className\n", ConsoleViewContentType.NORMAL_OUTPUT)
        val descriptor = RunContentDescriptor(console, handler, null, className)
        ExecutionManager.getInstance(project).contentManager.showRunContent(environment.executor, descriptor)
        handler.startNotify()
        handler.destroyProcess()
        callback?.processStarted(handler)
    }
}
