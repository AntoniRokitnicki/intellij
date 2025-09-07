# IntelliJ Listener Interfaces

This document summarizes common listener interfaces available when developing IntelliJ-based plugins. Each section describes what the listener does and highlights three typical use cases.

## VcsListener
**Description:** Notified when VCS mappings change.
**Top 3 use cases:**
1. Adjust plugin configuration after repository roots are added or removed.
2. Trigger reindexing or rescan affected modules.
3. Update UI components that depend on VCS settings.

## ChangeListListener
**Description:** Tracks changes to VCS changelists.
**Top 3 use cases:**
1. React to files being added or removed from changelists.
2. Enforce coding standards before changes are committed.
3. Synchronize changelist state with external tools.

## BranchChangeListener
**Description:** Receives notifications when the active VCS branch changes.
**Top 3 use cases:**
1. Refresh project configuration when switching branches.
2. Validate work-in-progress against target branch policies.
3. Update status bars or badges reflecting current branch.

## VcsAnnotationRefresher
**Description:** Signals that file annotations, such as blame info, need refreshing.
**Top 3 use cases:**
1. Invalidate cached annotations after commit operations.
2. Update gutter displays when authorship data changes.
3. Refresh external annotation views following VCS actions.

## GitPushListener
**Description:** Informed when a Git push operation completes.
**Top 3 use cases:**
1. Notify issue trackers or CI systems after pushing.
2. Display results of push in custom tool windows.
3. Trigger post-push code analysis.

## GitRepositoryChangeListener
**Description:** Fired when Git repository state updates, excluding index changes.
**Top 3 use cases:**
1. Refresh branch lists when remote refs change.
2. Update plugin caches of repository metadata.
3. Monitor repository for external modifications.

## GitRepositoryStateChangeListener
**Description:** Provides detailed notifications about repository state transitions, e.g., during rebase.
**Top 3 use cases:**
1. Track ongoing operations like merges or rebases.
2. Show warnings when repository enters a detached HEAD state.
3. Automate cleanup tasks after operations finish.

## GitConfigListener
**Description:** Notified when the `.git/config` file is modified.
**Top 3 use cases:**
1. Reapply plugin-specific Git settings when configuration changes.
2. Validate repository settings for required options.
3. Alert users to potential misconfigurations.

## AppLifecycleListener
**Description:** Observes application-level lifecycle events.
**Top 3 use cases:**
1. Initialize global resources at startup.
2. Release resources during shutdown.
3. Log lifecycle transitions for diagnostics.

## ProjectManagerListener
**Description:** Notified about project opening, closing, and creation events.
**Top 3 use cases:**
1. Configure project-specific services on open.
2. Perform cleanup before project close.
3. Track recently opened projects for analytics.

## ModuleManagerListener
**Description:** Watches changes to project modules.
**Top 3 use cases:**
1. Adjust module-level settings when modules are added or removed.
2. Rebuild module dependency graphs.
3. Validate module structure for plugin requirements.

## EditorFactoryListener
**Description:** Handles editor creation and disposal events.
**Top 3 use cases:**
1. Attach custom editor widgets on creation.
2. Track open editor count for resource management.
3. Remove listeners when editors are closed.

## FileEditorManagerListener
**Description:** Monitors file editor opening, closing, and selection changes.
**Top 3 use cases:**
1. Implement recent files navigation.
2. Save editor state across sessions.
3. Update context-sensitive tool windows.

## VirtualFileListener
**Description:** Receives notifications for virtual file system events.
**Top 3 use cases:**
1. React to file creation, deletion, or rename operations.
2. Mirror file changes to external storage.
3. Invalidate caches when files are modified.

## BulkFileListener
**Description:** Handles batch file system changes efficiently.
**Top 3 use cases:**
1. Process large refactorings without performance penalties.
2. Group related file events for transactional operations.
3. Trigger project-wide index updates after mass edits.

## DocumentListener
**Description:** Tracks in-memory document edits.
**Top 3 use cases:**
1. Implement live code analysis as users type.
2. Auto-save drafts after significant edits.
3. Update external systems on text changes.

## FileDocumentManagerListener
**Description:** Notified when documents are saved to or reloaded from disk.
**Top 3 use cases:**
1. Validate file content before saving.
2. Refresh editors when files change externally.
3. Maintain version history snapshots.

## PsiTreeChangeListener
**Description:** Observes changes in the PSI tree representing code structure.
**Top 3 use cases:**
1. Update custom code inspections on structure modifications.
2. Maintain AST-based caches.
3. Track refactoring operations.

## AnActionListener
**Description:** Monitors IDE action invocation lifecycle.
**Top 3 use cases:**
1. Audit command usage.
2. Enable or disable actions dynamically.
3. Implement custom shortcuts processing.

## ToolWindowManagerListener
**Description:** Notified about tool window registration and state changes.
**Top 3 use cases:**
1. Initialize content when a tool window is opened.
2. Persist tool window visibility across sessions.
3. Adjust layout based on active tool windows.

