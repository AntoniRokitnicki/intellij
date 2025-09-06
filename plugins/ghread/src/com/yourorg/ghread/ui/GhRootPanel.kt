package com.yourorg.ghread.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import javax.swing.JPanel
import java.awt.BorderLayout

class GhRootPanel(private val project: Project) : JPanel(BorderLayout()) {
  private val tabs = JBTabbedPane()

  init {
    border = JBUI.Borders.empty()
    tabs.addTab("Auth", AllIcons.General.BalloonInformation, GhAuthPanel(project))
    tabs.addTab("Repos", AllIcons.Vcs.Vendors.Github, GhReposPanel(project))
    tabs.addTab("Issues", AllIcons.Actions.ListFiles, GhIssuesPanel(project))
    tabs.addTab("PRs", AllIcons.Actions.InspectCode, GhPullsPanel(project))
    tabs.addTab("Notifications", AllIcons.General.InspectionsEye, GhNotificationsPanel(project))
    tabs.addTab("Commits", AllIcons.Vcs.History, GhCommitsPanel(project))
    tabs.addTab("Releases", AllIcons.Nodes.PpLib, GhReleasesPanel(project))
    tabs.addTab("Gists", AllIcons.Nodes.Folder, GhGistsPanel(project))
    tabs.addTab("Rate Limit", AllIcons.General.InspectionsOK, GhRateLimitPanel(project))
    add(tabs, BorderLayout.CENTER)
  }
}
