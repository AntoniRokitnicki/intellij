package com.example.augmentedmain

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.impl.DefaultJavaProgramRunner
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope

class AugmentedMainRunner : DefaultJavaProgramRunner() {
    override fun getRunnerId(): String = "AugmentedMainRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (!super.canRun(executorId, profile)) return false
        if (profile !is ApplicationConfiguration) return false
        val className = profile.mainClassName ?: return false
        val psiClass = JavaPsiFacade.getInstance(profile.project)
            .findClass(className, GlobalSearchScope.allScope(profile.project)) ?: return false
        return psiClass.findMethodsByName("main", false).isEmpty()
    }

    override fun execute(environment: ExecutionEnvironment) {
        val configuration = environment.runProfile as? ApplicationConfiguration
        val className = configuration?.mainClassName ?: "Unknown"
        val project = environment.project
        val connection = project.messageBus.connect()
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processStarting(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                if (env == environment) {
                    handler.notifyTextAvailable("Running $className\n", ProcessOutputTypes.STDOUT)
                    connection.disconnect()
                }
            }
        })
        super.execute(environment)
    }
}
