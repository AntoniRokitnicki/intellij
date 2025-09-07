package com.intellij.depheatmap

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.fileTypes.StdFileTypes

class ComputeCentralityAction : AnAction("Compute Dependency Centrality") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val graph = HashMap<String, MutableSet<String>>()
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val javaFiles = FileTypeIndex.getFiles(StdFileTypes.JAVA, scope)

        for (virtualFile in javaFiles) {
            val psiFile = psiManager.findFile(virtualFile) as? PsiJavaFile ?: continue
            val mainClass = psiFile.classes.firstOrNull() ?: continue
            val className = psiFile.packageName + "." + mainClass.name
            graph.computeIfAbsent(className) { mutableSetOf() }
            val imports = psiFile.importList?.importStatements ?: emptyArray()
            for (imp in imports) {
                val imported = imp.qualifiedName ?: continue
                graph[className]!!.add(imported)
                graph.computeIfAbsent(imported) { mutableSetOf() }
            }
        }

        val ranks = pageRank(graph)
        val top = ranks.entries.sortedByDescending { it.value }.take(10)
        val sb = StringBuilder("Top central classes:\n")
        for ((name, score) in top) {
            sb.append(String.format("%s: %.4f\n", name, score))
        }
        Messages.showInfoMessage(project, sb.toString(), "Dependency Centrality")
    }

    private fun pageRank(graph: Map<String, Set<String>>, iterations: Int = 20, damping: Double = 0.85): Map<String, Double> {
        val nodes = graph.keys
        if (nodes.isEmpty()) return emptyMap()
        val n = nodes.size
        val ranks = nodes.associateWith { 1.0 / n }.toMutableMap()
        repeat(iterations) {
            val newRanks = nodes.associateWith { (1 - damping) / n }.toMutableMap()
            for ((src, outs) in graph) {
                val share = ranks[src]!! / (if (outs.isEmpty()) n else outs.size)
                if (outs.isEmpty()) {
                    for (node in nodes) {
                        newRanks[node] = newRanks[node]!! + damping * share
                    }
                } else {
                    for (dst in outs) {
                        newRanks[dst] = newRanks[dst]!! + damping * share
                    }
                }
            }
            ranks.clear()
            ranks.putAll(newRanks)
        }
        return ranks
    }
}

