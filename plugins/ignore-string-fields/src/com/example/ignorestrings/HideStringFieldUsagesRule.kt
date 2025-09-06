package com.example.ignorestrings

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiField
import com.intellij.psi.CommonClassNames
import com.intellij.usages.Usage
import com.intellij.usages.rules.UsageFilteringRule
import com.intellij.usages.rules.UsageFilteringRuleProvider
import com.intellij.usages.UsageInfo2UsageAdapter

class HideStringFieldUsagesRule : UsageFilteringRule {
    override fun isVisible(usage: Usage): Boolean {
        if (!IgnoredStringsSettings.enabled(null)) return true
        val element = (usage as? UsageInfo2UsageAdapter)?.usageInfo?.element ?: return true
        val field = element.parent as? PsiField ?: return true
        return !field.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)
    }

    class Provider : UsageFilteringRuleProvider {
        override fun getActiveRules(project: Project): Array<UsageFilteringRule> {
            return arrayOf(HideStringFieldUsagesRule())
        }
    }
}
